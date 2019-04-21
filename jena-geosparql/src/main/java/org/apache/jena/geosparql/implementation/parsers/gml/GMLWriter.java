/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.implementation.parsers.gml;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.locationtech.jts.geom.*;

/**
 *
 *
 */
public class GMLWriter {

    public static final Namespace GML_NAMESPACE = Namespace.getNamespace("gml", "http://www.opengis.net/ont/gml");

    private static final XMLOutputter XML_OUTPUT = new XMLOutputter();

    public static String write(GeometryWrapper geometryWrapper) {

        Geometry geometry = geometryWrapper.getParsingGeometry();
        CoordinateSequenceDimensions dimensions = geometryWrapper.getCoordinateSequenceDimensions();
        String srsName = geometryWrapper.getSrsURI();
        Element gmlElement = expand(geometry, dimensions, srsName);
        String output = XML_OUTPUT.outputString(gmlElement);
        return output;
    }

    private static Element expand(final Geometry geometry, final CoordinateSequenceDimensions dimensions, final String srsName) {

        Element gmlElement;
        String srsDimension = convertDimensions(dimensions);
        switch (geometry.getGeometryType()) {
            case "Point":
                Point point = (Point) geometry;
                gmlElement = buildPoint(point.getCoordinateSequence(), srsName);
                break;
            case "LineString":
                LineString lineString = (LineString) geometry;
                gmlElement = buildLineString(lineString.getCoordinateSequence(), srsName);
                break;
            case "Polygon":
                Polygon polygon = (Polygon) geometry;
                gmlElement = buildPolygon(polygon, srsName);
                break;
            case "MultiPoint":
                MultiPoint multiPoint = (MultiPoint) geometry;
                gmlElement = buildMultiPoint(multiPoint, srsName);
                break;
            case "MultiLineString":
                MultiLineString multiLineString = (MultiLineString) geometry;
                gmlElement = buildMultiLineString(multiLineString, srsName);
                break;
            case "MultiPolygon":
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                gmlElement = buildMultiPolygon(multiPolygon, srsDimension, srsName);
                break;
            case "GeometryCollection":
                GeometryCollection geometryCollection = (GeometryCollection) geometry;
                gmlElement = buildMultiGeometry(geometryCollection, dimensions, srsName);
                break;
            default:
                throw new DatatypeFormatException("Geometry type not supported: " + geometry.getGeometryType());

        }
        return gmlElement;
    }

    public static String convertToGMLText(CustomCoordinateSequence coordSequence) {
        StringBuilder sb = new StringBuilder();

        int size = coordSequence.getSize();
        if (size != 0) {
            String coordText = coordSequence.getCoordinateText(0);
            sb.append(coordText);

            for (int i = 1; i < size; i++) {

                sb.append(" ");
                coordText = coordSequence.getCoordinateText(i);
                sb.append(coordText);
            }
        } else {
            sb.append("");
        }

        return sb.toString();

    }

    private static Element buildPoint(final CoordinateSequence coordSeq, final String srsName) {
        CustomCoordinateSequence coordSequence = (CustomCoordinateSequence) coordSeq;

        Element gmlRoot = new Element("Point", GML_NAMESPACE);
        gmlRoot.setAttribute("srsName", srsName);
        if (coordSequence.size() > 0) {
            Element pos = new Element("pos", GML_NAMESPACE);
            pos.addContent(convertToGMLText(coordSequence));
            gmlRoot.addContent(pos);
        }
        return gmlRoot;
    }

    private static Element buildLineString(final CoordinateSequence coordSeq, final String srsName) {
        CustomCoordinateSequence coordSequence = (CustomCoordinateSequence) coordSeq;

        Element gmlRoot = new Element("LineString", GML_NAMESPACE);
        gmlRoot.setAttribute("srsName", srsName);
        if (coordSequence.size() > 0) {
            Element posList = new Element("posList", GML_NAMESPACE);
            posList.addContent(convertToGMLText(coordSequence));
            gmlRoot.addContent(posList);
        }
        return gmlRoot;
    }

