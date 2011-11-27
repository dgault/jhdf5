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

package ch.systemsx.cisd.hdf5.tools;

import java.io.File;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.unix.FileLinkType;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.base.unix.Unix.Stat;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;
import ch.systemsx.cisd.hdf5.HDF5LinkInformation;
import ch.systemsx.cisd.hdf5.HDF5ObjectType;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * A class containing all information we need have about a link either in the file system or in an
 * HDF5 container.
 * 
 * @author Bernd Rinn
 */
public final class Link implements Comparable<Link>
{
    final static int UNKNOWN = -1;

    final static short UNKNOWN_S = -1;

    public enum Completeness
    {
        BASE, LAST_MODIFIED, FULL
    }

    private int linkNameLength;

    private String linkName;

    private String linkTargetOrNull;

    private HDF5EnumerationValue hdf5EncodedLinkType;

    private FileLinkType linkType;

    private long size;

    private long lastModified;

    private int uid;

    private int gid;

    private short permissions;

    private int crc32;

    /**
     * Returns a {@link Link} object for the given <var>link</var> {@link File}, or
     * <code>null</code> if a system call fails and <var>continueOnError</var> is <code>true</code>.
     */
    public static Link tryCreate(File file, boolean includeOwnerAndPermissions,
            IErrorStrategy errorStrategy)
    {
        try
        {
            return new Link(file, includeOwnerAndPermissions);
        } catch (IOExceptionUnchecked ex)
        {
            errorStrategy.dealWithError(new ArchivingException(file, ex.getCause()));
            return null;
        }
    }

    /**
     * Returns a {@link Link} object for the given <var>hdf5FileName</var>.
     */
    public static Link createRegularFile(String hdf5FileName, long size)
    {
        return new Link(hdf5FileName, FileLinkType.REGULAR_FILE, size, 0);
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

    private static int getCurrentUid()
    {
        if (Unix.isOperational())
        {
            return Unix.getUid();
        } else
        {
            return UNKNOWN;
        }
    }
    
    private static int getCurrentGid()
    {
        if (Unix.isOperational())
        {
            return Unix.getGid();
        } else
        {
            return UNKNOWN;
        }
    }
    
    /**
     * Used by the HDF5 library during reading.
     */
    public Link()
    {
    }

    /**
     * Used for HDF5 files which are created on the fly.
     */
    public Link(String hdf5FileName, FileLinkType type, long size, int crc32)
    {
        this.linkName = hdf5FileName;
        this.linkTargetOrNull = null;
        this.linkType = type;
        this.size = size;
        this.crc32 = crc32;
        this.lastModified = System.currentTimeMillis() / 1000;
        this.uid = getCurrentUid();
        this.gid = getCurrentGid();
        this.permissions = 0755;
    }

    public Link(HDF5LinkInformation info, long size)
    {
        this.linkName = info.getName();
        this.linkTargetOrNull = info.tryGetSymbolicLinkTarget();
        this.linkType = translateType(info.getType());
        this.size = size;
        this.lastModified = UNKNOWN;
        this.uid = UNKNOWN;
        this.gid = UNKNOWN;
        this.permissions = UNKNOWN_S;
    }

    /**
     * Returns a {@link Link} object for the given <var>link</var> {@link File}.
     */
    private Link(File link, boolean includeOwnerAndPermissions)
    {
        this.linkName = link.getName();
        if (includeOwnerAndPermissions && Unix.isOperational())
        {
            final Stat info = Unix.getLinkInfo(link.getPath(), false);
            this.linkType = info.getLinkType();
            this.size = info.getSize();
            this.lastModified = info.getLastModified();
            this.uid = info.getUid();
            this.gid = info.getGid();
            this.permissions = info.getPermissions();
        } else
        {
            this.linkType =
                    (link.isDirectory()) ? FileLinkType.DIRECTORY
                            : (link.isFile() ? FileLinkType.REGULAR_FILE : FileLinkType.OTHER);
            this.size = link.length();
            this.lastModified = link.lastModified() / 1000;
            this.uid = UNKNOWN;
            this.gid = UNKNOWN;
            this.permissions = UNKNOWN_S;
        }
        if (linkType == FileLinkType.SYMLINK)
        {
            this.linkTargetOrNull = tryReadLinkTarget(link);
        }
    }

    Link(String linkName, String linkTargetOrNull, FileLinkType linkType, long size,
            long lastModified, int uid, int gid, short permissions)
    {
        this.linkName = linkName;
        this.linkTargetOrNull = linkTargetOrNull;
        this.linkType = linkType;
        this.size = size;
        this.lastModified = lastModified;
        this.uid = uid;
        this.gid = gid;
        this.permissions = permissions;
    }

    /** For unit tests only! */
    @Private
    Link(String linkName, String linkTargetOrNull, FileLinkType linkType, long size,
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

    private static FileLinkType translateType(final HDF5ObjectType hdf5Type)
    {
        switch (hdf5Type)
        {
            case DATASET:
                return FileLinkType.REGULAR_FILE;
            case GROUP:
                return FileLinkType.DIRECTORY;
            case SOFT_LINK:
                return FileLinkType.SYMLINK;
            default:
                return FileLinkType.OTHER;
        }
    }

    /**
     * Call this method after reading the link from the archive and before using it.
     */
    int initAfterReading(String concatenatedNames, int startPos, IHDF5Reader reader,
            String groupPath, boolean readLinkTarget)
    {
        try
        {
            this.linkType = FileLinkType.valueOf(hdf5EncodedLinkType.getValue());
        } catch (Exception ex)
        {
            this.linkType = FileLinkType.OTHER;
        }
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
     * Call this method before writing the link to the archive.
     */
    void prepareForWriting(HDF5EnumerationType hdf5LinkTypeEnumeration,
            StringBuilder concatenatedNames)
    {
        this.linkNameLength = this.linkName.length();
        concatenatedNames.append(linkName);
        if (this.hdf5EncodedLinkType == null)
        {
            this.hdf5EncodedLinkType =
                    new HDF5EnumerationValue(hdf5LinkTypeEnumeration, linkType.name());
        }
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

    public Completeness getCompleteness()
    {
        if (hasUnixPermissions())
        {
            return Completeness.FULL;
        } else if (hasLastModified())
        {
            return Completeness.LAST_MODIFIED;
        } else
        {
            return Completeness.BASE;
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

    //
    // Comparable
    //

    public int compareTo(Link o)
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
        if (obj == null || obj instanceof Link == false)
        {
            return false;
        }
        final Link that = (Link) obj;
        return this.linkName.equals(that.linkName);
    }

    @Override
    public int hashCode()
    {
        return linkName.hashCode();
    }
}
