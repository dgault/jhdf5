/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.hdf5.h5ar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.AdapterIOutputStreamToOutputStream;
import ch.systemsx.cisd.base.io.IInputStream;
import ch.systemsx.cisd.base.io.IOutputStream;
import ch.systemsx.cisd.hdf5.HDF5DataBlock;
import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.FileFormat;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.SyncMode;
import ch.systemsx.cisd.hdf5.h5ar.NewArchiveEntry.NewDirectoryArchiveEntry;
import ch.systemsx.cisd.hdf5.h5ar.NewArchiveEntry.NewFileArchiveEntry;
import ch.systemsx.cisd.hdf5.h5ar.NewArchiveEntry.NewSymLinkArchiveEntry;
import ch.systemsx.cisd.hdf5.io.HDF5IOAdapterFactory;

/**
 * An archiver based on HDF5 as archive format for directory with fast random access to particular
 * files in the archive.
 * 
 * @author Bernd Rinn
 */
final class HDF5Archiver implements Closeable, Flushable, IHDF5Archiver, IHDF5ArchiveInfoProvider
{
    public static final int CHUNK_SIZE_AUTO = -1;

    private final static int MB = 1024 * 1024;

    final static int BUFFER_SIZE = 10 * MB;

    private final IHDF5Reader hdf5Reader;

    private final IHDF5Writer hdf5WriterOrNull;

    private final boolean closeReaderOnCloseFile;

    private final IErrorStrategy errorStrategy;

    private final DirectoryIndexProvider indexProvider;

    private final byte[] buffer;

    private final HDF5ArchiveUpdater updaterOrNull;

    private final HDF5ArchiveDeleter deleterOrNull;

    private final HDF5ArchiveTraverser processor;

    private final IdCache idCache;

    static IHDF5Reader createHDF5Reader(File archiveFile)
    {
        return HDF5FactoryProvider.get().configureForReading(archiveFile)
                .useUTF8CharacterEncoding().reader();
    }

    static IHDF5Writer createHDF5Writer(File archiveFile, FileFormat fileFormat, boolean noSync)
    {
        final IHDF5WriterConfigurator config = HDF5FactoryProvider.get().configure(archiveFile);
        config.fileFormat(fileFormat);
        config.useUTF8CharacterEncoding();
        if (noSync == false)
        {
            config.syncMode(SyncMode.SYNC);
        }
        return config.writer();
    }

    HDF5Archiver(File archiveFile, boolean readOnly)
    {
        this(archiveFile, readOnly, false, FileFormat.STRICTLY_1_6, null);
    }

    HDF5Archiver(File archiveFile, boolean readOnly, boolean noSync, FileFormat fileFormat,
            IErrorStrategy errorStrategyOrNull)
    {
        this.buffer = new byte[BUFFER_SIZE];
        this.closeReaderOnCloseFile = true;
        this.hdf5WriterOrNull = readOnly ? null : createHDF5Writer(archiveFile, fileFormat, noSync);
        this.hdf5Reader =
                (hdf5WriterOrNull != null) ? hdf5WriterOrNull : createHDF5Reader(archiveFile);
        if (errorStrategyOrNull == null)
        {
            this.errorStrategy = IErrorStrategy.DEFAULT_ERROR_STRATEGY;
        } else
        {
            this.errorStrategy = errorStrategyOrNull;
        }
        this.indexProvider = new DirectoryIndexProvider(hdf5Reader, errorStrategy);
        this.idCache = new IdCache();
        this.processor = new HDF5ArchiveTraverser(hdf5Reader, indexProvider, idCache);
        if (hdf5WriterOrNull == null)
        {
            this.updaterOrNull = null;
            this.deleterOrNull = null;
        } else
        {
            this.updaterOrNull = new HDF5ArchiveUpdater(hdf5WriterOrNull, indexProvider, buffer);
            this.deleterOrNull = new HDF5ArchiveDeleter(hdf5WriterOrNull, indexProvider);
        }
    }

