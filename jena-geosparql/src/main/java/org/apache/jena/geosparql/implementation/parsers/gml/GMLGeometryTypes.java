/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.implementation.parsers.gml;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.locationtech.jts.geom.Geometry;

/** GML element names used when serializing JTS geometries. */
public final class GMLGeometryTypes {
    private GMLGeometryTypes() {}

    /**
     * Returns the GML element name that the writer uses for the supplied JTS
     * geometry, for example {@code MultiCurve} for a JTS MultiLineString.
     * This mapping does not recover the type of an original GML literal.
     *
     * @param geometry The JTS geometry to represent in GML.
     * @return The element's local name, without a namespace prefix or URI.
     * @throws DatatypeFormatException if the writer does not support the geometry type.
     */
    public static String fromJts(Geometry geometry) {
        return switch (geometry.getGeometryType()) {
            case "Point" -> "Point";
            case "LineString" -> "LineString";
            case "Polygon" -> "Polygon";
            case "MultiPoint" -> "MultiPoint";
            case "MultiLineString" -> "MultiCurve";
            case "MultiPolygon" -> "MultiSurface";
            case "GeometryCollection" -> "MultiGeometry";
            default -> throw new DatatypeFormatException("Geometry type not supported: " + geometry.getGeometryType());
        };
    }
}
