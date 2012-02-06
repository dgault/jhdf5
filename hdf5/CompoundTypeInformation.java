/*
 * Copyright 2012 ETH Zuerich, CISD
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

/**
 * A class with basic information about the types of compound members in an HDF5 file.
 * 
 * @author Bernd Rinn
 */
final class CompoundTypeInformation
{
    final String name;

    final int compoundDataTypeId;

    final int nativeCompoundDataTypeId;

    final HDF5CompoundMemberInformation[] members;

    final int[] dataTypeIds;

    final int recordSize;

    CompoundTypeInformation(String name, int compoundDataTypeId, int nativeCompoundDataTypeId,
            int numberOfElements, int recordSize)
    {
        this.name = name;
        this.compoundDataTypeId = compoundDataTypeId;
        this.nativeCompoundDataTypeId = nativeCompoundDataTypeId;
        this.members = new HDF5CompoundMemberInformation[numberOfElements];
        this.dataTypeIds = new int[numberOfElements];
        this.recordSize = recordSize;
    }
}