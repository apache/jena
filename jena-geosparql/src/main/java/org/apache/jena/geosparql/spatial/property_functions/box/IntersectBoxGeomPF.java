/*
 * Copyright 2018 .
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
package org.apache.jena.geosparql.spatial.property_functions.box;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class IntersectBoxGeomPF extends GenericSpatialGeomBoxPropertyFunction {

    @Override
    protected boolean checkSecondFilter(SpatialArguments spatialArguments, GeometryWrapper targetGeometryWrapper) {
        GeometryWrapper geometryWrapper = spatialArguments.getGeometryWrapper();
        try {
            return geometryWrapper.intersects(targetGeometryWrapper);
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + targetGeometryWrapper.asLiteral() + ", " + geometryWrapper.asLiteral(), ex);
        }
    }

}
