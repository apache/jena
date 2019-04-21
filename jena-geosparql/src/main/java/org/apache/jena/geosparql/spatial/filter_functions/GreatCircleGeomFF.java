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
package org.apache.jena.geosparql.spatial.filter_functions;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase3;
import org.apache.jena.sparql.util.FmtUtils;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class GreatCircleGeomFF extends FunctionBase3 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2, NodeValue v3) {

        try {
            GeometryWrapper geometry1 = GeometryWrapper.extract(v1, GeometryLiteralIndex.GeometryIndex.PRIMARY);
            GeometryWrapper geometry2 = GeometryWrapper.extract(v2, GeometryLiteralIndex.GeometryIndex.SECONDARY);

            if (!(v3.isIRI() || v3.isString())) {
                throw new ExprEvalException("Not an IRI or String: " + FmtUtils.stringForNode(v3.asNode()));
            }

            String unitsURI;
            if (v3.isIRI()) {
                unitsURI = v3.asNode().getURI();
            } else {
                unitsURI = v3.asString();
            }
            double distance = geometry1.distanceGreatCircle(geometry2, unitsURI);
            return NodeValue.makeDouble(distance);
        } catch (DatatypeFormatException | FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }

}
