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
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.spatial.ConvertLatLonBox;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.property_functions.GenericSpatialGeomPropertyFunction;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
import java.util.List;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.util.FmtUtils;

/**
 *
 *
 */
public abstract class GenericSpatialBoxPropertyFunction extends GenericSpatialGeomPropertyFunction {

    private static final int LAT_MIN_POS = 0;
    private static final int LON_MIN_POS = 1;
    private static final int LAT_MAX_POS = 2;
    private static final int LON_MAX_POS = 3;
    private static final int LIMIT_POS = 4;

    @Override
    protected SpatialArguments extractObjectArguments(Node predicate, PropFuncArg object, SRSInfo indexSRSInfo) {
        try {
            //Check minimum arguments.
            List<Node> objectArgs = object.getArgList();
            if (objectArgs.size() < 4) {
                throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": Minimum of 4 arguments.");
            } else if (objectArgs.size() > 5) {
                throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": Maximum of 5 arguments.");
            }

            Node latMin = objectArgs.get(LAT_MIN_POS);
            Node lonMin = objectArgs.get(LON_MIN_POS);
            Node latMax = objectArgs.get(LAT_MAX_POS);
            Node lonMax = objectArgs.get(LON_MAX_POS);

            //Check minimum arguments are all bound.
            if (latMin.isVariable() || lonMin.isVariable() || latMax.isVariable() || lonMax.isVariable()) {
                throw new ExprEvalException("Arguments are not all concrete: " + FmtUtils.stringForNode(latMin) + ", " + FmtUtils.stringForNode(lonMin) + FmtUtils.stringForNode(latMax) + ", " + FmtUtils.stringForNode(lonMax));
            }

            //Find the limit.
            int limit;
            if (objectArgs.size() > LIMIT_POS) {
                NodeValue limitNode = NodeValue.makeNode(objectArgs.get(LIMIT_POS));
                if (!limitNode.isInteger()) {
                    throw new ExprEvalException("Not an integer: " + FmtUtils.stringForNode(limitNode.getNode()));
                }
                limit = limitNode.getInteger().intValue();
            } else {
                limit = DEFAULT_LIMIT;
            }

            Node geometryNode = ConvertLatLonBox.toNode(latMin, lonMin, latMax, lonMax);
            GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometryNode);

            SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, indexSRSInfo);

            return new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }
}
