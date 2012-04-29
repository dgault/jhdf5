/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.IErrorStrategy;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IOutputStream;
import ch.systemsx.cisd.base.unix.FileLinkType;
import ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5OpaqueType;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import ch.systemsx.cisd.hdf5.IHDF5WriterConfigurator.FileFormat;
import ch.systemsx.cisd.hdf5.io.HDF5IOAdapterFactory;

/**
 * A class to create or update <code>h5ar</code> archives.
 * 
 * @author Bernd Rinn
 */
class HDF5ArchiveUpdater
{
    private static final String OPAQUE_TAG_FILE = "FILE";

    private static final int SIZEHINT_FACTOR = 5;

    private static final int MIN_GROUP_MEMBER_COUNT_TO_COMPUTE_SIZEHINT = 100;

    private static final int SMALL_DATASET_LIMIT = 4096;

    private final IHDF5Writer hdf5Writer;

    private final IDirectoryIndexProvider indexProvider;

    private final IErrorStrategy errorStrategy;

    private final DirectoryIndexUpdater indexUpdater;

    private final byte[] buffer;

    private static class DataSetInfo
    {
        final long size;

        final int crc32;

        DataSetInfo(long size, int crc32)
        {
            super();
            this.size = size;
            this.crc32 = crc32;
        }
    }

    private final class H5ARIOutputStream implements IOutputStream, Flushable
    {
        private final IOutputStream delegate;

        private final String directory;

        private final String path;

        private final LinkRecord link;

        private final CRC32 crc32 = new CRC32();

        private long size = 0;

        H5ARIOutputStream(String normalizedDirectory, LinkRecord link, int chunkSize,
                boolean compress)
        {
            this.directory = normalizedDirectory;
            this.path = Utils.concatLink(this.directory, link.getLinkName());
            this.link = link;
            final HDF5GenericStorageFeatures creationStorageFeature =
                    compress ? HDF5GenericStorageFeatures.GENERIC_DEFLATE
                            : HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION;
            this.delegate =
                    HDF5IOAdapterFactory.asIOutputStream(hdf5Writer, path, creationStorageFeature,
                            getEffectiveChunkSize(chunkSize), OPAQUE_TAG_FILE);
            indexProvider.get(normalizedDirectory, false).addFlushable(this);
        }

        public void write(int b) throws IOExceptionUnchecked
        {
            crc32.update(b);
            ++size;
            delegate.write(b);
        }

        public void write(byte[] b) throws IOExceptionUnchecked
        {
            crc32.update(b);
            size += b.length;
            delegate.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
        {
            crc32.update(b, off, len);
            size += len;
            delegate.write(b, off, len);
        }

        public void flush() throws IOExceptionUnchecked
        {
            link.setCrc32((int) crc32.getValue());
            link.setSize(size);
            final boolean updateImmediateGroupOnly = hdf5Writer.isGroup(directory);
            updateIndicesOnThePath(path, link, updateImmediateGroupOnly);
            delegate.flush();
        }

        public void synchronize() throws IOExceptionUnchecked
        {
            delegate.synchronize();
        }

        public void close() throws IOExceptionUnchecked
        {
            flush();
            delegate.close();
            indexProvider.get(path, false).removeFlushable(this);
        }

    }

    public HDF5ArchiveUpdater(IHDF5Writer hdf5Writer, IDirectoryIndexProvider indexProvider,
            byte[] buffer)
    {
        this.hdf5Writer = hdf5Writer;
        this.indexProvider = indexProvider;
        this.errorStrategy = indexProvider.getErrorStrategy();
        this.indexUpdater = new DirectoryIndexUpdater(indexProvider);
        this.buffer = buffer;
    }

    public HDF5ArchiveUpdater archive(File path, ArchivingStrategy strategy, int chunkSize,
            boolean keepNameFromPath, IPathVisitor pathVisitorOrNull)
    {
        final File absolutePath = Utils.normalizePath(path);
        return archive(keepNameFromPath ? absolutePath.getParentFile() : absolutePath,
                absolutePath, strategy, chunkSize, pathVisitorOrNull);
    }

