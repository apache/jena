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
package org.apache.jena.geosparql.spatial.property_functions.cardinal;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.spatial.CardinalDirection;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.property_functions.GenericSpatialGeomPropertyFunction;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public abstract class GenericCardinalGeomPropertyFunction extends GenericSpatialGeomPropertyFunction {

    protected abstract CardinalDirection getCardinalDirection();

    @Override
    protected SearchEnvelope buildSearchEnvelope(GeometryWrapper geometryWrapper, SRSInfo indexSRSInfo) {
        CardinalDirection direction = getCardinalDirection();
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, indexSRSInfo, direction);

        return searchEnvelope;
    }

    @Override
    protected boolean checkSecondFilter(SpatialArguments spatialArguments, GeometryWrapper targetGeometryWrapper) {
        //Test Geometry against the Geometry from Object to see if it is a success.
        //Used when checking against bound Subjects.
        //Cardinal functions only check against the search envelope.
        SearchEnvelope searchEnvelope = spatialArguments.getSearchEnvelope();

        try {

            GeometryWrapper srs = targetGeometryWrapper.convertSRS(searchEnvelope.getSrsURI());
            Envelope targetEnvelope = srs.getEnvelope();

            boolean result = searchEnvelope.check(targetEnvelope);

            return result;
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + targetGeometryWrapper.asLiteral(), ex);
        }
    }

}
