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

import org.apache.commons.lang.StringUtils;

/**
 * A code generator for the identical parts of the HDF5 Java classes class for different numerical
 * types.
 * 
 * @author Bernd Rinn
 */
public class HDF5CodeGenerator
{

    static class TemplateParameters
    {
        final String name;

        final String capitalizedName;

        final String wrapperName;

        final String storageType;

        final String memoryType;

        TemplateParameters(String name, String capitalizedName, String wrapperName,
                String storageType, String memoryType)
        {
            this.name = name;
            this.capitalizedName = capitalizedName;
            this.wrapperName = wrapperName;
            this.storageType = storageType;
            this.memoryType = memoryType;
        }

    }

    static TemplateParameters tp(String name, String capitalizedName, String wrapperName,
            String storageType, String memoryType)
    {
        return new TemplateParameters(name, capitalizedName, wrapperName, storageType, memoryType);
    }

    static TemplateParameters tp(String name, String wrapperName, String storageType,
            String memoryType)
    {
        return new TemplateParameters(name, StringUtils.capitalize(name), wrapperName, storageType,
                memoryType);
    }

    static TemplateParameters tp(String name, String storageType, String memoryType)
    {
        return new TemplateParameters(name, StringUtils.capitalize(name), StringUtils
                .capitalize(name), storageType, memoryType);
    }

    static final TemplateParameters PLACEHOLDERS =
            tp("__name__", "__Name__", "__Wrappername__", "__Storagetype__", "__Memorytype__");

    static final TemplateParameters[] NUMERICAL_TYPES =
            new TemplateParameters[]
                { tp("byte", "H5T_STD_I8LE", "H5T_NATIVE_INT8"),
                        tp("short", "H5T_STD_I16LE", "H5T_NATIVE_INT16"),
                        tp("int", "Integer", "H5T_STD_I32LE", "H5T_NATIVE_INT32"),
                        tp("long", "H5T_STD_I64LE", "H5T_NATIVE_INT64"),
                        tp("float", "H5T_IEEE_F32LE", "H5T_NATIVE_FLOAT"),
                        tp("double", "H5T_IEEE_F64LE", "H5T_NATIVE_DOUBLE") };

    /**
     * Generate the code for all numerical types from <var>codeTemplate</var> and write it to
     * <code>stdout</code>.
     */
    static void generateCode(final String codeTemplate)
    {
        for (TemplateParameters t : NUMERICAL_TYPES)
        {
            String s = codeTemplate;
            s = StringUtils.replace(s, PLACEHOLDERS.name, t.name);
            s = StringUtils.replace(s, PLACEHOLDERS.capitalizedName, t.capitalizedName);
            s = StringUtils.replace(s, PLACEHOLDERS.wrapperName, t.wrapperName);
            s = StringUtils.replace(s, PLACEHOLDERS.storageType, t.storageType);
            s = StringUtils.replace(s, PLACEHOLDERS.memoryType, t.memoryType);
            System.out.println(s);
        }
    }

}
