/*
 * Copyright 2010 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.hdf5.hdf5lib.H5D.H5Dwrite;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5P_DEFAULT;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5S_ALL;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_NATIVE_INT64;
import static ch.systemsx.cisd.hdf5.hdf5lib.HDF5Constants.H5T_STD_I64LE;

import java.util.Date;

import ch.systemsx.cisd.hdf5.cleanup.ICallableWithCleanUp;
import ch.systemsx.cisd.hdf5.cleanup.ICleanUpRegistry;
import ch.systemsx.cisd.hdf5.hdf5lib.HDFNativeData;

/**
 * Implementation of {@link IHDF5DateTimeWriter}.
 * 
 * @author Bernd Rinn
 */
public class HDF5DateTimeWriter implements IHDF5DateTimeWriter
{

    private final HDF5BaseWriter baseWriter;

    HDF5DateTimeWriter(HDF5BaseWriter baseWriter)
    {
        assert baseWriter != null;

        this.baseWriter = baseWriter;
    }

    //
    // Date
    //

    public void setTimeStampAttribute(String objectPath, String name, long timeStamp)
    {
        assert objectPath != null;
        assert name != null;

        baseWriter.checkOpen();
        baseWriter.setAttribute(objectPath, name,
                HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH, H5T_STD_I64LE,
                H5T_NATIVE_INT64, new long[]
                    { timeStamp });
    }

    public void setDateAttribute(String objectPath, String name, Date date)
    {
        setTimeStampAttribute(objectPath, name, date.getTime());
    }

    public void setDateArrayAttribute(String objectPath, String name, Date[] dates)
    {
        setTimeStampArrayAttribute(objectPath, name, datesToTimeStamps(dates));
    }

    public void setTimeStampArrayAttribute(final String objectPath, final String name,
            final long[] timeStamps)
    {
        assert objectPath != null;
        assert name != null;
        assert timeStamps != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> setAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int memoryTypeId =
                            baseWriter.h5.createArrayType(H5T_NATIVE_INT64, timeStamps.length,
                                    registry);
                    final int storageTypeId =
                            baseWriter.h5.createArrayType(H5T_STD_I64LE, timeStamps.length,
                                    registry);
                    baseWriter.setAttribute(objectPath, name,
                            HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                            storageTypeId, memoryTypeId, timeStamps);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(setAttributeRunnable);
    }

    public void writeTimeStamp(final String objectPath, final long timeStamp)
    {
        assert objectPath != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseWriter.writeScalar(objectPath, H5T_STD_I64LE, H5T_NATIVE_INT64,
                                    HDFNativeData.longToByte(timeStamp), true, true, registry);
                    baseWriter.setTypeVariant(dataSetId,
                            HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                            registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeScalarRunnable);
    }

