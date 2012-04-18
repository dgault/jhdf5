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

/**
 * A class that represents parameters for {@link HDF5Archiver#list(String, ListParameters)}.
 * 
 * @author Bernd Rinn
 */
public final class ListParameters
{
    private final boolean recursive;

    private final boolean readLinkTargets;

    private final boolean testArchive;

    private final boolean suppressDirectoryEntries;

    private final boolean resolveSymbolicLinks;

    public static final ListParameters DEFAULT =
            new ListParameters(true, true, false, false, false);

    public static final ListParameters TEST = new ListParameters(true, true, false, false, false);

    public static final class ListParametersBuilder
    {
        private boolean recursive = true;

        private boolean readLinkTargets = true;

        private boolean testArchive = false;

        private boolean suppressDirectoryEntries = false;

        private boolean resolveSymbolicLinks = false;

        private ListParametersBuilder()
        {
        }

        public ListParametersBuilder nonRecursive()
        {
            this.recursive = false;
            return this;
        }

        public ListParametersBuilder recursive(@SuppressWarnings("hiding")
        boolean recursive)
        {
            this.recursive = recursive;
            return this;
        }

        public ListParametersBuilder noReadLinkTarget()
        {
            this.readLinkTargets = false;
            return this;
        }

        public ListParametersBuilder readLinkTargets(@SuppressWarnings("hiding")
        boolean readLinkTargets)
        {
            this.readLinkTargets = readLinkTargets;
            return this;
        }

        public ListParametersBuilder testArchive()
        {
            this.testArchive = true;
            return this;
        }

        public ListParametersBuilder testArchive(@SuppressWarnings("hiding")
        boolean testArchive)
        {
            this.testArchive = testArchive;
            return this;
        }

        public ListParametersBuilder suppressDirectoryEntries()
        {
            this.suppressDirectoryEntries = true;
            return this;
        }

        public ListParametersBuilder suppressDirectoryEntries(@SuppressWarnings("hiding")
        boolean suppressDirectoryEntries)
        {
            this.suppressDirectoryEntries = suppressDirectoryEntries;
            return this;
        }

        /**
         * Resolve symbolic links to their link targets.
         * <p>
         * This makes symbolic links kind of appear like hard links in the listing. Note, however,
         * that symbolic links to directories being resolved do not lead to the directory content
         * being listed as this could lead to infinite loops.
         */
        public ListParametersBuilder resolveSymbolicLinks()
        {
            this.resolveSymbolicLinks = true;
            return this;
        }

        public ListParametersBuilder resolveSymbolicLinks(@SuppressWarnings("hiding")
        boolean resolveSymbolicLinks)
        {
            this.resolveSymbolicLinks = resolveSymbolicLinks;
            return this;
        }

        public ListParameters get()
        {
            return new ListParameters(recursive, readLinkTargets, testArchive,
                    suppressDirectoryEntries, resolveSymbolicLinks);
        }
    }

    public static ListParametersBuilder build()
    {
        return new ListParametersBuilder();
    }

    private ListParameters(boolean recursive, boolean readLinkTargets, boolean testArchive,
            boolean suppressDirectoryEntries, boolean resolveSymbolicLinks)
    {
        this.recursive = recursive;
        this.readLinkTargets = readLinkTargets || resolveSymbolicLinks;
        this.testArchive = testArchive;
        this.suppressDirectoryEntries = suppressDirectoryEntries;
        this.resolveSymbolicLinks = resolveSymbolicLinks;
    }

    public boolean isRecursive()
    {
        return recursive;
    }

    public boolean isReadLinkTargets()
    {
        return readLinkTargets;
    }

    public boolean isTestArchive()
    {
        return testArchive;
    }

    public boolean isSuppressDirectoryEntries()
    {
        return suppressDirectoryEntries;
    }

    public boolean isResolveSymbolicLinks()
    {
        return resolveSymbolicLinks;
    }
}