    public IOutputStream archiveFile(String directory, LinkRecord link, boolean compress,
            int chunkSize)
    {
        if (link.getLinkType() != FileLinkType.REGULAR_FILE)
        {
            errorStrategy.dealWithError(new ArchivingException("A regular file is expected here."));
        }
        return new H5ARIOutputStream(Utils.normalizePath(directory), link, chunkSize, compress);
    }

    public HDF5ArchiveUpdater archive(String directory, LinkRecord link, InputStream inputOrNull,
            boolean compress, int chunkSize)
    {
        boolean ok = true;
        final String normalizedDir = Utils.normalizePath(directory);
        final String hdf5ObjectPath = Utils.concatLink(normalizedDir, link.getLinkName());
        final boolean groupExists = hdf5Writer.isGroup(normalizedDir);
        if (link.getLinkType() == FileLinkType.DIRECTORY)
        {
            if (inputOrNull == null)
            {
                ok = archiveDirectory(normalizedDir, link);
            } else
            {
                errorStrategy.dealWithError(new ArchivingException(
                        "Cannot take InputStream when archiving a directory."));
            }
        } else if (link.getLinkType() == FileLinkType.SYMLINK)
        {
            if (inputOrNull == null)
            {
                ok = archiveSymLink(normalizedDir, link);
            } else
            {
                errorStrategy.dealWithError(new ArchivingException(
                        "Cannot take InputStream when archiving a symlink."));
            }
        } else if (link.getLinkType() == FileLinkType.REGULAR_FILE)
        {
            if (inputOrNull != null)
            {
                final HDF5GenericStorageFeatures compression =
                        compress ? HDF5GenericStorageFeatures.GENERIC_DEFLATE
                                : HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION;
                try
                {
                    final DataSetInfo info =
                            copyToHDF5(inputOrNull, hdf5ObjectPath, compression, chunkSize);
                    link.setCrc32(info.crc32);
                    link.setSize(info.size);
                } catch (IOException ex)
                {
                    ok = false;
                    errorStrategy.dealWithError(new ArchivingException(hdf5ObjectPath, ex));
                } catch (HDF5Exception ex)
                {
                    ok = false;
                    errorStrategy.dealWithError(new ArchivingException(hdf5ObjectPath, ex));
                }
            } else
            {
                errorStrategy.dealWithError(new ArchivingException(
                        "Need to have InputStream when archiving a regular file."));
            }
        } else
        {
            errorStrategy.dealWithError(new ArchivingException(
                    "Don't know how to archive file link type " + link.getLinkType()));
            ok = false;
        }
        if (ok)
        {
            updateIndicesOnThePath(hdf5ObjectPath, link, groupExists);
        }
        return this;
    }

    public HDF5ArchiveUpdater archive(String rootDirInArchive, File path,
            ArchivingStrategy strategy, int chunkSize, IPathVisitor pathVisitorOrNull)
    {
        final File absolutePath = Utils.normalizePath(path);
        final String normalizedRootArchiveInDir = Utils.normalizePath(rootDirInArchive);
        final String hdf5ObjectPath =
                Utils.concatLink(normalizedRootArchiveInDir, absolutePath.getName());
        final String hdf5GroupPath = FilenameUtils.getFullPathNoEndSeparator(hdf5ObjectPath);
        final boolean groupExists =
                hdf5GroupPath.length() == 0 ? true : hdf5Writer.isGroup(hdf5GroupPath);
        final boolean ok;
        int crc32 = 0;
        final LinkRecord linkOrNull = LinkRecord.tryCreate(absolutePath, true, errorStrategy);
        if (linkOrNull != null && linkOrNull.isSymLink())
        {
            ok = archiveSymLink("", linkOrNull, absolutePath, pathVisitorOrNull);
        } else if (absolutePath.isDirectory())
        {
            ok =
                    archiveDirectory(normalizedRootArchiveInDir, absolutePath, true, strategy,
                            chunkSize, pathVisitorOrNull);
        } else if (absolutePath.isFile())
        {
            final LinkRecord pseudoLinkForChecksum = new LinkRecord();
            ok =
                    archiveFile(absolutePath, hdf5ObjectPath, pseudoLinkForChecksum,
                            strategy.getStorageFeatureForPath(hdf5ObjectPath), chunkSize, pathVisitorOrNull);
            crc32 = pseudoLinkForChecksum.getCrc32();
        } else
        {
            ok = false;
            errorStrategy.dealWithError(new ArchivingException(absolutePath, new IOException(
                    "Path corresponds to neither a file nor a directory.")));
        }
        if (ok)
        {
            indexUpdater.updateIndicesOnThePath(normalizedRootArchiveInDir, absolutePath, crc32,
                    groupExists, strategy.doStoreOwnerAndPermissions());
        }
        return this;
    }