    HDF5Archiver(IHDF5Reader reader, boolean enforceReadOnly, IErrorStrategy errorStrategyOrNull)
    {
        this.buffer = new byte[BUFFER_SIZE];
        this.closeReaderOnCloseFile = false;
        this.hdf5WriterOrNull =
                (enforceReadOnly == false && reader instanceof IHDF5Writer) ? (IHDF5Writer) reader
                        : null;
        if (errorStrategyOrNull == null)
        {
            this.errorStrategy = IErrorStrategy.DEFAULT_ERROR_STRATEGY;
        } else
        {
            this.errorStrategy = errorStrategyOrNull;
        }
        this.hdf5Reader = reader;
        this.indexProvider = new DirectoryIndexProvider(hdf5Reader, errorStrategy);
        this.idCache = new IdCache();
        this.processor = new HDF5ArchiveTraverser(hdf5Reader, indexProvider, idCache);
        if (hdf5WriterOrNull == null)
        {
            this.updaterOrNull = null;
            this.deleterOrNull = null;
        } else
        {
            this.updaterOrNull = new HDF5ArchiveUpdater(hdf5WriterOrNull, indexProvider, buffer);
            this.deleterOrNull = new HDF5ArchiveDeleter(hdf5WriterOrNull, indexProvider);
        }
    }

    //
    // Closeable
    //

    public void close()
    {
        flush();
        if (closeReaderOnCloseFile)
        {
            hdf5Reader.close();
        } else
        {
            indexProvider.close();
        }
    }

    //
    // Flusheable
    //

    public void flush()
    {
        if (hdf5WriterOrNull != null)
        {
            hdf5WriterOrNull.flush();
        }
    }

    //
    // IHDF5ArchiveInfo
    //

    public boolean exists(String path)
    {
        final String normalizedPath = Utils.normalizePath(path);
        final String parentPath = Utils.getParentPath(normalizedPath);
        final String name = normalizedPath.substring(parentPath.length() + 1);
        return indexProvider.get(parentPath, false).exists(name);
    }

    public boolean isDirectory(String path)
    {
        final String normalizedPath = Utils.normalizePath(path);
        final String parentPath = Utils.getParentPath(normalizedPath);
        final String name = normalizedPath.substring(parentPath.length() + 1);
        return indexProvider.get(parentPath, false).isDirectory(name);
    }

    public boolean isRegularFile(String path)
    {
        return isRegularFile(tryGetFileLink(path, false));
    }

    public boolean isSymLink(String path)
    {
        return isSymLink(tryGetFileLink(path, false));
    }

    public ArchiveEntry tryGetEntry(String path, boolean readLinkTarget)
    {
        final String normalizedPath = Utils.normalizePath(path);
        final String parentPath = Utils.getParentPath(normalizedPath);
        final String name = normalizedPath.substring(parentPath.length() + 1);
        return trytoArchiveEntry(parentPath, normalizedPath,
                indexProvider.get(parentPath, readLinkTarget).tryGetLink(name), idCache);
    }

    private LinkRecord tryGetFileLink(String path, boolean readLinkTargets)
    {
        final String normalizedPath = Utils.normalizePath(path);
        final String parentPath = Utils.getParentPath(normalizedPath);
        final String name = normalizedPath.substring(parentPath.length() + 1);
        return indexProvider.get(parentPath, readLinkTargets).tryGetFileLink(name);
    }

    private static boolean isRegularFile(LinkRecord linkOrNull)
    {
        return linkOrNull != null && linkOrNull.isRegularFile();
    }

    private static boolean isSymLink(LinkRecord linkOrNull)
    {
        return linkOrNull != null && linkOrNull.isSymLink();
    }

    private static ArchiveEntry trytoArchiveEntry(String dir, String path, LinkRecord linkOrNull,
            IdCache idCache)
    {
        return linkOrNull != null ? new ArchiveEntry(dir, path, linkOrNull, idCache) : null;
    }