    public void createTimeStampArray(String objectPath, int size)
    {
        createTimeStampArray(objectPath, size, HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    public void createTimeStampArray(final String objectPath, final long size, final int blockSize)
    {
        createTimeStampArray(objectPath, size, blockSize,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    public void createTimeStampArray(final String objectPath, final int size,
            final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert size >= 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int longBytes = 8;
                    final int dataSetId;
                    if (features.requiresChunking())
                    {
                        dataSetId =
                                baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features,
                                        new long[]
                                            { 0 }, new long[]
                                            { size }, longBytes, registry);
                    } else
                    {
                        dataSetId =
                                baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features,
                                        new long[]
                                            { size }, null, longBytes, registry);
                    }
                    baseWriter.setTypeVariant(dataSetId,
                            HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                            registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void createTimeStampArray(final String objectPath, final long length,
            final int blockSize, final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert length >= 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int longBytes = 8;
                    final int dataSetId =
                            baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features,
                                    new long[]
                                        { length }, new long[]
                                        { blockSize }, longBytes, registry);
                    baseWriter.setTypeVariant(dataSetId,
                            HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                            registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeTimeStampArray(final String objectPath, final long[] timeStamps)
    {
        writeTimeStampArray(objectPath, timeStamps,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    public void writeTimeStampArray(final String objectPath, final long[] timeStamps,
            final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert timeStamps != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int longBytes = 8;
                    final int dataSetId =
                            baseWriter.getOrCreateDataSetId(objectPath, H5T_STD_I64LE, new long[]
                                { timeStamps.length }, longBytes, features, registry);
                    H5Dwrite(dataSetId, H5T_NATIVE_INT64, H5S_ALL, H5S_ALL, H5P_DEFAULT, timeStamps);
                    baseWriter.setTypeVariant(dataSetId,
                            HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH,
                            registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeTimeStampArrayBlock(final String objectPath, final long[] data,
            final long blockNumber)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] dimensions = new long[]
                        { data.length };
                    final long[] slabStartOrNull = new long[]
                        { data.length * blockNumber };
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, new long[]
                                        { data.length * (blockNumber + 1) }, -1, registry);
                    baseWriter.checkIsTimeStamp(objectPath, dataSetId, registry);
                    final int dataSpaceId =
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, slabStartOrNull, dimensions);
                    final int memorySpaceId =
                            baseWriter.h5.createSimpleDataSpace(dimensions, registry);
                    H5Dwrite(dataSetId, H5T_NATIVE_INT64, memorySpaceId, dataSpaceId, H5P_DEFAULT,
                            data);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeTimeStampArrayBlockWithOffset(final String objectPath, final long[] data,
            final int dataSize, final long offset)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] blockDimensions = new long[]
                        { dataSize };
                    final long[] slabStartOrNull = new long[]
                        { offset };
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, new long[]
                                        { offset + dataSize }, -1, registry);
                    baseWriter.checkIsTimeStamp(objectPath, dataSetId, registry);
                    final int dataSpaceId =
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, slabStartOrNull, blockDimensions);
                    final int memorySpaceId =
                            baseWriter.h5.createSimpleDataSpace(blockDimensions, registry);
                    H5Dwrite(dataSetId, H5T_NATIVE_INT64, memorySpaceId, dataSpaceId, H5P_DEFAULT,
                            data);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeDate(final String objectPath, final Date date)
    {
        writeTimeStamp(objectPath, date.getTime());
    }

    public void writeDateArray(final String objectPath, final Date[] dates)
    {
        writeTimeStampArray(objectPath, datesToTimeStamps(dates));
    }

    public void writeDateArray(final String objectPath, final Date[] dates,
            final HDF5GenericStorageFeatures features)
    {
        writeTimeStampArray(objectPath, datesToTimeStamps(dates), features);
    }

    private static long[] datesToTimeStamps(Date[] dates)
    {
        assert dates != null;

        final long[] timestamps = new long[dates.length];
        for (int i = 0; i < timestamps.length; ++i)
        {
            timestamps[i] = dates[i].getTime();
        }
        return timestamps;
    }

    //
    // Duration
    //

    public void setTimeDurationAttribute(String objectPath, String name, long timeDuration,
            HDF5TimeUnit timeUnit)
    {
        baseWriter.checkOpen();
        baseWriter.setAttribute(objectPath, name, timeUnit.getTypeVariant(), H5T_STD_I64LE,
                H5T_NATIVE_INT64, new long[]
                    { timeDuration });
    }

    public void setTimeDurationAttribute(String objectPath, String name,
            HDF5TimeDuration timeDuration)
    {
        setTimeDurationAttribute(objectPath, name, timeDuration.getValue(), timeDuration.getUnit());
    }

    public void setTimeDurationArrayAttribute(final String objectPath, final String name,
            final HDF5TimeDurationArray timeDurations)
    {
        assert objectPath != null;
        assert name != null;
        assert timeDurations != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> setAttributeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int memoryTypeId =
                            baseWriter.h5.createArrayType(H5T_NATIVE_INT64,
                                    timeDurations.timeDurations.length, registry);
                    final int storageTypeId =
                            baseWriter.h5.createArrayType(H5T_STD_I64LE,
                                    timeDurations.timeDurations.length, registry);
                    baseWriter.setAttribute(objectPath, name,
                            timeDurations.timeUnit.getTypeVariant(), storageTypeId, memoryTypeId,
                            timeDurations.timeDurations);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(setAttributeRunnable);
    }

    public void writeTimeDuration(final String objectPath, final long timeDuration)
    {
        writeTimeDuration(objectPath, timeDuration, HDF5TimeUnit.SECONDS);
    }

    public void writeTimeDuration(final String objectPath, final long timeDuration,
            final HDF5TimeUnit timeUnit)
    {
        assert objectPath != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Object> writeScalarRunnable = new ICallableWithCleanUp<Object>()
            {
                public Object call(ICleanUpRegistry registry)
                {
                    final int dataSetId =
                            baseWriter.writeScalar(objectPath, H5T_STD_I64LE, H5T_NATIVE_INT64,
                                    HDFNativeData.longToByte(timeDuration), true, true, registry);
                    baseWriter.setTypeVariant(dataSetId, timeUnit.getTypeVariant(), registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeScalarRunnable);
    }

    public void writeTimeDuration(String objectPath, HDF5TimeDuration timeDuration)
    {
        writeTimeDuration(objectPath, timeDuration.getValue(), timeDuration.getUnit());
    }

    public void createTimeDurationArray(String objectPath, int size, HDF5TimeUnit timeUnit)
    {
        createTimeDurationArray(objectPath, size, timeUnit,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    public void createTimeDurationArray(final String objectPath, final long size,
            final int blockSize, final HDF5TimeUnit timeUnit)
    {
        createTimeDurationArray(objectPath, size, blockSize, timeUnit,
                HDF5GenericStorageFeatures.GENERIC_NO_COMPRESSION);
    }

    public void createTimeDurationArray(final String objectPath, final int size,
            final HDF5TimeUnit timeUnit, final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert size >= 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int longBytes = 8;
                    final int dataSetId;
                    if (features.requiresChunking())
                    {
                        dataSetId =
                                baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features,
                                        new long[]
                                            { 0 }, new long[]
                                            { size }, longBytes, registry);
                    } else
                    {
                        dataSetId =
                                baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features,
                                        new long[]
                                            { size }, null, longBytes, registry);
                    }
                    baseWriter.setTypeVariant(dataSetId, timeUnit.getTypeVariant(), registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void createTimeDurationArray(final String objectPath, final long size,
            final int blockSize, final HDF5TimeUnit timeUnit,
            final HDF5GenericStorageFeatures features)
    {
        assert objectPath != null;
        assert size >= 0;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int longBytes = 8;
                    final int dataSetId =
                            baseWriter.createDataSet(objectPath, H5T_STD_I64LE, features,
                                    new long[]
                                        { size }, new long[]
                                        { blockSize }, longBytes, registry);
                    baseWriter.setTypeVariant(dataSetId, timeUnit.getTypeVariant(), registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeTimeDurationArray(final String objectPath, final long[] timeDurations)
    {
        writeTimeDurationArray(objectPath, timeDurations, HDF5TimeUnit.SECONDS,
                HDF5IntStorageFeatures.INT_NO_COMPRESSION);
    }

    public void writeTimeDurationArray(final String objectPath, final long[] timeDurations,
            final HDF5TimeUnit timeUnit)
    {
        writeTimeDurationArray(objectPath, timeDurations, timeUnit,
                HDF5IntStorageFeatures.INT_NO_COMPRESSION);
    }

    public void writeTimeDurationArray(final String objectPath, final long[] timeDurations,
            final HDF5TimeUnit timeUnit, final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert timeDurations != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final int longBytes = 8;
                    final int dataSetId =
                            baseWriter.getOrCreateDataSetId(objectPath, H5T_STD_I64LE, new long[]
                                { timeDurations.length }, longBytes, features, registry);
                    H5Dwrite(dataSetId, H5T_NATIVE_INT64, H5S_ALL, H5S_ALL, H5P_DEFAULT,
                            timeDurations);
                    baseWriter.setTypeVariant(dataSetId, timeUnit.getTypeVariant(), registry);
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeTimeDurationArray(final String objectPath,
            final HDF5TimeDuration[] timeDurations)
    {
        writeTimeDurationArray(objectPath, timeDurations, HDF5IntStorageFeatures.INT_NO_COMPRESSION);
    }

    public void writeTimeDurationArray(final String objectPath,
            final HDF5TimeDuration[] timeDurations, final HDF5IntStorageFeatures features)
    {
        assert objectPath != null;
        assert timeDurations != null;

        if (timeDurations.length == 0)
        {
            return;
        }
        final HDF5TimeDurationArray durations = convertToDurationArray(timeDurations);
        writeTimeDurationArray(objectPath, durations.timeDurations, durations.timeUnit);
    }

    private HDF5TimeDurationArray convertToDurationArray(HDF5TimeDuration[] timeDurations)
    {
        HDF5TimeUnit unit = timeDurations[0].getUnit();
        boolean needsConversion = false;
        for (int i = 1; i < timeDurations.length; ++i)
        {
            final HDF5TimeUnit u = timeDurations[i].getUnit();
            if (u != unit)
            {
                if (u.ordinal() < unit.ordinal())
                {
                    unit = u;
                }
                needsConversion = true;
            }
        }
        final long[] durations = new long[timeDurations.length];
        if (needsConversion)
        {
            for (int i = 0; i < timeDurations.length; ++i)
            {
                durations[i] = unit.convert(timeDurations[i]);
            }
        } else
        {
            for (int i = 0; i < timeDurations.length; ++i)
            {
                durations[i] = timeDurations[i].getValue();
            }
        }
        return new HDF5TimeDurationArray(durations, unit);
    }

    public void writeTimeDurationArrayBlock(final String objectPath, final long[] data,
            final long blockNumber, final HDF5TimeUnit timeUnit)
    {
        writeTimeDurationArrayBlockWithOffset(objectPath, data, data.length, data.length
                * blockNumber, timeUnit);
    }

    public void writeTimeDurationArrayBlockWithOffset(final String objectPath, final long[] data,
            final int dataSize, final long offset, final HDF5TimeUnit timeUnit)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] blockDimensions = new long[]
                        { dataSize };
                    final long[] slabStartOrNull = new long[]
                        { offset };
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, new long[]
                                        { offset + dataSize }, -1, registry);
                    final HDF5TimeUnit storedUnit =
                            baseWriter.checkIsTimeDuration(objectPath, dataSetId, registry);
                    final int dataSpaceId =
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, slabStartOrNull, blockDimensions);
                    final int memorySpaceId =
                            baseWriter.h5.createSimpleDataSpace(blockDimensions, registry);
                    H5Dwrite(dataSetId, H5T_NATIVE_INT64, memorySpaceId, dataSpaceId, H5P_DEFAULT,
                            convertTimeDurations(storedUnit, timeUnit, data));
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    public void writeTimeDurationArrayBlock(final String objectPath, final HDF5TimeDuration[] data,
            final long blockNumber)
    {
        writeTimeDurationArrayBlockWithOffset(objectPath, data, data.length, data.length
                * blockNumber);
    }

    public void writeTimeDurationArrayBlockWithOffset(final String objectPath,
            final HDF5TimeDuration[] data, final int dataSize, final long offset)
    {
        assert objectPath != null;
        assert data != null;

        baseWriter.checkOpen();
        final ICallableWithCleanUp<Void> writeRunnable = new ICallableWithCleanUp<Void>()
            {
                public Void call(ICleanUpRegistry registry)
                {
                    final long[] blockDimensions = new long[]
                        { dataSize };
                    final long[] slabStartOrNull = new long[]
                        { offset };
                    final int dataSetId =
                            baseWriter.h5.openAndExtendDataSet(baseWriter.fileId, objectPath,
                                    baseWriter.fileFormat, new long[]
                                        { offset + dataSize }, -1, registry);
                    final HDF5TimeUnit storedUnit =
                            baseWriter.checkIsTimeDuration(objectPath, dataSetId, registry);
                    final int dataSpaceId =
                            baseWriter.h5.getDataSpaceForDataSet(dataSetId, registry);
                    baseWriter.h5.setHyperslabBlock(dataSpaceId, slabStartOrNull, blockDimensions);
                    final int memorySpaceId =
                            baseWriter.h5.createSimpleDataSpace(blockDimensions, registry);
                    H5Dwrite(dataSetId, H5T_NATIVE_INT64, memorySpaceId, dataSpaceId, H5P_DEFAULT,
                            convertTimeDurations(storedUnit, data));
                    return null; // Nothing to return.
                }
            };
        baseWriter.runner.call(writeRunnable);
    }

    static long[] convertTimeDurations(final HDF5TimeUnit toTimeUnit,
            final HDF5TimeUnit fromTimeUnit, final long[] data)
    {
        if (toTimeUnit != fromTimeUnit)
        {
            final long[] convertedData = new long[data.length];
            for (int i = 0; i < data.length; ++i)
            {
                convertedData[i] = toTimeUnit.convert(data[i], fromTimeUnit);
            }
            return convertedData;
        } else
        {
            return data;
        }
    }

    static long[] convertTimeDurations(final HDF5TimeUnit toTimeUnit, final HDF5TimeDuration[] data)
    {
        final long[] convertedData = new long[data.length];
        for (int i = 0; i < data.length; ++i)
        {
            convertedData[i] = toTimeUnit.convert(data[i]);
        }
        return convertedData;
    }

}
