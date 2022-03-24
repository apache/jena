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
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.jts.GeometryTransformation;
import org.apache.jena.geosparql.implementation.registry.MathTransformRegistry;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class GeometryTransformIndex {

    private static boolean INDEX_ACTIVE = false;
    private static final String GEOMETRY_TRANSFORM_LABEL = "Geometry Transform";
    private static ExpiringMap<String, GeometryWrapper> GEOMETRY_TRANSFORM_INDEX = new ExpiringMap<>(GEOMETRY_TRANSFORM_LABEL, UNLIMITED_MAP, MAP_EXPIRY_INTERVAL);

    /**
     *
     * @param sourceGeometryWrapper
     * @param srsURI
     * @param storeSRSTransform
     * @return GeometryWrapper following transformation.
     * @throws TransformException
     * @throws org.opengis.util.FactoryException
     */
    public static final GeometryWrapper transform(GeometryWrapper sourceGeometryWrapper, String srsURI, Boolean storeSRSTransform) throws TransformException, FactoryException {

        GeometryWrapper transformedGeometryWrapper;
        String key = sourceGeometryWrapper.getLexicalForm() + "@" + srsURI;

        if (INDEX_ACTIVE && storeSRSTransform) {
            
            transformedGeometryWrapper = GEOMETRY_TRANSFORM_INDEX.get(key);
            if (transformedGeometryWrapper == null) {
                transformedGeometryWrapper = transform(sourceGeometryWrapper, srsURI);
                GEOMETRY_TRANSFORM_INDEX.put(key, transformedGeometryWrapper);
            }
                        
        } else {
            transformedGeometryWrapper = transform(sourceGeometryWrapper, srsURI);
        }
        
        return transformedGeometryWrapper;
    }

    private static GeometryWrapper transform(GeometryWrapper sourceGeometryWrapper, String srsURI) throws MismatchedDimensionException, FactoryException, TransformException {
        CoordinateReferenceSystem sourceCRS = sourceGeometryWrapper.getCRS();
        CoordinateReferenceSystem targetCRS = SRSRegistry.getCRS(srsURI);
        MathTransform transform = MathTransformRegistry.getMathTransform(sourceCRS, targetCRS);
        Geometry parsingGeometry = sourceGeometryWrapper.getParsingGeometry();

        //Transform the coordinates into a new Geometry.
        Geometry transformedGeometry = GeometryTransformation.transform(parsingGeometry, transform);

        //Construct a new GeometryWrapper using info from original GeometryWrapper.
        String geometryDatatypeURI = sourceGeometryWrapper.getGeometryDatatypeURI();
        DimensionInfo dimensionInfo = sourceGeometryWrapper.getDimensionInfo();
        return new GeometryWrapper(transformedGeometry, srsURI, geometryDatatypeURI, dimensionInfo);
    }

    /**
     * Empty the Geometry Transform Index.
     */
    public static final void clear() {
        GEOMETRY_TRANSFORM_INDEX.clear();
    }

    /**
     * Sets whether the maximum size of the Geometry Transform Index.
     *
     * @param maxSize : use -1 for unlimited size
     */
    public static final void setMaxSize(int maxSize) {
        GEOMETRY_TRANSFORM_INDEX.setMaxSize(maxSize);
    }

    /**
     * Sets the expiry time in milliseconds of the Geometry Transform Index, if
     * active.
     *
     * @param expiryInterval : use 0 or negative for unlimited timeout
     */
    public static final void setExpiry(long expiryInterval) {
        GEOMETRY_TRANSFORM_INDEX.setExpiryInterval(expiryInterval);
    }

    /**
     *
     * @return Number of items in the index.
     */
    public static final long getGeometryTransformIndexSize() {
        return GEOMETRY_TRANSFORM_INDEX.mappingCount();
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
            GEOMETRY_TRANSFORM_INDEX.startExpiry();
        } else {
            GEOMETRY_TRANSFORM_INDEX.stopExpiry();
        }
    }

    /**
     * Reset the index to the provided max size and expiry interval.<br>
     * All contents will be lost.
     *
     * @param maxSize
     * @param expiryInterval
     */
    public static void reset(int maxSize, long expiryInterval) {
        GEOMETRY_TRANSFORM_INDEX = new ExpiringMap<>(GEOMETRY_TRANSFORM_LABEL, maxSize, expiryInterval);
    }
}