    //
    // IHDF5ArchiveReader
    //

    public List<ArchiveEntry> list(String fileOrDir)
    {
        return list(fileOrDir, ListParameters.DEFAULT);
    }

    public List<ArchiveEntry> list(String fileOrDir, final ListParameters params)
    {
        final List<ArchiveEntry> result = new ArrayList<ArchiveEntry>(100);
        list(fileOrDir, new IListEntryVisitor()
            {
                public void visit(ArchiveEntry entry)
                {
                    if (params.isSuppressDirectoryEntries() == false
                            || entry.isDirectory() == false)
                        result.add(entry);
                }
            }, params);
        return result;
    }

    public List<ArchiveEntry> test()
    {
        final List<ArchiveEntry> result = new ArrayList<ArchiveEntry>(100);
        list("/", new IListEntryVisitor()
        {
            public void visit(ArchiveEntry entry)
            {
                if (entry.isOK() == false)
                {
                    result.add(entry);
                }
            }
        }, ListParameters.TEST);
    return result;
    }

    public IHDF5Archiver list(String fileOrDir, IListEntryVisitor visitor)
    {
        return list(fileOrDir, visitor, ListParameters.DEFAULT);
    }

    public IHDF5Archiver list(String fileOrDir, final IListEntryVisitor visitor,
            ListParameters params)
    {
        final ArchiveEntryListProcessor listProcessor =
                new ArchiveEntryListProcessor(
                        params.isSuppressDirectoryEntries() ? new IListEntryVisitor()
                            {
                                public void visit(ArchiveEntry entry)
                                {
                                    if (entry.isDirectory() == false)
                                    {
                                        visitor.visit(entry);
                                    }
                                }
                            } : visitor, buffer, params.isTestArchive());
        processor.process(fileOrDir, params.isRecursive(), params.isReadLinkTargets(),
                listProcessor);
        return this;
    }

    public List<ArchiveEntry> verifyAgainstFilesystem(String rootDirectory)
    {
        return verifyAgainstFilesystem("/", rootDirectory, VerifyParameters.DEFAULT);
    }

    public List<ArchiveEntry> verifyAgainstFilesystem(String fileOrDir, String rootDirectory)
    {
        return verifyAgainstFilesystem(fileOrDir, rootDirectory, VerifyParameters.DEFAULT);
    }

    public List<ArchiveEntry> verifyAgainstFilesystem(String fileOrDir, String rootDirectory,
            VerifyParameters params)
    {
        final List<ArchiveEntry> verifyErrors = new ArrayList<ArchiveEntry>();
        verifyAgainstFilesystem(fileOrDir, rootDirectory, new IListEntryVisitor()
            {
                public void visit(ArchiveEntry entry)
                {
                    if (entry.isOK() == false)
                        verifyErrors.add(entry);
                }
            }, params);
        return verifyErrors;
    }

    public IHDF5Archiver verifyAgainstFilesystem(String fileOrDir, String rootDirectory,
            IListEntryVisitor visitor)
    {
        return verifyAgainstFilesystem(fileOrDir, rootDirectory, visitor, VerifyParameters.DEFAULT);
    }

    public IHDF5Archiver verifyAgainstFilesystem(String fileOrDir, String rootDirectory,
            IListEntryVisitor visitor, VerifyParameters params)
    {
        final ArchiveEntryVerifyProcessor verifyProcessor =
                new ArchiveEntryVerifyProcessor(visitor, rootDirectory, buffer,
                        params.isVerifyAttributes(), params.isNumeric());
        processor.process(fileOrDir, params.isRecursive(), params.isReadLinkTargets(),
                verifyProcessor);
        return this;
    }

