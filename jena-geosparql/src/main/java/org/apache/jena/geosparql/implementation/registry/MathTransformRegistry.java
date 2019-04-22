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
package org.apache.jena.geosparql.implementation.registry;

import java.lang.invoke.MethodHandles;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.sis.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class MathTransformRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final MultiKeyMap<CoordinateReferenceSystem, MathTransform> MATH_TRANSFORM_REGISTRY = MultiKeyMap.multiKeyMap(new HashedMap<>());

    public synchronized static final MathTransform getMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws FactoryException, MismatchedDimensionException, TransformException {

        MathTransform transform;
        MultiKey<CoordinateReferenceSystem> key = new MultiKey<>(sourceCRS, targetCRS);
        if (MATH_TRANSFORM_REGISTRY.containsKey(key)) {
            transform = MATH_TRANSFORM_REGISTRY.get(key);
        } else {
            transform = CRS.findOperation(sourceCRS, targetCRS, null).getMathTransform();
            MATH_TRANSFORM_REGISTRY.put(key, transform);
        }
        return transform;
    }

    public synchronized static final void clear() {
        MATH_TRANSFORM_REGISTRY.clear();
    }

}
