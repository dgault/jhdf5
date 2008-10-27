/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.hdf5;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.time.StopWatch;

import ch.systemsx.cisd.hdf5.tools.HDF5Archiver;

/**
 * @author Bernd Rinn
 */
public class HDF52Dir
{

    public static void main(String[] args) throws IOException
    {
        if (args.length != 1 && args.length != 2 && args.length != 3)
        {
            System.err.println("Syntax: HDF52Dir <hdf5 file> [<path in file>] [<root>]");
            System.exit(1);
        }
        final File hdf5File = new File(args[0]);
        final String pathInFile = (args.length > 1) ? args[1] : "/";
        final File rootDir = new File((args.length > 2) ? args[2] : ".");
        if (rootDir.isDirectory() == false)
        {
            System.err.println("Path '" + rootDir + "' is not a directory.");
            System.exit(1);
        }
        final StopWatch watch = new StopWatch();
        watch.start();
        final HDF5Archiver archiver = new HDF5Archiver(hdf5File, true, false, false);
        archiver.extract(rootDir, pathInFile, false, true);
        archiver.close();
        watch.stop();
        System.out.println("Extracting hdf5 file took " + watch);
    }

}
