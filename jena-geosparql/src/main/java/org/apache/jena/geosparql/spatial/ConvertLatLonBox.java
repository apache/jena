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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.WKTLiteralFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.FmtUtils;

/**
 *
 *
 */
public class ConvertLatLonBox {

    public static final String toWKT(double latMin, double lonMin, double latMax, double lonMax) {
        Literal wktBox = toLiteral(latMin, lonMin, latMax, lonMax);
        return wktBox.getLexicalForm();
    }

    public static final Literal toLiteral(double latMin, double lonMin, double latMax, double lonMax) {
        ConvertLatLon.checkBounds(latMin, lonMin);
        ConvertLatLon.checkBounds(latMax, lonMax);
        return WKTLiteralFactory.createBox(latMin, lonMin, latMax, lonMax, SRS_URI.WGS84_CRS);
    }

    public static final NodeValue toNodeValue(NodeValue v1, NodeValue v2, NodeValue v3, NodeValue v4) {
        if (!v1.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(v1.asNode()));
        }

        if (!v2.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(v2.asNode()));
        }

        if (!v3.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(v3.asNode()));
        }

        if (!v4.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(v4.asNode()));
        }

        double latMin = v1.getDouble();
        double lonMin = v2.getDouble();
        double latMax = v3.getDouble();
        double lonMax = v4.getDouble();
        Literal wktBox = toLiteral(latMin, lonMin, latMax, lonMax);

        return NodeValue.makeNode(wktBox.asNode());
    }

    public static final Node toNode(Node n1, Node n2, Node n3, Node n4) {
        NodeValue result = toNodeValue(NodeValue.makeNode(n1), NodeValue.makeNode(n2), NodeValue.makeNode(n3), NodeValue.makeNode(n4));
        return result.asNode();
    }

}
