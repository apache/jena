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
package org.apache.jena.geosparql.spatial.property_functions.cardinal;

import java.util.List;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.util.FmtUtils;

/**
 *
 *
 */
public abstract class GenericCardinalPropertyFunction extends GenericCardinalGeomPropertyFunction {

    private static final int LAT_POS = 0;
    private static final int LON_POS = 1;
    private static final int LIMIT_POS = 2;

    @Override
    protected SpatialArguments extractObjectArguments(Node predicate, PropFuncArg object, SRSInfo indexSRSInfo) {

        try {
            //Check minimum arguments.
            List<Node> objectArgs = object.getArgList();
            if (objectArgs.size() < 2) {
                throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": Minimum of 2 arguments.");
            } else if (objectArgs.size() > 3) {
                throw new ExprEvalException(FmtUtils.stringForNode(predicate) + ": Maximum of 3 arguments.");
            }

            Node lat = objectArgs.get(LAT_POS);
            Node lon = objectArgs.get(LON_POS);

            //Check minimum arguments are all bound.
            if (lat.isVariable() || lon.isVariable()) {
                throw new ExprEvalException("Arguments are not all concrete: " + FmtUtils.stringForNode(lat) + ", " + FmtUtils.stringForNode(lon));
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

            GeometryWrapper geometryWrapper = ConvertLatLon.toGeometryWrapper(lat, lon);

            SearchEnvelope searchEnvelope = buildSearchEnvelope(geometryWrapper, indexSRSInfo);

            return new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }

}
