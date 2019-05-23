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
package org.apache.jena.geosparql.implementation.parsers.wkt;

import java.util.Arrays;
import java.util.Objects;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.parsers.ParserReader;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 *
 *
 */
public class WKTReader implements ParserReader {

    private static final GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    private final CoordinateSequenceDimensions dims;
    private final Geometry geometry;
    private final DimensionInfo dimensionInfo;
    private final String srsURI;

    protected WKTReader(String geometryType, String dimensionString, String coordinates, String srsURI) {
        this.dims = convertDimensionString(dimensionString);
        this.geometry = buildGeometry(geometryType, coordinates);
        this.dimensionInfo = new DimensionInfo(dims, geometry.getDimension());
        this.srsURI = srsURI;
    }

    protected WKTReader(String geometryType, String dimensionString, String coordinates) {
        this(geometryType, dimensionString, coordinates, SRS_URI.DEFAULT_WKT_CRS84);
    }

    protected WKTReader() {
        this("point", "", "", SRS_URI.DEFAULT_WKT_CRS84);
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public CoordinateSequenceDimensions getDimensions() {
        return dims;
    }

    @Override
    public DimensionInfo getDimensionInfo() {
        return dimensionInfo;
    }

    @Override
    public String getSrsURI() {
        return srsURI;
    }

    private static CoordinateSequenceDimensions convertDimensionString(String dimensionsString) {

        CoordinateSequenceDimensions dims;
        switch (dimensionsString) {
            case "zm":
                dims = CoordinateSequenceDimensions.XYZM;
                break;
            case "z":
                dims = CoordinateSequenceDimensions.XYZ;
                break;
            case "m":
                dims = CoordinateSequenceDimensions.XYM;
                break;
            default:
                dims = CoordinateSequenceDimensions.XY;
                break;
        }
        return dims;
    }

    private Geometry buildGeometry(String geometryType, String coordinates) throws DatatypeFormatException {

        Geometry geo;

        try {
            switch (geometryType) {
                case "point":
                    CustomCoordinateSequence pointSequence = new CustomCoordinateSequence(dims, clean(coordinates));
                    geo = GEOMETRY_FACTORY.createPoint(pointSequence);
                    break;
                case "linestring":
                    CustomCoordinateSequence lineSequence = new CustomCoordinateSequence(dims, clean(coordinates));
                    geo = GEOMETRY_FACTORY.createLineString(lineSequence);
                    break;
                case "linearring":
                    CustomCoordinateSequence linearSequence = new CustomCoordinateSequence(dims, clean(coordinates));
                    geo = GEOMETRY_FACTORY.createLinearRing(linearSequence);
                    break;
                case "polygon":
                    geo = buildPolygon(coordinates);
                    break;
                case "multipoint":
                    CustomCoordinateSequence multiPointSequence = new CustomCoordinateSequence(dims, clean(coordinates));
                    geo = GEOMETRY_FACTORY.createMultiPoint(multiPointSequence);
                    break;
                case "multilinestring":
                    geo = buildMultiLineString(coordinates);
                    break;
                case "multipolygon":
                    geo = buildMultiPolygon(coordinates);
                    break;
                case "geometrycollection":
                    geo = buildGeometryCollection(coordinates);
                    break;
                default:
                    throw new DatatypeFormatException("Geometry type not supported: " + geometryType);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DatatypeFormatException("Build WKT Geometry Exception - Type: " + geometryType + ", Coordinates: " + coordinates + ". " + ex.getMessage());
        }
        return geo;
    }

    private String clean(String unclean) {
        return unclean.replace(")", "").replace("(", "").trim();
    }

    private Geometry buildGeometryCollection(String coordinates) throws DatatypeFormatException {

        if (coordinates.isEmpty()) {
            return GEOMETRY_FACTORY.createGeometryCollection(new Geometry[0]);
        }

        //Split coordinates
        String tidied = coordinates.substring(1, coordinates.length() - 1);
        tidied = tidied.replaceAll("[\\ ]?,[\\ ]?", ","); //Remove spaces around commas
        String[] partCoordinates = tidied.split("\\),(?=[^\\(])"); //Split whenever there is a ), but not ),(

        Geometry[] geometries = new Geometry[partCoordinates.length];

        for (int i = 0; i < partCoordinates.length; i++) {
            WKTReader partWKTInfo = extract(partCoordinates[i]);
            geometries[i] = partWKTInfo.geometry;
        }
        return GEOMETRY_FACTORY.createGeometryCollection(geometries);
    }

    private Geometry buildMultiLineString(String coordinates) {

        if (coordinates.isEmpty()) {
            return GEOMETRY_FACTORY.createMultiLineString(new LineString[0]);
        }

        String[] splitCoordinates = splitCoordinates(coordinates);
        LineString[] lineStrings = splitLineStrings(splitCoordinates);
        return GEOMETRY_FACTORY.createMultiLineString(lineStrings);
    }

    private Geometry buildMultiPolygon(String coordinates) {

        if (coordinates.isEmpty()) {
            return GEOMETRY_FACTORY.createMultiPolygon(new Polygon[0]);
        }

        String trimmed = coordinates.replace(")) ,", ")),");
        String[] multiCoordinates = trimmed.split("\\)\\),");
        Polygon[] polygons = new Polygon[multiCoordinates.length];
        for (int i = 0; i < multiCoordinates.length; i++) {
            polygons[i] = buildPolygon(multiCoordinates[i]);
        }

        return GEOMETRY_FACTORY.createMultiPolygon(polygons);
    }

    private Polygon buildPolygon(String coordinates) {

        Polygon polygon;

        String[] splitCoordinates = splitCoordinates(coordinates);
        if (splitCoordinates.length == 1) { //Polygon without holes.
            CustomCoordinateSequence shellSequence = new CustomCoordinateSequence(dims, clean(coordinates));
            polygon = GEOMETRY_FACTORY.createPolygon(shellSequence);
        } else {    //Polygon with holes
            String shellCoordinates = splitCoordinates[0];

            CustomCoordinateSequence shellSequence = new CustomCoordinateSequence(dims, clean(shellCoordinates));
            LinearRing shellLinearRing = GEOMETRY_FACTORY.createLinearRing(shellSequence);

            String[] splitHoleCoordinates = Arrays.copyOfRange(splitCoordinates, 1, splitCoordinates.length);
            LinearRing[] holesLinearRing = splitLinearRings(dims, splitHoleCoordinates);

            polygon = GEOMETRY_FACTORY.createPolygon(shellLinearRing, holesLinearRing);

        }
        return polygon;
    }

    private String[] splitCoordinates(String coordinates) {

        String trimmed = coordinates.replace(") ,", "),");
        return trimmed.split("\\),");

    }

    private LineString[] splitLineStrings(String[] splitCoordinates) {

        LineString[] lineStrings = new LineString[splitCoordinates.length];

        for (int i = 0; i < splitCoordinates.length; i++) {
            CustomCoordinateSequence sequence = new CustomCoordinateSequence(dims, clean(splitCoordinates[i]));
            LineString lineString = GEOMETRY_FACTORY.createLineString(sequence);
            lineStrings[i] = lineString;
        }

        return lineStrings;

    }

    private LinearRing[] splitLinearRings(CoordinateSequenceDimensions dims, String[] splitCoordinates) {

        LinearRing[] linearRings = new LinearRing[splitCoordinates.length];

        for (int i = 0; i < splitCoordinates.length; i++) {
            CustomCoordinateSequence sequence = new CustomCoordinateSequence(dims, clean(splitCoordinates[i]));
            LinearRing linearRing = GEOMETRY_FACTORY.createLinearRing(sequence);
            linearRings[i] = linearRing;
        }

        return linearRings;

    }

    public static WKTReader extract(String geometryLiteral) throws DatatypeFormatException {

        WKTTextSRS wktTextSRS = new WKTTextSRS(geometryLiteral);

        String srsURI = wktTextSRS.srsURI;
        String wktText = wktTextSRS.wktText;
        String goemetryType = "point";
        String dimension = "";
        String coordinates = "";

        if (!wktText.equals("")) {

            wktText = wktText.trim();
            wktText = wktText.toLowerCase();

            String[] parts = wktText.split("\\(", 2);

            String remainder;
            if (parts.length == 1) { //Check for "empty" keyword and remove.
                remainder = parts[0].replace("empty", "").trim();
            } else {
                int coordinatesStart = wktText.indexOf("(");
                coordinates = wktText.substring(coordinatesStart);
                remainder = parts[0].trim();
            }

            int firstSpace = remainder.indexOf(" ");

            if (firstSpace != -1) {
                goemetryType = remainder.substring(0, firstSpace);
                dimension = remainder.substring(firstSpace + 1);
            } else {
                goemetryType = remainder;
                //dimension = ""; //Dimension already set to empty, but kept as a reminder.
            }
        }

        return new WKTReader(goemetryType, dimension, coordinates, srsURI);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.dims);
        hash = 83 * hash + Objects.hashCode(this.geometry);
        hash = 83 * hash + Objects.hashCode(this.dimensionInfo);
        hash = 83 * hash + Objects.hashCode(this.srsURI);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WKTReader other = (WKTReader) obj;
        if (!Objects.equals(this.srsURI, other.srsURI)) {
            return false;
        }
        if (this.dims != other.dims) {
            return false;
        }
        if (!Objects.equals(this.geometry, other.geometry)) {
            return false;
        }
        return Objects.equals(this.dimensionInfo, other.dimensionInfo);
    }

    @Override
    public String toString() {
        return "WKTReader{" + "dims=" + dims + ", geometry=" + geometry + ", dimensionInfo=" + dimensionInfo + ", srsURI=" + srsURI + '}';
    }

    private static class WKTTextSRS {

        private final String wktText;
        private final String srsURI;

        public WKTTextSRS(String wktLiteral) {
            int startSRS = wktLiteral.indexOf("<");
            int endSRS = wktLiteral.indexOf(">");

            //Check that both chevrons are located and extract SRS_URI name, otherwise default.
            if (startSRS != -1 && endSRS != -1) {
                srsURI = wktLiteral.substring(startSRS + 1, endSRS);
                wktText = wktLiteral.substring(endSRS + 1);

            } else {
                srsURI = SRS_URI.DEFAULT_WKT_CRS84;
                wktText = wktLiteral;
            }
        }

        public String getWktText() {
            return wktText;
        }

        public String getSrsURI() {
            return srsURI;
        }

    }

}
