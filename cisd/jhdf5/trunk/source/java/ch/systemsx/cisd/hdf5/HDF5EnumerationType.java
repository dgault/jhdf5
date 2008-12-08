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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ncsa.hdf.hdf5lib.HDFNativeData;

/**
 * A class the represents an enumeration for a given HDF5 file and <var>values</var> array.
 * 
 * @author Bernd Rinn
 */
public final class HDF5EnumerationType extends HDF5DataType implements Iterable<String>
{
    private final String nameOrNull;

    private final String[] values;

    private Map<String, Integer> nameToIndexMap;

    HDF5EnumerationType(int fileId, int storageTypeId, int nativeTypeId, String nameOrNull,
            String[] values)
    {
        super(fileId, storageTypeId, nativeTypeId);

        assert values != null;

        this.nameOrNull = nameOrNull;
        this.values = values;
    }

    private Map<String, Integer> getMap()
    {
        if (nameToIndexMap == null)
        {
            nameToIndexMap = new HashMap<String, Integer>(values.length);
            for (int i = 0; i < values.length; ++i)
            {
                nameToIndexMap.put(values[i], i);
            }
        }
        return nameToIndexMap;
    }

    String[] getValueArray()
    {
        return values;
    }

    Object createArray(int length)
    {
        if (values.length < Byte.MAX_VALUE)
        {
            return new byte[length];
        } else if (values.length < Short.MAX_VALUE)
        {
            return new short[length];
        } else
        {
            return new int[length];
        }
    }

    /**
     * Returns the ordinal value for the given string <var>value</var>, if <var>value</var> is a
     * member of the enumeration, and <code>null</code> otherwise.
     */
    public Integer tryGetIndexForValue(String value)
    {
        return getMap().get(value);
    }

    /**
     * Returns the name of this type, if it exists and <code>null</code> otherwise.
     */
    public String tryGetName()
    {
        return nameOrNull;
    }

    /**
     * Returns a name for this type, if it is it known and <code>UNKNOWN</code> otherwise.
     */
    @Override
    public String getName()
    {
        return (nameOrNull == null) ? "UNKNOWN" : nameOrNull;
    }

    /**
     * Returns the allowed values of this enumeration type.
     */
    public List<String> getValues()
    {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    byte getStorageSize()
    {
        final int len = values.length;
        if (len < Byte.MAX_VALUE)
        {
            return 1;
        } else if (len < Short.MAX_VALUE)
        {
            return 2;
        } else
        {
            return 4;
        }
    }

    HDF5EnumerationValue createFromStorageForm(byte[] data, int offset)
    {
        switch (getStorageSize())
        {
            case 1:
                return new HDF5EnumerationValue(this, data[offset]);
            case 2:
                return new HDF5EnumerationValue(this, HDFNativeData.byteToShort(data, offset));
            case 4:
                return new HDF5EnumerationValue(this, HDFNativeData.byteToInt(data, offset));
        }
        throw new Error("Illegal storage size.");
    }

    //
    // Iterable
    //

    /**
     * Returns an {@link Iterator} over all values of this enumeration type.
     * {@link Iterator#remove()} is not allowed and will throw an
     * {@link UnsupportedOperationException}.
     */
    public Iterator<String> iterator()
    {
        return new Iterator<String>()
            {
                private int index = 0;

                public boolean hasNext()
                {
                    return index < values.length;
                }

                public String next()
                {
                    return values[index++];
                }

                /**
                 * @throws UnsupportedOperationException As this iterator doesn't support removal.
                 */
                public void remove() throws UnsupportedOperationException
                {
                    throw new UnsupportedOperationException();
                }

            };
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final HDF5EnumerationType other = (HDF5EnumerationType) obj;
        return Arrays.equals(values, other.values);
    }

}
