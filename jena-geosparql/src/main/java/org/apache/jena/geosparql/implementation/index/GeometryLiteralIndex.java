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

import static org.apache.jena.geosparql.implementation.index.CacheConfiguration.MAP_EXPIRY_INTERVAL;
import static org.apache.jena.geosparql.implementation.index.CacheConfiguration.UNLIMITED_MAP;

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;

/**
 *
 *
 */
public class GeometryLiteralIndex {

    private static boolean INDEX_ACTIVE = false;
    private static Cache<String, GeometryWrapper>
            PRIMARY_INDEX = CacheConfiguration.create(UNLIMITED_MAP, MAP_EXPIRY_INTERVAL);
    private static Cache<String, GeometryWrapper> SECONDARY_INDEX = CacheConfiguration.create(UNLIMITED_MAP, MAP_EXPIRY_INTERVAL);

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

    private static GeometryWrapper retrieveMemoryIndex(String geometryLiteral, GeometryDatatype geometryDatatype, Cache<String, GeometryWrapper> index, Cache<String, GeometryWrapper> otherIndex) {

        GeometryWrapper geometryWrapper;

        if (INDEX_ACTIVE) {

            geometryWrapper = index.getIfPresent(geometryLiteral);
            if (geometryWrapper == null) {
                geometryWrapper = otherIndex.getIfPresent(geometryLiteral);
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
     *
     * @return Number of items in the primary index.
     */
    public static final long getPrimaryIndexSize() {
        return PRIMARY_INDEX.size();
    }

    /**
     *
     * @return Number of items in the secondary index.
     */
    public static final long getSecondaryIndexSize() {
        return SECONDARY_INDEX.size();
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
    }

    /**
     * Reset the indexes to the provided max size and expiry interval.<br>
     * All contents will be lost.
     *
     * @param maxSize
     * @param expiryInterval
     */
    public static void reset(int maxSize, long expiryInterval) {
        PRIMARY_INDEX = CacheConfiguration.create(maxSize, expiryInterval);
        SECONDARY_INDEX = CacheConfiguration.create(maxSize, expiryInterval);
    }

}