    private static Element buildPolygon(final Polygon polygon, final String srsName) {

        Element gmlRoot = new Element(polygon.getGeometryType(), GML_NAMESPACE);
        gmlRoot.setAttribute("srsName", srsName);

        if (!polygon.isEmpty()) {
            LineString lineString = polygon.getExteriorRing();
            CustomCoordinateSequence coordSequence = (CustomCoordinateSequence) lineString.getCoordinateSequence();

            //Find exterior shell
            Element exterior = new Element("exterior", GML_NAMESPACE);
            Element exteriorPosList = new Element("posList", GML_NAMESPACE);
            exteriorPosList.addContent(convertToGMLText(coordSequence));
            exterior.addContent(exteriorPosList);
            gmlRoot.addContent(exterior);

            //Find inner holes
            int interiorRings = polygon.getNumInteriorRing();

            for (int i = 0; i < interiorRings; i++) {
                //flush all content
                Element interior = new Element("interior", GML_NAMESPACE);
                Element innerPosList = new Element("posList", GML_NAMESPACE);
                LineString innerLineString = polygon.getInteriorRingN(i);
                CustomCoordinateSequence innerCoordSequence = (CustomCoordinateSequence) innerLineString.getCoordinateSequence();
                innerPosList.addContent(convertToGMLText(innerCoordSequence));
                interior.addContent(innerPosList);
                gmlRoot.addContent(interior);
            }

        } else {
            //Do nothing to the GML
        }
        return gmlRoot;
    }

    private static Element buildMultiPoint(final MultiPoint multiPoint, final String srsName) {

        Element gmlRoot = new Element(multiPoint.getGeometryType(), GML_NAMESPACE);
        gmlRoot.setAttribute("srsName", srsName);

        if (!multiPoint.isEmpty()) {

            int geomCount = multiPoint.getNumGeometries();
            for (int i = 0; i < geomCount; i++) {
                Element pointMember = new Element("pointMember", GML_NAMESPACE);

                Point point = (Point) multiPoint.getGeometryN(i);
                Element pointElement = buildPoint(point.getCoordinateSequence(), srsName);
                pointMember.addContent(pointElement);
                gmlRoot.addContent(pointMember);
            }

        } else {
            //Do nothing
        }
        return gmlRoot;
    }

    private static Element buildMultiLineString(final MultiLineString multiLineString, final String srsName) {

        //Element gmlRoot = new Element(multiLineString.getGeometryType(), GML_NAMESPACE);
        Element gmlRoot = new Element("MultiCurve", GML_NAMESPACE);
        gmlRoot.setAttribute("srsName", srsName);

        if (!multiLineString.isEmpty()) {

            int geomCount = multiLineString.getNumGeometries();
            for (int i = 0; i < geomCount; i++) {
                //Element lineStringMember = new Element("LineStringMember", GML_NAMESPACE);
                Element lineStringMember = new Element("curveMember", GML_NAMESPACE);

                LineString lineString = (LineString) multiLineString.getGeometryN(i);
                Element lineStringElement = buildLineString(lineString.getCoordinateSequence(), srsName);
                lineStringMember.addContent(lineStringElement);
                gmlRoot.addContent(lineStringMember);
            }

        } else {
            //Do nothing
        }
        return gmlRoot;
    }

    private static Element buildMultiPolygon(final MultiPolygon multiPolygon, final String dimensionString, final String srsName) {

        //Element gmlRoot = new Element(multiPolygon.getGeometryType(), GML_NAMESPACE);
        Element gmlRoot = new Element("MultiSurface", GML_NAMESPACE);
        gmlRoot.setAttribute("srsName", srsName);

        if (!multiPolygon.isEmpty()) {

            int geomCount = multiPolygon.getNumGeometries();
            for (int i = 0; i < geomCount; i++) {
                //Element polygonMember = new Element("PolygonMember", GML_NAMESPACE);
                Element polygonMember = new Element("surfaceMember", GML_NAMESPACE);

                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

                polygonMember.addContent(buildPolygon(polygon, srsName));
                gmlRoot.addContent(polygonMember);
            }

        } else {
            //Do nothing
        }
        return gmlRoot;
    }

    private static Element buildMultiGeometry(final GeometryCollection geometryCollection, final CoordinateSequenceDimensions dimensions, final String srsName) {

        //Element gmlRoot = new Element(geometryCollection.getGeometryType(), GML_NAMESPACE);
        Element gmlRoot = new Element("MultiGeometry", GML_NAMESPACE);
        gmlRoot.setAttribute("srsName", srsName);

        if (!geometryCollection.isEmpty()) {

            int geomCount = geometryCollection.getNumGeometries();
            for (int i = 0; i < geomCount; i++) {
                Element geometryMember = new Element("geometryMember", GML_NAMESPACE);

                Geometry geometry = geometryCollection.getGeometryN(i);
                geometryMember.addContent(expand(geometry, dimensions, srsName));
                gmlRoot.addContent(geometryMember);
            }

        } else {
            //Do nothing
        }
        return gmlRoot;
    }

    private static String convertDimensions(final CoordinateSequenceDimensions dimensions) {

        switch (dimensions) {
            case XYZ:
                return "3";
            case XYZM:
                return "3";
            case XYM:
                return "2";
            default:
                return "2";
        }
    }
}