    public HDF5ArchiveUpdater archiveBelow(String rootDirInArchive, File directory,
            ArchivingStrategy strategy, int chunkSize, IPathVisitor pathVisitorOrNull)
    {
        final File absoluteDirectory = Utils.normalizePath(directory);
        if (absoluteDirectory.isDirectory())
        {
            archiveDirectory(Utils.normalizePath(rootDirInArchive), absoluteDirectory, false,
                    strategy, chunkSize, pathVisitorOrNull);
        } else
        {
            errorStrategy.dealWithError(new ArchivingException(absoluteDirectory, new IOException(
                    "Path does not correspond to a directory.")));
        }
        return this;
    }

    public HDF5ArchiveUpdater archive(File parentDirToStrip, File path, ArchivingStrategy strategy,
            int chunkSize, IPathVisitor pathVisitorOrNull)
    {
        final File absoluteParentDirToStrip = Utils.normalizePath(parentDirToStrip);
        final File absolutePath = Utils.normalizePath(path);
        final String hdf5ObjectPath = getRelativePath(absoluteParentDirToStrip, absolutePath);
        final String hdf5GroupPath = FilenameUtils.getFullPathNoEndSeparator(hdf5ObjectPath);
        final boolean groupExists =
                hdf5GroupPath.length() == 0 ? true : hdf5Writer.isGroup(hdf5GroupPath);
        final boolean ok;
        int crc32 = 0;
        final LinkRecord linkOrNull = LinkRecord.tryCreate(absolutePath, true, errorStrategy);
        if (linkOrNull != null && linkOrNull.isSymLink())
        {
            ok = archiveSymLink("", linkOrNull, absolutePath, pathVisitorOrNull);
        } else if (absolutePath.isDirectory())
        {
            ok =
                    archiveDirectory(absoluteParentDirToStrip, absolutePath, strategy, chunkSize,
                            pathVisitorOrNull);
        } else if (absolutePath.isFile())
        {
            final LinkRecord pseudoLinkForChecksum = new LinkRecord();
            ok =
                    archiveFile(absolutePath, hdf5ObjectPath, pseudoLinkForChecksum,
                            strategy.getStorageFeatureForPath(hdf5ObjectPath), chunkSize, pathVisitorOrNull);
            crc32 = pseudoLinkForChecksum.getCrc32();
        } else
        {
            ok = false;
            errorStrategy.dealWithError(new ArchivingException(absolutePath, new IOException(
                    "Path corresponds to neither a file nor a directory.")));
        }
        if (ok)
        {
            updateIndicesOnThePath(absoluteParentDirToStrip, absolutePath, crc32, groupExists,
                    strategy.doStoreOwnerAndPermissions());
        }
        return this;
    }

    private void updateIndicesOnThePath(File parentDirToStrip, File path, int crc32,
            boolean immediateGroupOnly, boolean storeOwnerAndPermissions)
    {
        final String rootAbsolute = parentDirToStrip.getAbsolutePath();
        File pathProcessing = path;
        int crc32Processing = crc32;
        while (true)
        {
            File dirProcessingOrNull = pathProcessing.getParentFile();
            String dirAbsolute =
                    (dirProcessingOrNull != null) ? dirProcessingOrNull.getAbsolutePath() : "";
            if (dirProcessingOrNull == null || dirAbsolute.startsWith(rootAbsolute) == false)
            {
                break;
            }
            final String hdf5GroupPath = getRelativePath(rootAbsolute, dirAbsolute);
            final IDirectoryIndex index = indexProvider.get(hdf5GroupPath, false);
            final LinkRecord linkOrNull =
                    LinkRecord.tryCreate(pathProcessing, storeOwnerAndPermissions, errorStrategy);
            if (linkOrNull != null)
            {
                linkOrNull.setCrc32(crc32Processing);
                crc32Processing = 0; // Directories don't have a checksum
                index.updateIndex(linkOrNull);
            }
            pathProcessing = dirProcessingOrNull;
            if (immediateGroupOnly)
            {
                break;
            }
        }
    }

