/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.spatial.index.compat;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.index.v1.SpatialIndexAdapterV1;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexIoKryo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("removal")
public class SpatialIndexIo {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** Attempt to load a spatial index from file using all supported formats. */
    public static final SpatialIndex load(Path spatialIndexFile) throws SpatialIndexException {
        return load(spatialIndexFile, false);
    }

    /**
     * Attempt to load a spatial index from file using all supported formats.
     * This method should only be used for testing as it allows suppressing warnings when loading legacy index formats.
     */
    public static final SpatialIndex load(Path spatialIndexFile, boolean suppressLegacyWarnings) throws SpatialIndexException {
        SpatialIndex result;
        try {
            result = SpatialIndexIoKryo.load(spatialIndexFile);
        } catch (Throwable t1) {
            if (!suppressLegacyWarnings) {
                LOGGER.warn("Failed to load spatial index with latest format. Trying legacy formats...", t1);
            }
            try {
                org.apache.jena.geosparql.spatial.index.v1.SpatialIndexV1 v1 = org.apache.jena.geosparql.spatial.index.v1.SpatialIndexV1.load(spatialIndexFile.toFile());
                result = new SpatialIndexAdapterV1(v1);

                if (!suppressLegacyWarnings) {
                    LOGGER.warn("Successfully loaded spatial index with legacy format v1. Upgrade advised.");
                }
            } catch (Throwable t2) {
                LOGGER.warn("Failed to load spatial index legacy format.", t2);
                t1.addSuppressed(new RuntimeException("Failed to load spatial index with any format.", t2));
                throw t1;
            }
        }
        return result;
    }
}
