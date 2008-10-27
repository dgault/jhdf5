/*
 * Copyright 2007 ETH Zuerich, CISD.
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
import java.util.BitSet;

import ch.systemsx.cisd.hdf5.HDF5Writer;

/**
 * @author Bernd Rinn
 */
public class HDF5WriteTest
{

    public static void main(String[] args)
    {
        final BitSet bs = new BitSet();
        bs.set(127);
        bs.set(64);
        bs.set(128);
        // bs.set(191);
        HDF5Writer writer = new HDF5Writer(new File("test.h5")).overwrite().open();
        // writer.write("/Group1/SubGroup1/MyDataSet", new float[] { 1.0f, 2.0f, 3.0f, 4.0f });
        // writer.link("/Group1/SubGroup1/MyDataSet", "/Group1/MyDataSet");
        // writer.write("/Group1/MyDataSet", new float[] { 4.0f, 3.0f, 2.0f, 1.0f });
        // writer.write("/Group1/MyDataSet", new double[] { 4.0, 3.0, 2.0, 1.0 });
        writer.writeBitField("/Group1/MyBitSet", bs);
        writer.writeFloatMatrix("/Group1/MyDataSet", new float[][]
            {
                { 4, 3, 2, 1, 0, -1 },
                { 0, 1, 2, 3, 4, 5 } });
        writer.writeLongArray("/Group1/MyDataSet2", new long[]
            { 4, 3, 2, 1 });
        writer.writeLongArray("/Group1/MyDataSet3", new long[]
            { 1 });
        // writer.write("/Group1/MyDataSet", new int[] { 4, 3, 2, 1 });
        writer.createHardLink("/Group1/MyDataSet", "/Group1/SubGroup1/MyDataSet");
        writer.writeString("/Group1/MyString", "Und schon wieder die Geschichte vom Pferd!");
        writer.addStringAttribute("/Group1/MyDataSet", "foo", "Die Geschichte vom Pferd");
        // writer.addAttribute("/Group1/SubGroup1/MyDataSet", "foo", "No story");
        writer.addDoubleAttribute("/", "version", 17.0);
        writer.addBooleanAttribute("/Group1", "active", true);
        writer.writeByteArray("/empty", new byte[0]);
        writer.close();
    }

}
