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
import java.util.List;

/**
 * @author Bernd Rinn
 */
public class HDF5ReadTest
{

    public static void main(String[] args)
    {
        HDF5Reader reader = new HDF5Reader(new File("test.h5")).open();
        System.out.println(reader.getGroupMemberPaths("/"));
        describe(reader, "/Group1/MyBitSet", null);
        describe(reader, "/Group1/MyDataSet", null);
        long[] bsl = reader.readLongArray("/Group1/MyBitSet");
        System.out.println("length of /Group1/MyBitSet=" + bsl.length);
        for (long l : bsl)
        {
            System.out.print(l + " ");
        }
        System.out.println();
        BitSet bs = reader.readBitField("/Group1/MyBitSet");
        System.out.println(bs);
        System.out.println(reader.getDoubleAttribute("/", "version"));
        List<String> members = reader.getGroupMemberPaths("/Group1");
        for (String m : members)
        {
            System.out.println("  " + m);
        }
        listAttributes(reader, "/Group1");
        listAttributes(reader, "/Group1/MyDataSet");
        describe(reader, "/Group1/MyDataSet", "foo");
        describe(reader, "/Group1", "active");
        System.out.println(reader.getBooleanAttribute("/Group1", "active"));
        System.out.println(reader.getStringAttribute("/Group1/MyDataSet", "foo"));
        System.out.println(reader.getStringAttribute("/Group1/SubGroup1/MyDataSet", "foo"));
        System.out.println(reader.readDoubleMatrix("/Group1/MyDataSet")[1][0]);
        System.out.println(reader.readFloatMatrix("/Group1/SubGroup1/MyDataSet")[1][2]);
        System.out.println(reader.readString("/Group1/MyString").length());
        listAttributes(reader, "empty");
    }

    private static void listAttributes(HDF5Reader reader, String objectName)
    {
        final List<String> attributeNames = reader.getAttributeNames(objectName);
        System.out.printf("Found %d attributes for object '%s':\n", attributeNames.size(),
                objectName);
        for (String a : attributeNames)
        {
            System.out.println(a);
        }
    }

    private static void describe(HDF5Reader reader, String objectName, String attributeNameOrNull)
    {
        HDF5DataSetInformation dsInfo;
        HDF5DataTypeInformation dtInfo;
        if (attributeNameOrNull == null)
        {
            dsInfo = reader.getDataSetInformation(objectName);
            dtInfo = dsInfo.getTypeInformation();
        } else
        {
            dsInfo = null;
            dtInfo = reader.getAttributeInformation(objectName, attributeNameOrNull);
        }
        System.out.printf("%s%s, class=%s, elemSize=%d", objectName,
                attributeNameOrNull != null ? "#" + attributeNameOrNull : "",
                dtInfo.getDataClass(), dtInfo.getElementSize());
        if (dsInfo != null)
        {
            System.out.printf(", rank=%d, scalar=%s, variant=%s\n", dsInfo.getRank(), Boolean
                    .toString(dsInfo.isScalar()), dsInfo.tryGetTypeVariant());
            for (long dim : dsInfo.getDimensions())
            {
                System.out.println("  DIM " + dim);
            }
        } else
        {
            System.out.println();
        }
    }
}
