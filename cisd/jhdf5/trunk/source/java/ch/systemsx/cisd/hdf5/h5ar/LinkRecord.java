/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.base.exceptions.IErrorStrategy;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.unix.FileLinkType;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.base.unix.Unix.Stat;
import ch.systemsx.cisd.hdf5.CompoundElement;
import ch.systemsx.cisd.hdf5.CompoundType;
import ch.systemsx.cisd.hdf5.HDF5LinkInformation;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * A class containing all information we need to have about a link either in the file system or in
 * an HDF5 container.
 * 
 * @author Bernd Rinn
 */
@CompoundType(name = "Link", mapAllFields = false)
final class LinkRecord implements Comparable<LinkRecord>
{
    @CompoundElement(memberName = "linkNameLength")
    private int linkNameLength;

    @CompoundElement(memberName = "linkType", typeName = "linkType")
    private FileLinkType linkType;

    @CompoundElement(memberName = "size")
    private long size;

    @CompoundElement(memberName = "lastModified")
    private long lastModified;

    @CompoundElement(memberName = "uid")
    private int uid;

    @CompoundElement(memberName = "gid")
    private int gid;

    @CompoundElement(memberName = "permissions")
    private short permissions;

    @CompoundElement(memberName = "checksum")
    private int crc32;

    private String linkName;

    private String linkTargetOrNull;

    private FileLinkType verifiedType;

    private long verifiedSize = -1;

    private int verifiedCrc32 = 0;

    /**
     * Returns a {@link LinkRecord} object for the given <var>link</var> {@link File}, or
     * <code>null</code> if a system call fails and <var>continueOnError</var> is <code>true</code>.
     */
    public static LinkRecord tryCreate(File file, boolean includeOwnerAndPermissions,
            IErrorStrategy errorStrategy)
    {
        try
        {
            return new LinkRecord(file, includeOwnerAndPermissions);
        } catch (IOExceptionUnchecked ex)
        {
            errorStrategy.dealWithError(new ArchivingException(file, ex.getCause()));
            return null;
        }
    }

    /**
     * Returns the link target of <var>symbolicLink</var>, or <code>null</code>, if
     * <var>symbolicLink</var> is not a symbolic link or the link target could not be read.
     */
    public static String tryReadLinkTarget(File symbolicLink)
    {
        if (Unix.isOperational())
        {
            return Unix.tryReadSymbolicLink(symbolicLink.getPath());
        } else
        {
            return null;
        }
    }

    /**
     * Used by the HDF5 library during reading.
     */
    LinkRecord()
    {
    }

    /**
     * A link for user-created {@Link NewArchiveEntry}.
     */
    LinkRecord(NewArchiveEntry entry)
    {
        this(entry.getName(), entry.getLinkTarget(), entry.getLinkType(), Utils.UNKNOWN, entry
                .getLastModified(), entry.getUid(), entry.getGid(), entry.getPermissions(),
                Utils.UNKNOWN);
    }

    /**
     * Creates a link record for a new directory entry.
     */
    LinkRecord(String hdf5DirectoryPath)
    {
        this(hdf5DirectoryPath, System.currentTimeMillis() / 1000, Utils.getCurrentUid(), Utils
                .getCurrentGid(), (short) 0755);
    }

    /**
     * Creates the root directory entry from the stat record of the HDF5 archive.
     */
    LinkRecord(Stat hdf5Archive)
    {
        this("", hdf5Archive.getLastModified(), hdf5Archive.getUid(), hdf5Archive.getGid(),
                hdf5Archive.getPermissions());
    }

    /**
     * Creates the root directory entry from the File of the HDF5 archive.
     */
    LinkRecord(File hdf5Archive)
    {
        this("", hdf5Archive.lastModified() / 1000, Utils.getCurrentUid(), Utils
                .getCurrentGid(), Utils.UNKNOWN_S);
    }

    /**
     * Creates a directory entry.
     */
    LinkRecord(String hdf5DirectoryPath, long lastModified, int uid, int gid, short permissions)
    {
        this.linkName = hdf5DirectoryPath;
        this.linkTargetOrNull = null;
        this.linkType = FileLinkType.DIRECTORY;
        this.lastModified = lastModified;
        this.uid = uid;
        this.gid = gid;
        this.permissions = permissions;
    }

    /**
     * Used by {@link DirectoryIndex}.
     */
    LinkRecord(HDF5LinkInformation info, long size)
    {
        this.linkName = info.getName();
        this.linkTargetOrNull = info.tryGetSymbolicLinkTarget();
        this.linkType = Utils.translateType(info.getType());
        this.size = size;
        this.lastModified = Utils.UNKNOWN;
        this.uid = Utils.UNKNOWN;
        this.gid = Utils.UNKNOWN;
        this.permissions = Utils.UNKNOWN_S;
    }

