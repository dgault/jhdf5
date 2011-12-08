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

import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * A strategy role for dealing with errors in the HDF5 archiver.
 * 
 * @author Bernd Rinn
 */
public interface IErrorStrategy
{
    /**
     * The default error strategy, just re-throws the exception.
     */
    public static final IErrorStrategy DEFAULT_ERROR_STRATEGY = new IErrorStrategy()
        {
            public void dealWithError(ArchiverException ex) throws ArchiverException
            {
                throw ex;
            }

            public void warning(String message)
            {
                System.err.println(message);
            }
        };

    /**
     * An error strategy that prints out the HDF5 error stack on an {@link HDF5LibraryException},
     * otherwise the same as {@link #DEFAULT_ERROR_STRATEGY}.
     */
    public static final IErrorStrategy DEBUG_ERROR_STRATEGY = new IErrorStrategy()
        {
            public void dealWithError(ArchiverException ex) throws ArchiverException
            {
                if (ex.getCause() instanceof HDF5LibraryException)
                {
                    System.err.println(((HDF5LibraryException) ex.getCause())
                            .getHDF5ErrorStackAsString());
                }
                throw ex;
            }

            public void warning(String message)
            {
                System.err.println(message);
            }
        };

    /**
     * Called when an exception <var>ex</var> has occurred. Can, but doesn't have to, abort the
     * operation by re-throwing the exception.
     * 
     * @throws ArchiverException if the operation should be aborted
     */
    public void dealWithError(final ArchiverException ex) throws ArchiverException;

    /**
     * Called to issue a warning message.
     */
    public void warning(String message);
}