    private void updateIndicesOnThePath(String path, LinkRecord link, boolean immediateGroupOnly)
    {
        String pathProcessing = path.startsWith("/") ? path : ("/" + path);
        if ("/".equals(pathProcessing))
        {
            return;
        }
        int crc32 = link.getCrc32();
        long size = link.getSize();
        long lastModified = link.getLastModified();
        short permissions = link.getPermissions();
        int uid = link.getUid();
        int gid = link.getGid();
        FileLinkType fileLinkType = link.getLinkType();
        String linkTargetOrNull = link.tryGetLinkTarget();
        while (true)
        {
            final String hdf5GroupPath =
                    Utils.getQuasiParentPath(Utils.normalizePath(pathProcessing));
            final IDirectoryIndex index = indexProvider.get(hdf5GroupPath, false);
            final String hdf5FileName = FilenameUtils.getName(pathProcessing);
            final LinkRecord linkProcessing =
                    new LinkRecord(hdf5FileName, linkTargetOrNull, fileLinkType, size,
                            lastModified, uid, gid, permissions, crc32);
            index.updateIndex(linkProcessing);
            fileLinkType = FileLinkType.DIRECTORY;
            crc32 = 0; // Directories don't have a checksum
            size = Utils.UNKNOWN; // Directories don't have a size
            linkTargetOrNull = null; // Directories don't have a link target
            pathProcessing = hdf5GroupPath;
            if (immediateGroupOnly || pathProcessing.length() == 0)
            {
                break;
            }
        }
    }

    private boolean archiveDirectory(String parentDirectory, LinkRecord link)
    {
        final String hdf5GroupPath = Utils.concatLink(parentDirectory, link.getLinkName());
        try
        {
            hdf5Writer.createGroup(hdf5GroupPath);
            return true;
        } catch (HDF5Exception ex)
        {
            errorStrategy.dealWithError(new ArchivingException(hdf5GroupPath, ex));
            return false;
        }
    }

    private boolean archiveDirectory(File parentDirToStrip, File dir, ArchivingStrategy strategy,
            int chunkSize, IPathVisitor pathVisitorOrNull)
    {
        final String rootPathInArchive = getRelativePath(parentDirToStrip, dir);
        return archiveDirectory(rootPathInArchive, dir, false, strategy, chunkSize,
                pathVisitorOrNull);
    }