    public IHDF5Archiver extractFile(String path, OutputStream out) throws IOExceptionUnchecked
    {
        if (hdf5Reader.isDataSet(path) == false)
        {
            errorStrategy.dealWithError(new UnarchivingException(path, "not found in archive"));
            return this;
        }
        try
        {
            for (HDF5DataBlock<byte[]> block : hdf5Reader.getAsByteArrayNaturalBlocks(path))
            {
                out.write(block.getData());
            }
        } catch (IOException ex)
        {
            errorStrategy.dealWithError(new UnarchivingException(new File("stdout"), ex));
        }
        return this;
    }

    public byte[] extractFileAsByteArray(String path) throws IOExceptionUnchecked
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        extractFile(path, out);
        return out.toByteArray();
    }

    public IInputStream extractFileAsIInputStream(String path)
    {
        if (hdf5Reader.isDataSet(path) == false)
        {
            errorStrategy.dealWithError(new UnarchivingException(path, "not found in archive"));
            return null;
        }
        return HDF5IOAdapterFactory.asIInputStream(hdf5Reader, path);
    }

    public InputStream extractFileAsInputStream(String path)
    {
        return new AdapterIInputStreamToInputStream(extractFileAsIInputStream(path));
    }

    public IHDF5Archiver extractToFilesystem(File root, String path) throws IllegalStateException
    {
        return extractToFilesystem(root, path, ArchivingStrategy.DEFAULT, null);
    }

    public IHDF5Archiver extractToFilesystem(File root, String path, IListEntryVisitor visitorOrNull)
            throws IllegalStateException
    {
        return extractToFilesystem(root, path, ArchivingStrategy.DEFAULT, visitorOrNull);
    }

    public IHDF5Archiver extractToFilesystem(File root, String path, ArchivingStrategy strategy,
            IListEntryVisitor visitorOrNull) throws IllegalStateException
    {
        final IArchiveEntryProcessor extractor =
                new ArchiveEntryExtractProcessor(visitorOrNull, strategy, root.getAbsolutePath(),
                        buffer);
        processor.process(path, true, true, extractor);
        return this;
    }

    //
    // IHDF5Archiver
    //

    public IHDF5Archiver archiveFromFilesystem(File path) throws IllegalStateException
    {
        return archiveFromFilesystem(path, ArchivingStrategy.DEFAULT, false, (IPathVisitor) null);
    }

    public IHDF5Archiver archiveFromFilesystem(File path, ArchivingStrategy strategy)
            throws IllegalStateException
    {
        return archiveFromFilesystem(path, strategy, false, (IPathVisitor) null);
    }

    public IHDF5Archiver archiveFromFilesystem(File path, IPathVisitor pathVisitorOrNull)
            throws IllegalStateException
    {
        return archiveFromFilesystem(path, ArchivingStrategy.DEFAULT, false, pathVisitorOrNull);
    }

    public IHDF5Archiver archiveFromFilesystem(File path, ArchivingStrategy strategy,
            boolean keepNameFromPath, IPathVisitor pathVisitorOrNull) throws IllegalStateException
    {
        checkReadWrite();
        updaterOrNull.archive(path, strategy, CHUNK_SIZE_AUTO, keepNameFromPath, pathVisitorOrNull);
        return this;
    }

    public IHDF5Archiver archiveFromFilesystem(File root, File path) throws IllegalStateException
    {
        return archiveFromFilesystem(root, path, ArchivingStrategy.DEFAULT);
    }

    public IHDF5Archiver archiveFromFilesystem(File root, File path, ArchivingStrategy strategy)
            throws IllegalStateException
    {
        return archiveFromFilesystem(root, path, strategy, null);
    }

    public IHDF5Archiver archiveFromFilesystem(File root, File path, ArchivingStrategy strategy,
            IPathVisitor pathVisitorOrNull) throws IllegalStateException
    {
        checkReadWrite();
        updaterOrNull.archive(root, path, strategy, CHUNK_SIZE_AUTO, pathVisitorOrNull);
        return this;
    }

    public IHDF5Archiver archiveFile(String path, byte[] data) throws IllegalStateException
    {
        return archiveFile(NewArchiveEntry.file(path), new ByteArrayInputStream(data), null);
    }

    public IHDF5Archiver archiveFile(String path, InputStream input)
    {
        return archiveFile(NewArchiveEntry.file(path), input, null);
    }

    public IHDF5Archiver archiveFile(NewFileArchiveEntry entry, InputStream input)
    {
        return archiveFile(entry, input, null);
    }

    public IHDF5Archiver archiveFile(NewFileArchiveEntry entry, byte[] data)
    {
        return archiveFile(entry, new ByteArrayInputStream(data), null);
    }

    public OutputStream archiveFileAsOutputStream(NewFileArchiveEntry entry)
    {
        return new AdapterIOutputStreamToOutputStream(archiveFileAsIOutputStream(entry));
    }

    public IOutputStream archiveFileAsIOutputStream(NewFileArchiveEntry entry)
    {
        checkReadWrite();
        final LinkRecord link = new LinkRecord(entry);
        final IOutputStream stream =
                updaterOrNull.archiveFile(entry.getParentPath(), link, entry.isCompress(),
                        entry.getChunkSize());
        return stream;
    }

    public IHDF5Archiver archiveFile(NewFileArchiveEntry entry, InputStream input,
            IPathVisitor pathVisitorOrNull)
    {
        checkReadWrite();
        final LinkRecord link = new LinkRecord(entry);
        updaterOrNull.archive(entry.getParentPath(), link, input, entry.isCompress(),
                entry.getChunkSize(), pathVisitorOrNull);
        entry.setCrc32(link.getCrc32());
        return this;
    }

    public IHDF5Archiver archiveSymlink(NewSymLinkArchiveEntry entry)
    {
        return archiveSymlink(entry, null);
    }

    public IHDF5Archiver archiveSymlink(String path, String linkTarget)
    {
        return archiveSymlink(NewArchiveEntry.symlink(path, linkTarget), null);
    }

    public IHDF5Archiver archiveSymlink(NewSymLinkArchiveEntry entry, IPathVisitor pathVisitorOrNull)
    {
        checkReadWrite();
        final LinkRecord link = new LinkRecord(entry);
        updaterOrNull.archive(entry.getParentPath(), link, null, false, CHUNK_SIZE_AUTO,
                pathVisitorOrNull);
        return this;
    }

    public IHDF5Archiver archiveDirectory(String path)
    {
        return archiveDirectory(NewArchiveEntry.directory(path), null);
    }

    public IHDF5Archiver archiveDirectory(NewDirectoryArchiveEntry entry)
    {
        return archiveDirectory(entry, null);
    }

    public IHDF5Archiver archiveDirectory(NewDirectoryArchiveEntry entry,
            IPathVisitor pathVisitorOrNull) throws IllegalStateException, IllegalArgumentException
    {
        checkReadWrite();
        final LinkRecord link = new LinkRecord(entry);
        updaterOrNull.archive(entry.getParentPath(), link, null, false, CHUNK_SIZE_AUTO,
                pathVisitorOrNull);
        return this;
    }

    public IHDF5Archiver delete(String hdf5ObjectPath)
    {
        return delete(Collections.singletonList(hdf5ObjectPath), null);
    }

    public IHDF5Archiver delete(List<String> hdf5ObjectPaths)
    {
        return delete(hdf5ObjectPaths, null);
    }

    public IHDF5Archiver delete(List<String> hdf5ObjectPaths, IPathVisitor pathVisitorOrNull)
    {
        checkReadWrite();
        deleterOrNull.delete(hdf5ObjectPaths, pathVisitorOrNull);
        return this;
    }

    private void checkReadWrite()
    {
        if (updaterOrNull == null)
        {
            throw new IllegalStateException("Cannot update archive in read-only mode.");
        }
    }

}
