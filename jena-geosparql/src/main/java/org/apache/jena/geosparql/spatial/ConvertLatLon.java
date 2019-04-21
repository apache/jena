/*
 * Copyright 2019 .
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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.WKTLiteralFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.FmtUtils;

/**
 *
 *
 */
public class ConvertLatLon {

    public static final Literal toLiteral(double lat, double lon) {
        checkBounds(lat, lon);
        return WKTLiteralFactory.createPoint(lat, lon, SRS_URI.WGS84_CRS);
    }

    public static final String toWKT(double lat, double lon) {
        Literal wktPoint = toLiteral(lat, lon);
        return wktPoint.getLexicalForm();
    }

    public static final NodeValue toNodeValue(NodeValue latNodeValue, NodeValue lonNodeValue) {
        if (!latNodeValue.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(latNodeValue.asNode()));
        }

        if (!lonNodeValue.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(lonNodeValue.asNode()));
        }

        double lat = latNodeValue.getDouble();
        double lon = lonNodeValue.getDouble();
        Literal wktPoint = toLiteral(lat, lon);

        return NodeValue.makeNode(wktPoint.asNode());
    }

    public static final Node toNode(Node latNode, Node lonNode) {
        NodeValue result = toNodeValue(NodeValue.makeNode(latNode), NodeValue.makeNode(lonNode));
        return result.asNode();
    }

    public static final GeometryWrapper toGeometryWrapper(Node latNode, Node lonNode) {

        NodeValue latNodeValue = NodeValue.makeNode(latNode);
        NodeValue lonNodeValue = NodeValue.makeNode(lonNode);

        if (!latNodeValue.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(latNodeValue.asNode()));
        }

        if (!lonNodeValue.isNumber()) {
            throw new DatatypeFormatException("Not a number: " + FmtUtils.stringForNode(lonNodeValue.asNode()));
        }
        double lat = latNodeValue.getDouble();
        double lon = lonNodeValue.getDouble();
        checkBounds(lat, lon);
        return GeometryWrapper.fromPoint(lat, lon, SRS_URI.WGS84_CRS);
    }

    public static final void checkBounds(double latitude, double longitude) throws DatatypeFormatException {

        if (latitude < -90.0 || latitude > 90.0) {
            throw new DatatypeFormatException("Lat/Lon out of bounds: " + latitude + ", " + longitude);
        } else if (longitude < -180.0 || longitude > 180.0) {
            throw new DatatypeFormatException("Lat/Lon out of bounds: " + latitude + ", " + longitude);
        }
    }
}