    private boolean archiveDirectory(String rootDirInArchive, File dir,
            boolean includeDirNameInGroupPath, ArchivingStrategy strategy, int chunkSize,
            IPathVisitor pathVisitorOrNull)
    {
        final File[] fileEntries = dir.listFiles();
        if (fileEntries == null)
        {
            errorStrategy.dealWithError(new ArchivingException(dir, new IOException(
                    "Cannot read directory")));
            return false;
        }
        final String hdf5GroupPath =
                includeDirNameInGroupPath ? Utils.concatLink(rootDirInArchive, dir.getName())
                        : rootDirInArchive;
        if ("/".equals(hdf5GroupPath) == false)
            try
            {
                if (hdf5Writer.getFileFormat() != FileFormat.STRICTLY_1_8
                        && fileEntries.length > MIN_GROUP_MEMBER_COUNT_TO_COMPUTE_SIZEHINT)
                {
                    // Compute size hint and pre-create group in order to improve performance.
                    int totalLength = computeSizeHint(fileEntries);
                    hdf5Writer.createGroup(hdf5GroupPath, totalLength * SIZEHINT_FACTOR);
                } else
                {
                    hdf5Writer.createGroup(hdf5GroupPath);
                }
            } catch (HDF5Exception ex)
            {
                errorStrategy.dealWithError(new ArchivingException(hdf5GroupPath, ex));
            }
        final List<LinkRecord> linkEntries =
                DirectoryIndex.convertFilesToLinks(fileEntries,
                        strategy.doStoreOwnerAndPermissions(), errorStrategy);

        if (pathVisitorOrNull != null)
        {
            pathVisitorOrNull.visit(hdf5GroupPath);
        }
        final Iterator<LinkRecord> linkIt = linkEntries.iterator();
        for (int i = 0; i < fileEntries.length; ++i)
        {
            final File file = fileEntries[i];
            final LinkRecord link = linkIt.next();
            if (link == null)
            {
                linkIt.remove();
                continue;
            }
            final String absoluteEntry = file.getAbsolutePath();
            if (link.isDirectory())
            {
                if (strategy.doExclude(absoluteEntry, true))
                {
                    linkIt.remove();
                    continue;
                }
                final boolean ok =
                        archiveDirectory(hdf5GroupPath, file, true, strategy, chunkSize,
                                pathVisitorOrNull);
                if (ok == false)
                {
                    linkIt.remove();
                }
            } else
            {
                if (strategy.doExclude(absoluteEntry, false))
                {
                    linkIt.remove();
                    continue;
                }
                if (link.isSymLink())
                {
                    final boolean ok = archiveSymLink(hdf5GroupPath, link, file, pathVisitorOrNull);
                    if (ok == false)
                    {
                        linkIt.remove();
                    }
                } else if (link.isRegularFile())
                {
                    final String hdf5ObjectPath =
                            Utils.concatLink(hdf5GroupPath, file.getName());
                    final boolean ok =
                            archiveFile(file, hdf5ObjectPath, link,
                                    strategy.getStorageFeatureForPath(hdf5ObjectPath), chunkSize,
                                    pathVisitorOrNull);
                    if (ok == false)
                    {
                        linkIt.remove();
                    }
                } else
                {
                    errorStrategy.dealWithError(new ArchivingException(file, new IOException(
                            "Path corresponds to neither a file nor a directory.")));
                }
            }
        }

        final boolean verbose = (pathVisitorOrNull != null);
        final IDirectoryIndex index = indexProvider.get(hdf5GroupPath, verbose);
        index.updateIndex(linkEntries);
        return true;
    }

    private boolean archiveSymLink(String hdf5GroupPath, LinkRecord link)
    {
        if (link.tryGetLinkTarget() == null)
        {
            errorStrategy.dealWithError(new ArchivingException(link.getLinkName(), new IOException(
                    "Link target not given for symbolic link.")));
            return false;
        }
        return archiveSymLink(hdf5GroupPath, link, link.tryGetLinkTarget(), null);
    }

    private boolean archiveSymLink(String hdf5GroupPath, LinkRecord link, File file,
            IPathVisitor pathVisitorOrNull)
    {
        final String linkTarget = LinkRecord.tryReadLinkTarget(file);
        if (linkTarget == null)
        {
            errorStrategy.dealWithError(new ArchivingException(file, new IOException(
                    "Cannot read link target of symbolic link.")));
            return false;
        }
        return archiveSymLink(hdf5GroupPath, link, linkTarget, pathVisitorOrNull);
    }

    private boolean archiveSymLink(String hdf5GroupPath, LinkRecord link, String linkTarget,
            IPathVisitor pathVisitorOrNull)
    {
        try
        {
            final String hdf5LinkPath = Utils.concatLink(hdf5GroupPath, link.getLinkName());
            hdf5Writer.createSoftLink(linkTarget, hdf5LinkPath);
            if (pathVisitorOrNull != null)
            {
                pathVisitorOrNull.visit(hdf5LinkPath);
            }
        } catch (HDF5Exception ex)
        {
            errorStrategy.dealWithError(new ArchivingException(Utils.concatLink(hdf5GroupPath,
                    link.getLinkName()), ex));
            return false;
        }
        return true;

    }

    private static int computeSizeHint(final File[] entries)
    {
        int totalLength = 0;
        for (File entry : entries)
        {
            totalLength += entry.getName().length();
        }
        return totalLength;
    }

