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
package org.apache.jena.geosparql.implementation;

import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Convenience methods to produce WKT Literals.
 *
 */
public class WKTLiteralFactory {

    /**
     * WKT point.
     *
     * @param x
     * @param y
     * @return WKT Point in default WKT CRS84.
     */
    public static final Literal createPoint(Double x, Double y) {
        return createPoint(x, y, "");
    }

    /**
     * WKT point with no check for value range or order.
     *
     * @param x
     * @param y
     * @param srsURI
     * @return WKT Point with SRS URI.
     */
    public static final Literal createPoint(Double x, Double y, String srsURI) {
        String tidyURI = tidySrsURI(srsURI);
        return ResourceFactory.createTypedLiteral(tidyURI + "POINT(" + reducePrecision(x) + " " + reducePrecision(y) + ")", WKTDatatype.INSTANCE);
    }

    /**
     * WKT LineString between two points.
     *
     * @param xMin
     * @param yMin
     * @param xMax
     * @param yMax
     * @return WKT LineString in default WKT CRS84.
     */
    public static final Literal createLineString(Double xMin, Double yMin, Double xMax, Double yMax) {
        return createLineString(xMin, yMin, xMax, yMax, "");
    }

    /**
     * WKT LineString between two points with no check for value range or order.
     *
     * @param xMin
     * @param yMin
     * @param xMax
     * @param yMax
     * @return WKT LineString with SRS URI.
     */
    public static final Literal createLineString(Double xMin, Double yMin, Double xMax, Double yMax, String srsURI) {
        String tidyURI = tidySrsURI(srsURI);
        return ResourceFactory.createTypedLiteral(tidyURI + "LINESTRING(" + reducePrecision(xMin) + " " + reducePrecision(yMin) + ", " + reducePrecision(xMax) + " " + reducePrecision(yMax) + ")", WKTDatatype.INSTANCE);
    }

    /**
     * WKT Polygon box from two points.
     *
     * @param xMin
     * @param yMin
     * @param xMax
     * @param yMax
     * @return WKT LineString in default WKT CRS84.
     */
    public static final Literal createBox(Double xMin, Double yMin, Double xMax, Double yMax) {
        return createLineString(xMin, yMin, xMax, yMax, "");
    }

    /**
     * WKT Polygon box from two points with no check for value range or order.
     *
     * @param xMin
     * @param yMin
     * @param xMax
     * @param yMax
     * @return WKT LineString with SRS URI.
     */
    public static final Literal createBox(Double xMin, Double yMin, Double xMax, Double yMax, String srsURI) {
        String tidyURI = tidySrsURI(srsURI);
        return ResourceFactory.createTypedLiteral(tidyURI + "POLYGON((" + reducePrecision(xMin) + " " + reducePrecision(yMin) + ", " + reducePrecision(xMax) + " " + reducePrecision(yMin) + ", " + reducePrecision(xMax) + " " + reducePrecision(yMax) + ", " + reducePrecision(xMin) + " " + reducePrecision(yMax) + ", " + reducePrecision(xMin) + " " + reducePrecision(yMin) + "))", WKTDatatype.INSTANCE);
    }

    private static String tidySrsURI(String srsURI) {
        String tidyURI;
        if (!srsURI.isEmpty()) {
            tidyURI = "<" + srsURI + "> ";
        } else {
            tidyURI = "";
        }
        return tidyURI;
    }

    /**
     * Reduce precision if decimal places are zero.
     *
     * @param value
     * @return Value with zero decimal places stripped.
     */
    public static final String reducePrecision(Double value) {
        long longValue = value.longValue();

        if (value == longValue) {
            return Long.toString(longValue);
        } else {
            return value.toString();
        }
    }

}
