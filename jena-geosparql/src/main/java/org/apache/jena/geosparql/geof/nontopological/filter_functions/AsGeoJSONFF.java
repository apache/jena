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
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GeoJSONDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

public class AsGeoJSONFF extends FunctionBase1 {

    private final GeoJsonWriter writer;

    public AsGeoJSONFF() {
        writer = new GeoJsonWriter();
        writer.setForceCCW(true);
        // removed from GeoJSON 2016
        writer.setEncodeCRS(false);
    }

    @Override
    public NodeValue exec(NodeValue v) {
        try {
            GeometryWrapper gw = GeometryWrapper.extract(v);
            // GeoJSON 2016 removed support for other crs, need to transform to CRS 84
            GeometryWrapper convertedGeom = gw.transform(SRS_URI.DEFAULT_WKT_CRS84);

            String json = writer.write(convertedGeom.getParsingGeometry());

            Node node = NodeFactory.createLiteralByValue(json, GeoJSONDatatype.INSTANCE);
            NodeValue result = NodeValue.makeNode(node);

            return result;
        } catch (MismatchedDimensionException | TransformException | FactoryException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }
}