    /**
     * Returns a {@link LinkRecord} object for the given <var>link</var> {@link File}.
     */
    private LinkRecord(File file, boolean includeOwnerAndPermissions)
    {
        this.linkName = file.getName();
        if (includeOwnerAndPermissions && Unix.isOperational())
        {
            final Stat info = Unix.getLinkInfo(file.getPath(), false);
            this.linkType = info.getLinkType();
            this.size = (linkType == FileLinkType.REGULAR_FILE) ? info.getSize() : 0;
            this.lastModified = info.getLastModified();
            this.uid = info.getUid();
            this.gid = info.getGid();
            this.permissions = info.getPermissions();
        } else
        {
            this.linkType =
                    (file.isDirectory()) ? FileLinkType.DIRECTORY
                            : (file.isFile() ? FileLinkType.REGULAR_FILE : FileLinkType.OTHER);
            this.size = (linkType == FileLinkType.REGULAR_FILE) ? file.length() : 0;
            this.lastModified = file.lastModified() / 1000;
            this.uid = Utils.UNKNOWN;
            this.gid = Utils.UNKNOWN;
            this.permissions = Utils.UNKNOWN_S;
        }
        if (linkType == FileLinkType.SYMLINK)
        {
            this.linkTargetOrNull = tryReadLinkTarget(file);
        }
    }

    LinkRecord(String linkName, String linkTargetOrNull, FileLinkType linkType, long size,
            long lastModified, int uid, int gid, short permissions, int crc32)
    {
        this.linkName = linkName;
        this.linkTargetOrNull = linkTargetOrNull;
        this.linkType = linkType;
        this.size = size;
        this.lastModified = lastModified;
        this.uid = uid;
        this.gid = gid;
        this.permissions = permissions;
        this.crc32 = crc32;
    }

    /**
     * Call this method after reading the link from the archive and before using it.
     */
    int initAfterReading(String concatenatedNames, int startPos, IHDF5Reader reader,
            String groupPath, boolean readLinkTarget)
    {
        final int endPos = startPos + linkNameLength;
        this.linkName = concatenatedNames.substring(startPos, endPos);
        if (readLinkTarget && linkType == FileLinkType.SYMLINK)
        {
            this.linkTargetOrNull =
                    reader.getLinkInformation(groupPath + "/" + linkName)
                            .tryGetSymbolicLinkTarget();
        }
        return endPos;
    }

    /**
     * Call this method to read additionally the link target of a symlink.
     */
    void addLinkTarget(IHDF5Reader reader, String groupPath)
    {
        if (linkType == FileLinkType.SYMLINK && linkTargetOrNull == null)
        {
            this.linkTargetOrNull =
                    reader.getLinkInformation(groupPath + "/" + linkName)
                            .tryGetSymbolicLinkTarget();
        }
    }

    /**
     * Call this method before writing the link to the archive.
     */
    void prepareForWriting(StringBuilder concatenatedNames)
    {
        this.linkNameLength = this.linkName.length();
        concatenatedNames.append(linkName);
    }

    public String getLinkName()
    {
        return linkName;
    }

    public String tryGetLinkTarget()
    {
        return linkTargetOrNull;
    }

    public boolean isDirectory()
    {
        return linkType == FileLinkType.DIRECTORY;
    }

    public boolean isSymLink()
    {
        return linkType == FileLinkType.SYMLINK;
    }

    public boolean isRegularFile()
    {
        return linkType == FileLinkType.REGULAR_FILE;
    }

    public FileLinkType getLinkType()
    {
        return linkType;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public long getSize()
    {
        return size;
    }

    public boolean hasLastModified()
    {
        return lastModified >= 0;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public boolean hasUnixPermissions()
    {
        return uid >= 0 && gid >= 0 && permissions >= 0;
    }

    public int getUid()
    {
        return uid;
    }

    public int getGid()
    {
        return gid;
    }

    public short getPermissions()
    {
        return permissions;
    }

    public ArchiveEntryCompleteness getCompleteness()
    {
        if (hasUnixPermissions())
        {
            return ArchiveEntryCompleteness.FULL;
        } else if (hasLastModified())
        {
            return ArchiveEntryCompleteness.LAST_MODIFIED;
        } else
        {
            return ArchiveEntryCompleteness.BASE;
        }
    }

    public int getCrc32()
    {
        return crc32;
    }

    public void setCrc32(int crc32)
    {
        this.crc32 = crc32;
    }

    public FileLinkType getVerifiedType()
    {
        return verifiedType;
    }

    public void setVerifiedType(FileLinkType verifiedType)
    {
        this.verifiedType = verifiedType;
    }

    public int getVerifiedCrc32()
    {
        return verifiedCrc32;
    }

    public long getVerifiedSize()
    {
        return verifiedSize;
    }

    public void setFileVerification(long size, int crc32)
    {
        this.verifiedSize = size;
        this.verifiedCrc32 = crc32;
    }

    public void resetVerification()
    {
        verifiedType = null;
        verifiedSize = -1;
        verifiedCrc32 = 0;
    }

    //
    // Comparable
    //

    public int compareTo(LinkRecord o)
    {
        // We put all directories before all files.
        if (isDirectory() && o.isDirectory() == false)
        {
            return -1;
        } else if (isDirectory() == false && o.isDirectory())
        {
            return 1;
        } else
        {
            return getLinkName().compareTo(o.getLinkName());
        }
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof LinkRecord == false)
        {
            return false;
        }
        final LinkRecord that = (LinkRecord) obj;
        return this.linkName.equals(that.linkName);
    }

    @Override
    public int hashCode()
    {
        return linkName.hashCode();
    }

    @Override
    public String toString()
    {
        return "LinkRecord [linkName=" + linkName + ", linkType=" + linkType + ", size=" + size
                + ", lastModified=" + lastModified + ", uid=" + uid + ", gid=" + gid
                + ", permissions=" + permissions + ", crc32=" + crc32 + ", linkTargetOrNull="
                + linkTargetOrNull + "]";
    }
}
