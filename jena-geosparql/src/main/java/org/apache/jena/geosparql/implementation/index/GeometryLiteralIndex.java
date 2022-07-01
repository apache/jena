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
package org.apache.jena.geosparql.implementation.index;

import io.github.galbiston.expiring_map.ExpiringMap;
import static io.github.galbiston.expiring_map.MapDefaultValues.MAP_EXPIRY_INTERVAL;
import static io.github.galbiston.expiring_map.MapDefaultValues.UNLIMITED_MAP;
import java.util.Map;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;

/**
 *
 *
 */
public class GeometryLiteralIndex {

    private static boolean INDEX_ACTIVE = false;
    private static final String PRIMARY_INDEX_LABEL = "Primary Geometry Literal Index";
    private static final String SECONDARY_INDEX_LABEL = "Secondary Geometry Literal Index";
    private static ExpiringMap<String, GeometryWrapper> PRIMARY_INDEX = new ExpiringMap<>(PRIMARY_INDEX_LABEL, UNLIMITED_MAP, MAP_EXPIRY_INTERVAL);
    private static ExpiringMap<String, GeometryWrapper> SECONDARY_INDEX = new ExpiringMap<>(SECONDARY_INDEX_LABEL, UNLIMITED_MAP, MAP_EXPIRY_INTERVAL);

    public enum GeometryIndex {
        PRIMARY, SECONDARY
    }

    public static final GeometryWrapper retrieve(String geometryLiteral, GeometryDatatype geometryDatatype, GeometryIndex targetIndex) {
        GeometryWrapper geometryWrapper;

        switch (targetIndex) {
            case SECONDARY:
                geometryWrapper = retrieveMemoryIndex(geometryLiteral, geometryDatatype, SECONDARY_INDEX, PRIMARY_INDEX);
                break;
            default:
                geometryWrapper = retrieveMemoryIndex(geometryLiteral, geometryDatatype, PRIMARY_INDEX, SECONDARY_INDEX);
        }

        return geometryWrapper;
    }

    private static GeometryWrapper retrieveMemoryIndex(String geometryLiteral, GeometryDatatype geometryDatatype, Map<String, GeometryWrapper> index, Map<String, GeometryWrapper> otherIndex) {

        GeometryWrapper geometryWrapper;

        if (INDEX_ACTIVE) {

            geometryWrapper = index.get(geometryLiteral);
            if (geometryWrapper == null) {
                geometryWrapper = otherIndex.get(geometryLiteral);
                if (geometryWrapper == null) {
                    geometryWrapper = geometryDatatype.read(geometryLiteral);
                }
                index.put(geometryLiteral, geometryWrapper);
            }
         
            return geometryWrapper;
        }

        return geometryDatatype.read(geometryLiteral);

    }

    /**
     * Empty the Geometry Literal Index.
     */
    public static final void clear() {
        PRIMARY_INDEX.clear();
        SECONDARY_INDEX.clear();
    }

    /**
     * Sets the maximum size of Geometry Literal Index.
     *
     * @param maxSize : use -1 for unlimited size
     */
    public static final void setMaxSize(int maxSize) {
        PRIMARY_INDEX.setMaxSize(maxSize);
        SECONDARY_INDEX.setMaxSize(maxSize);
    }

    /**
     * Sets the expiry time in milliseconds of the Geometry Literal Indexes, if
     * active.
     *
     * @param expiryInterval : use 0 or negative for unlimited timeout
     */
    public static final void setExpiry(long expiryInterval) {
        PRIMARY_INDEX.setExpiryInterval(expiryInterval);
        SECONDARY_INDEX.setExpiryInterval(expiryInterval);
    }

    /**
     *
     * @return Number of items in the primary index.
     */
    public static final long getPrimaryIndexSize() {
        return PRIMARY_INDEX.mappingCount();
    }

    /**
     *
     * @return Number of items in the secondary index.
     */
    public static final long getSecondaryIndexSize() {
        return SECONDARY_INDEX.mappingCount();
    }

    /**
     *
     * @return True if index is active.
     */
    public static boolean isIndexActive() {
        return INDEX_ACTIVE;
    }

    /**
     * Sets whether the index is active.
     *
     * @param indexActive
     */
    public static void setIndexActive(boolean indexActive) {
        INDEX_ACTIVE = indexActive;
        if (INDEX_ACTIVE) {
            PRIMARY_INDEX.startExpiry();
            SECONDARY_INDEX.startExpiry();
        } else {
            PRIMARY_INDEX.stopExpiry();
            SECONDARY_INDEX.stopExpiry();
        }
    }

    /**
     * Reset the indexes to the provided max size and expiry interval.<br>
     * All contents will be lost.
     *
     * @param maxSize
     * @param expiryInterval
     */
    public static void reset(int maxSize, long expiryInterval) {
        PRIMARY_INDEX = new ExpiringMap<>(PRIMARY_INDEX_LABEL, maxSize, expiryInterval);
        SECONDARY_INDEX = new ExpiringMap<>(SECONDARY_INDEX_LABEL, maxSize, expiryInterval);
    }

}