    private boolean archiveFile(File file, String hdf5ObjectPath, LinkRecord link,
            HDF5GenericStorageFeatures features, int chunkSize, IPathVisitor pathVisitorOrNull)
            throws ArchivingException
    {
        boolean ok = true;
        try
        {
            final DataSetInfo info = copyToHDF5(file, hdf5ObjectPath, features, chunkSize);
            link.setSize(info.size);
            link.setCrc32(info.crc32);
            if (pathVisitorOrNull != null)
            {
                pathVisitorOrNull.visit(hdf5ObjectPath);
            }
        } catch (IOException ex)
        {
            ok = false;
            errorStrategy.dealWithError(new ArchivingException(file, ex));
        } catch (HDF5Exception ex)
        {
            ok = false;
            errorStrategy.dealWithError(new ArchivingException(hdf5ObjectPath, ex));
        }
        return ok;
    }

    private static String getRelativePath(File root, File filePath)
    {
        return getRelativePath(root.getAbsolutePath(), filePath.getAbsolutePath());
    }

    private static String getRelativePath(String parentDirToBeStripped, String filePath)
    {
        if (filePath.startsWith(parentDirToBeStripped) == false
                && parentDirToBeStripped.startsWith(filePath) == false)
        {
            throw new IOExceptionUnchecked("Path '" + filePath + "' does not start with '"
                    + parentDirToBeStripped + "'.");
        }
        final String path =
                (parentDirToBeStripped.length() > filePath.length()) ? "" : filePath
                        .substring(parentDirToBeStripped.length());
        if (path.length() == 0)
        {
            return "/";
        } else
        {
            return FilenameUtils.separatorsToUnix(path);
        }
    }

    private DataSetInfo copyToHDF5(final File source, final String objectPath,
            final HDF5GenericStorageFeatures compression, int chunkSize) throws IOException
    {
        final InputStream input = FileUtils.openInputStream(source);
        try
        {
            return copyToHDF5(input, objectPath, compression, chunkSize);
        } finally
        {
            IOUtils.closeQuietly(input);
        }
    }

    private int getEffectiveChunkSize(int chunkSize)
    {
        return (chunkSize <= 0 || chunkSize > buffer.length) ? buffer.length : chunkSize;
    }

    private DataSetInfo copyToHDF5(final InputStream input, final String objectPath,
            final HDF5GenericStorageFeatures compression, int chunkSize) throws IOException
    {
        final int effectiveBufferLength = getEffectiveChunkSize(chunkSize);
        final CRC32 crc32 = new CRC32();
        HDF5GenericStorageFeatures features = compression;
        int n = fillBuffer(input, effectiveBufferLength);
        // Deal with small data sources separately to keep the file size smaller
        if (n < effectiveBufferLength)
        {
            // For data sets roughly up to 4096 bytes the overhead of a chunked data set outweighs
            // the saving of the compression.
            if (n <= SMALL_DATASET_LIMIT || features.isDeflating() == false)
            {
                features = HDF5GenericStorageFeatures.GENERIC_CONTIGUOUS;
            }
            final HDF5OpaqueType type =
                    hdf5Writer.createOpaqueByteArray(objectPath, OPAQUE_TAG_FILE, n, features);
            hdf5Writer.writeOpaqueByteArrayBlockWithOffset(objectPath, type, buffer, n, 0);
            crc32.update(buffer, 0, n);
            return new DataSetInfo(n, (int) crc32.getValue());
        }

        final HDF5OpaqueType type =
                hdf5Writer.createOpaqueByteArray(objectPath, OPAQUE_TAG_FILE, 0,
                        effectiveBufferLength, compression);
        long count = 0;
        while (n > 0)
        {
            hdf5Writer.writeOpaqueByteArrayBlockWithOffset(objectPath, type, buffer, n, count);
            count += n;
            crc32.update(buffer, 0, n);
            n = fillBuffer(input, effectiveBufferLength);
        }
        return new DataSetInfo(count, (int) crc32.getValue());
    }

    private int fillBuffer(InputStream input, int bufferLength) throws IOException
    {
        int ofs = 0;
        int len = bufferLength;
        int count = 0;
        int n = 0;
        while (len > 0 && -1 != (n = input.read(buffer, ofs, len)))
        {
            ofs += n;
            len -= n;
            count += n;
        }
        return count;
    }
}
