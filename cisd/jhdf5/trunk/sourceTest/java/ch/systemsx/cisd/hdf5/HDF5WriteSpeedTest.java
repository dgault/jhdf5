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

package ch.systemsx.cisd.hdf5;

import java.io.File;

/**
 * 
 *
 * @author Bernd Rinn
 */
public class HDF5WriteSpeedTest
{

    public static void main(String[] args)
    {
        final long start = System.currentTimeMillis();
        final HDF5Writer writer = new HDF5Writer(new File("speedtest.h5"));
        final byte[] arr = new byte[100];
        for (int i = 0; i < 10000; ++i)
        {
            writer.writeByteArray("/" + i, arr);
        }
        writer.close();
        System.out.println("Took: " + (System.currentTimeMillis() - start) / 1000.f + " s");
    }

}
