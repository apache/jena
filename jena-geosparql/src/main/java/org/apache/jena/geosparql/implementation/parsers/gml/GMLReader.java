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
package org.apache.jena.geosparql.implementation.parsers.gml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.SRSInfoException;
import org.apache.jena.geosparql.implementation.UnitsOfMeasure;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.parsers.ParserReader;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class GMLReader implements ParserReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    //Geometry attributes
    private final Geometry geometry;
    private final String srsURI;
    private final CoordinateSequenceDimensions dims;
    private final DimensionInfo dimensionInfo;

    private static final Namespace GML_NAMESPACE = Namespace.getNamespace("gml", "http://www.opengis.net/ont/gml");

    /**
     * Aiming to achieve SF-0 of GML Simple Features Profile 2.0 [10-100R3].<br>
     * This is based on GML3.2 and limits the set of geometries.<br>
     * <br>
     * [10-100r3], page 22: All point geometries must use {@code <gml:pos>}
     * child element and all other geometries {@code <gml:posList>}. <br>
     * Converting geometries to those found in WKT and supported by JTS: Point,
     * LineString and Polygon.<br>
     * <br>
     * [10-100r3], page 23: The multi-shapes are listed. Multi-shapes have been
     * renamed between GML2 and GML3. MultiPolygon is now MultiSurface and
     * MutliLineString is now MultiCurve.<br>
     * Only X,Y and X,Y,Z coordinate and spatial dimensions supported.<br>
     * [07-036], page 310 states "srsDimension is the dimension of the
     * coordinate reference system as stated in the coordinate reference system
     * definition."<br>
     * [10-100r3], page 22 states "c) coordinate reference systems may have 1, 2
     * or 3 dimensions".
     *
     * At time of implementing: JTS only supports GML1.0 and GML2.0, while
     * Apache SIS is document based and not GML fragments.
     *
     * @see
     * <a href="https://en.wikipedia.org/wiki/Geography_Markup_Language#GML_Simple_Features_Profile"></a>
     * @see
     * <a href="https://portal.opengeospatial.org/files/?artifact_id=42729"></a>
     *
     * @param gmlElement
     * @throws DatatypeFormatException
     */
    protected GMLReader(Element gmlElement) throws DatatypeFormatException, SRSInfoException {
        this.srsURI = getSrsURI(gmlElement);
        SRSInfo srsInfo = new SRSInfo(srsURI);
        CoordinateReferenceSystem crs = srsInfo.getCrs();

        // [07-036], page 56: "The optional attribute srsDimension is the number of coordinate values in a position. This dimension is derived
        // from the coordinate reference system."
        int srsDimension = crs.getCoordinateSystem().getDimension();
        this.dims = CoordinateSequenceDimensions.find(srsDimension);
        String geometryType = gmlElement.getName();

        this.geometry = buildGeometry(geometryType, gmlElement, dims, srsInfo);
        this.dimensionInfo = new DimensionInfo(dims, geometry.getDimension());
    }

    protected GMLReader(Geometry geometry, int srsDimension, String srsURI) {
        this.srsURI = srsURI;
        this.geometry = geometry;
        this.dims = CoordinateSequenceDimensions.find(srsDimension);
        this.dimensionInfo = new DimensionInfo(dims, geometry.getDimension());
    }

    protected GMLReader(Geometry geometry, int srsDimension) {
        this(geometry, srsDimension, SRS_URI.DEFAULT_WKT_CRS84);
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public String getSrsURI() {
        return srsURI;
    }

    @Override
    public CoordinateSequenceDimensions getDimensions() {
        return dims;
    }

    @Override
    public DimensionInfo getDimensionInfo() {
        return dimensionInfo;
    }

    private static Boolean isSRSNameWarningIssued = false;

    private static String getSrsURI(Element gmlElement) {
        String srsNameURI = gmlElement.getAttributeValue("srsName");
        if (srsNameURI == null) {
            srsNameURI = SRS_URI.DEFAULT_WKT_CRS84;
            if (!isSRSNameWarningIssued) {
                LOGGER.warn("GML Literal with no srsName. Defaulting to CRS84 {} used as WKT default SRS. This warning will be issued once.", srsNameURI);
                isSRSNameWarningIssued = true;
            }

        }
        return srsNameURI;
    }

    private static Geometry buildGeometry(String shape, Element gmlElement, CoordinateSequenceDimensions dims, SRSInfo srsInfo) throws DatatypeFormatException {

        /**
         *
         * LineStringSegment
         * (http://www.datypic.com/sc/niem21/e-gml32_LineStringSegment.html) has
         * same structure as LineString
         * (http://www.datypic.com/sc/niem21/e-gml32_LineString.html).<br>
         * <br>
         * PolygonPatch
         * (http://www.datypic.com/sc/niem21/e-gml32_PolygonPatch.html) has same
         * structure as Polygon
         * (http://www.datypic.com/sc/niem21/e-gml32_Polygon.html).
         */
        Geometry geo;
        try {
            switch (shape) {
                case "Point":
                    geo = buildPoint(gmlElement, dims);
                    break;
                case "LineString":
                    geo = buildLineString(gmlElement, dims);
                    break;
                case "Curve":
                    geo = buildCurve(gmlElement, dims, srsInfo);
                    break;
                case "Polygon":
                    geo = buildPolygon(gmlElement, dims);
                    break;
                case "Surface":
                    geo = buildSurface(gmlElement, dims, srsInfo);
                    break;
                case "MultiPoint":
                    geo = buildMultiPoint(gmlElement, dims);
                    break;
                //case "MultiLineString" is deprecated in GML3.2
                case "MultiCurve":
                    geo = buildMultiCurve(gmlElement, dims, srsInfo);
                    break;
                //case "MultiPolygon" is deprecated in GML3.2
                case "MultiSurface":
                    geo = buildMultiSurface(gmlElement, dims, srsInfo);
                    break;
                case "MultiGeometry":
                    geo = buildMultiGeometry(gmlElement, dims, srsInfo);
                    break;
                default:
                    throw new DatatypeFormatException("Geometry shape not supported ([10-100r3], page 22): " + shape);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DatatypeFormatException("Build GML Geometry Exception - Shape: " + shape + ", Element: " + gmlElement + ". " + ex.getMessage());
        }

        return geo;
    }

    private static CustomCoordinateSequence extractPos(Element gmlElement, CoordinateSequenceDimensions dims) {
        String coordinates = gmlElement.getChildTextNormalize("pos", GML_NAMESPACE);
        if (coordinates == null) {
            coordinates = "";
        }
        return new CustomCoordinateSequence(dims, coordinates);
    }

    private static String extractPosList(Element gmlElement, int srsDimension) {
        String posList = gmlElement.getChildTextNormalize("posList", GML_NAMESPACE);
        if (posList == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("");
        String[] coordinates = posList.trim().split(" ");

        int mod = coordinates.length % srsDimension;
        if (mod != 0) {
            throw new DatatypeFormatException("GML Pos List does not divide into srs dimension: " + coordinates.length + " divide " + srsDimension + " remainder " + mod + ".");
        }

        int finalCoordinate = coordinates.length - 1;
        for (int i = 0; i < coordinates.length; i++) {
            if (i != 0 & i % srsDimension == 0) {
                sb.append(",");
            }
            String coordinate = coordinates[i];
            sb.append(coordinate);
            if (i != finalCoordinate) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    private static List<Coordinate> buildCoordinateList(Element gmlElement, int srsDimension) {

        String posList = gmlElement.getChildTextNormalize("posList", GML_NAMESPACE);
        String[] coordinates = posList.trim().split(" ");

        int mod = coordinates.length % srsDimension;
        if (mod != 0) {
            throw new DatatypeFormatException("GML Pos List does not divide into srs dimension: " + coordinates.length + " divide " + srsDimension + " remainder " + mod + ".");
        }

        List<Coordinate> coordinateList = new ArrayList<>();

        for (int i = 0; i < coordinates.length; i += srsDimension) {
            Coordinate coord;
            //[10-100rs], page 22: "c) coordinate reference systems may have 1, 2 or 3 dimensions". 1 dimension does not fit with spatial relations of GeoSPARQL.
            switch (srsDimension) {
                case 2:
                    coord = new CoordinateXY(Double.parseDouble(coordinates[i]), Double.parseDouble(coordinates[i + 1]));
                    break;
                case 3:
                    coord = new Coordinate(Double.parseDouble(coordinates[i]), Double.parseDouble(coordinates[i + 1]), Double.parseDouble(coordinates[i + 2]));
                    break;
                default:
                    throw new DatatypeFormatException("SRS dimension " + srsDimension + " is not supported.");
            }
            coordinateList.add(coord);
        }

        return coordinateList;
    }

    private static Point buildPoint(Element gmlElement, CoordinateSequenceDimensions dims) {
        CustomCoordinateSequence coordinateSequence = extractPos(gmlElement, dims);
        return GEOMETRY_FACTORY.createPoint(coordinateSequence);
    }

    private static LineString buildLineString(Element gmlElement, CoordinateSequenceDimensions dims) {
        int srsDimension = CoordinateSequenceDimensions.convertToInt(dims);
        String posList = extractPosList(gmlElement, srsDimension);
        CustomCoordinateSequence coordinateSequence = new CustomCoordinateSequence(dims, posList);
        return GEOMETRY_FACTORY.createLineString(coordinateSequence);
    }

    /**
     * Curve has one or more LineStringSegments that have connecting points.
     * http://www.datypic.com/sc/niem21/e-gml32_Curve.html <br>
     * "The curve segments are connected to one another, with the end point of
     * each segment except the last being the start point of the next segment in
     * the segment list."<br>
     * [07-036], page 22: "gml:Curve with gml:LineStringSegment, gml:Arc,
     * gml:Circle or gml:CircleByCenterPoint segments1."
     *
     * @param gmlElement
     * @param srsDimension
     * @return
     */
    private static LineString buildCurve(Element gmlElement, CoordinateSequenceDimensions dims, SRSInfo srsInfo) {
        //TODO Try using: GeometricShapeFactory gsf = new GeometricShapeFactory();
        //TODO Arc: three points that describe - centre and angles?
        //TODO Circle: three points that describe - centre and angles?
        //TODO Add methods to GeometryWrapperFactory.createGMLArc, createGMLCircle, createGMLCircleByCentrePoint.

        //LineStringSegements
        Element segmentsElement = gmlElement.getChild("segments", GML_NAMESPACE);
        if (segmentsElement == null) {
            return GEOMETRY_FACTORY.createLineString();
        }

        List<Element> segments = segmentsElement.getChildren("LineStringSegment", GML_NAMESPACE);
        if (!segments.isEmpty()) {
            return buildLineStringSegments(segments, dims);
        }

        segments = segmentsElement.getChildren("Arc", GML_NAMESPACE);
        if (!segments.isEmpty()) {
            return buildArc(segments, dims);
        }

        segments = segmentsElement.getChildren("Circle", GML_NAMESPACE);
        if (!segments.isEmpty()) {
            return buildCircle(segments, dims);
        }

        segments = segmentsElement.getChildren("CircleByCenterPoint", GML_NAMESPACE);
        if (!segments.isEmpty()) {
            return buildCircleByCentrePoint(segments, dims, srsInfo);
        } else {
            throw new DatatypeFormatException("GML Curve segments not supported or empty: " + gmlElement);
        }

    }

    private static LineString buildLineStringSegments(List<Element> segments, CoordinateSequenceDimensions dims) {

        int srsDimension = CoordinateSequenceDimensions.convertToInt(dims);
        List<Coordinate> lineStringCoords = new ArrayList<>();
        for (Element segment : segments) {

            List<Coordinate> segmentCoords = buildCoordinateList(segment, srsDimension);
            if (lineStringCoords.isEmpty()) {
                lineStringCoords = segmentCoords;
            } else {
                if (!segmentCoords.isEmpty()) {
                    Coordinate lastCoord = lineStringCoords.get(lineStringCoords.size() - 1);
                    Coordinate firstCoord = segmentCoords.get(0);

                    if (!firstCoord.equals2D(lastCoord)) {
                        throw new DatatypeFormatException("GML LineString segments do not have matching last and first coordinates: " + lineStringCoords + " - " + segmentCoords);
                    }

                    segmentCoords.remove(0);
                    lineStringCoords.addAll(segmentCoords);
                }
            }

        }
        CustomCoordinateSequence coordinateSequence = new CustomCoordinateSequence(dims, lineStringCoords);
        return GEOMETRY_FACTORY.createLineString(coordinateSequence);
    }

    private static final LineString buildArc(List<Element> segments, CoordinateSequenceDimensions dims) {
        int srsDimension = CoordinateSequenceDimensions.convertToInt(dims);

        Element posListElement = segments.get(0); //Already ensured that non-zero length. gml:Arc only has single posList.
        List<Coordinate> coordinates = buildCoordinateList(posListElement, srsDimension);
        if (coordinates.size() != 3) {
            throw new DatatypeFormatException("GML Arc posList does not contain 3 coordinates: " + posListElement);
        }

        Coordinate centre = findCentre(coordinates);
        Coordinate coord0 = coordinates.get(0);
        double radius = Math.hypot(centre.x - coord0.x, centre.y - coord0.y); //All coordinates on the arc are radius distance from centre point.

        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory(GEOMETRY_FACTORY);
        geometricShapeFactory.setCentre(centre);
        geometricShapeFactory.setWidth(radius * 2);

        double startAng = findAngle(centre, coord0);
        double angExtent = findAngle(centre, coordinates.get(2));
        return geometricShapeFactory.createArc(startAng, angExtent);
    }

    protected static Coordinate findCentre(List<Coordinate> coordinates) {
        if (coordinates.size() < 3) {
            throw new DatatypeFormatException("GML posList does not contain 3 coordinates: " + coordinates);
        }

        double x1 = coordinates.get(0).x;
        double y1 = coordinates.get(0).y;
        double x2 = coordinates.get(1).x;
        double y2 = coordinates.get(1).y;
        double x3 = coordinates.get(2).x;
        double y3 = coordinates.get(2).y;

        double A = (x1 * (y2 - y3)) - (y1 * (x2 - x3)) + (x2 * y3) - (x3 * y2);
        double B = (((x1 * x1) + (y1 * y1)) * (y3 - y2)) + (((x2 * x2) + (y2 * y2)) * (y1 - y3)) + (((x3 * x3) + (y3 * y3)) * (y2 - y1));
        double C = (((x1 * x1) + (y1 * y1)) * (x2 - x3)) + (((x2 * x2) + (y2 * y2)) * (x3 - x1)) + (((x3 * x3) + (y3 * y3)) * (x1 - x2));

        double x = -(B / (2 * A));
        double y = -(C / (2 * A));
        Coordinate centre = new Coordinate(x, y);
        return centre;
    }

    protected static double findAngle(Coordinate coord0, Coordinate coord1) {

        LineSegment line = new LineSegment(coord0, coord1);
        double angle = line.angle();
        return angle;
    }

    private static LineString buildCircle(List<Element> segments, CoordinateSequenceDimensions dims) {
        int srsDimension = CoordinateSequenceDimensions.convertToInt(dims);

        Element posListElement = segments.get(0); //Already ensured that non-zero length. gml:Circle only has single posList.
        List<Coordinate> coordinates = buildCoordinateList(posListElement, srsDimension);
        if (coordinates.size() != 3) {
            throw new DatatypeFormatException("GML Circle posList does not contain 3 coordinates: " + posListElement);
        }

        Coordinate centre = findCentre(coordinates);
        Coordinate coord = coordinates.get(0);
        double radius = Math.hypot(centre.x - coord.x, centre.y - coord.y);

        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory(GEOMETRY_FACTORY);
        geometricShapeFactory.setCentre(centre);
        geometricShapeFactory.setWidth(radius * 2);
        Polygon circlePolygon = geometricShapeFactory.createCircle();
        return circlePolygon.getExteriorRing();
    }

    private static LineString buildCircleByCentrePoint(List<Element> segments, CoordinateSequenceDimensions dims, SRSInfo srsInfo) {

        Element circleElement = segments.get(0);
        Point point = buildPoint(circleElement, dims);
        Coordinate centre = point.getCoordinate();

        //Get radius and convert units of measure if required.
        Element radiusElement = circleElement.getChild("radius", GML_NAMESPACE);
        double radius = Double.parseDouble(radiusElement.getTextNormalize());

        //Extract units of measure uri. Use SRS units of measure if absent.
        String uomURI = radiusElement.getAttributeValue("uom");

        if (uomURI != null) {
            UnitsOfMeasure uom = new UnitsOfMeasure(uomURI);
            //Convert the radius if the specified units of measure don't match.
            radius = UnitsOfMeasure.conversion(radius, uom, srsInfo.getUnitsOfMeasure());
        }

        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory(GEOMETRY_FACTORY);
        geometricShapeFactory.setCentre(centre);
        geometricShapeFactory.setSize(radius * 2);  //Set the width and height, i.e. the diameter.
        Polygon circlePolygon = geometricShapeFactory.createCircle();
        return circlePolygon.getExteriorRing();
    }

    private static Polygon buildPolygon(Element gmlElement, CoordinateSequenceDimensions dims) {

        Polygon polygon;

        //Exterior shell - [0..1]
        Element exteriorElement = gmlElement.getChild("exterior", GML_NAMESPACE);
        LinearRing exteriorLinearRing;
        if (exteriorElement != null) {
            Element exteriorLinearRingElement = exteriorElement.getChild("LinearRing", GML_NAMESPACE);
            exteriorLinearRing = buildLinearRing(exteriorLinearRingElement, dims);
        } else {
            exteriorLinearRing = GEOMETRY_FACTORY.createLinearRing();
        }
        //Interior shell - [0..*]
        List<Element> interiorElements = gmlElement.getChildren("interior", GML_NAMESPACE);
        List<LinearRing> interiorList = new ArrayList<>();
        for (Element interiorElement : interiorElements) {
            Element interiorLinearRingElement = interiorElement.getChild("LinearRing", GML_NAMESPACE);
            LinearRing linearRing = buildLinearRing(interiorLinearRingElement, dims);
            interiorList.add(linearRing);
        }

        //Build the polygon depending on whether interior shells were found.
        if (interiorList.isEmpty()) {
            polygon = GEOMETRY_FACTORY.createPolygon(exteriorLinearRing);
        } else {
            LinearRing[] interiorLinearRings = interiorList.toArray(new LinearRing[interiorList.size()]);
            polygon = GEOMETRY_FACTORY.createPolygon(exteriorLinearRing, interiorLinearRings);
        }

        return polygon;
    }

    private static LinearRing buildLinearRing(Element gmlElement, CoordinateSequenceDimensions dims) {
        int srsDimension = CoordinateSequenceDimensions.convertToInt(dims);
        String posList = extractPosList(gmlElement, srsDimension);
        CustomCoordinateSequence sequence = new CustomCoordinateSequence(dims, posList);
        LinearRing linearRing = GEOMETRY_FACTORY.createLinearRing(sequence);
        return linearRing;
    }

    private static Polygon buildSurface(Element gmlElement, CoordinateSequenceDimensions dims, SRSInfo srsInfo) {

        //http://www.datypic.com/sc/niem21/e-gml32_patches.html
        //gml:patches [1..1]
        Element patchesElement = gmlElement.getChild("patches", GML_NAMESPACE);
        if (patchesElement == null) {
            throw new DatatypeFormatException("GML Surface does not contain patches child: " + gmlElement);
        }

        //PolygonPatches [0..*]
        List<Element> polygonPatches = patchesElement.getChildren("PolygonPatch", GML_NAMESPACE);
        if (polygonPatches.isEmpty()) {
            throw new DatatypeFormatException("GML Surface does not contain PolygonPatches. Only gml:PolygonPatches are supported in Simple Features profile ([10-100r3], page 22):" + gmlElement);
        }

        List<Polygon> polys = new ArrayList<>();
        for (Element patch : polygonPatches) {

            LinearRing exteriorLinearRing;
            //Exterior shell - [0..1]
            Element exteriorElement = patch.getChild("exterior", GML_NAMESPACE);
            if (exteriorElement != null) {
                Geometry exteriorGeom = buildSurfacePatch(exteriorElement, dims, srsInfo);
                exteriorLinearRing = GEOMETRY_FACTORY.createLinearRing(exteriorGeom.getCoordinates());
            } else {
                exteriorLinearRing = GEOMETRY_FACTORY.createLinearRing();
            }

            //Interior shell - [0..*]
            List<Element> interiorElements = gmlElement.getChildren("interior", GML_NAMESPACE);
            List<LinearRing> interiorList = new ArrayList<>();
            for (Element interiorElement : interiorElements) {
                Geometry interiorGeom = buildSurfacePatch(interiorElement, dims, srsInfo);
                LinearRing interiorLinearRing = GEOMETRY_FACTORY.createLinearRing(interiorGeom.getCoordinates());
                interiorList.add(interiorLinearRing);
            }

            //Build the polygon depending on whether interior shells were found.
            Polygon polygon;
            if (interiorList.isEmpty()) {
                polygon = GEOMETRY_FACTORY.createPolygon(exteriorLinearRing);
            } else {
                LinearRing[] interiorLinearRings = interiorList.toArray(new LinearRing[interiorList.size()]);
                polygon = GEOMETRY_FACTORY.createPolygon(exteriorLinearRing, interiorLinearRings);
            }
            polys.add(polygon);
        }

        //Unionise all the polygons on the surface together.
        Geometry unionGeom = CascadedPolygonUnion.union(polys);
        Polygon unionPolygon;
        if (unionGeom instanceof Polygon) {
            unionPolygon = (Polygon) unionGeom;
        } else {
            throw new AssertionError("CascadePolygonUnion has not produced a Polygon geometry in GML Reader.");
        }

        return unionPolygon;
    }

    private static LinearRing buildSurfacePatch(Element gmlElement, CoordinateSequenceDimensions dims, SRSInfo srsInfo) {
        Element linearRingElement = gmlElement.getChild("LinearRing", GML_NAMESPACE);
        LinearRing linearRing = null;
        if (linearRingElement != null) {
            //LinearRing [1..1]
            linearRing = buildLinearRing(linearRingElement, dims);
        } else {
            //Ring [1..1] element containing curveMember [1..1] and then Curve [1..1]
            Element ringElement = gmlElement.getChild("Ring", GML_NAMESPACE);
            if (ringElement == null) {
                Element curveMemberElement = gmlElement.getChild("curveMember", GML_NAMESPACE);
                if (curveMemberElement != null) {
                    Element curveElement = curveMemberElement.getChild("Curve", GML_NAMESPACE);
                    if (curveElement != null) {
                        LineString lineString = buildCurve(curveElement, dims, srsInfo);
                        linearRing = GEOMETRY_FACTORY.createLinearRing(lineString.getCoordinateSequence());
                    }
                }
            }
        }
        if (linearRing == null) {
            throw new DatatypeFormatException("GML Surface does not contain correct LinearRing or Ring elements ([10-100r3], page 22):" + gmlElement);
        }
        return linearRing;
    }

    private static Geometry buildMultiPoint(Element gmlElement, CoordinateSequenceDimensions dims) {

        List<Element> memberElements = getMembers(gmlElement, "pointMember");
        List<Point> points = new ArrayList<>(memberElements.size());

        for (Element member : memberElements) {
            Point point = buildPoint(member, dims);
            points.add(point);
        }

        Point[] pointArray = points.toArray(new Point[points.size()]);
        return GEOMETRY_FACTORY.createMultiPoint(pointArray);
    }

    private static Geometry buildMultiCurve(Element gmlElement, CoordinateSequenceDimensions dims, SRSInfo srsInfo) {

        List<Element> memberElements = getMembers(gmlElement, "curveMember");
        List<LineString> lineStrings = new ArrayList<>(memberElements.size());

        for (Element member : memberElements) {
            String shape = member.getName();
            LineString lineString;
            switch (shape) {
                case "LineString":
                    lineString = buildLineString(member, dims);
                    break;
                case "Curve":
                    lineString = buildCurve(member, dims, srsInfo);
                    break;
                default:
                    throw new DatatypeFormatException("GML MultiCurve does not contain LineString or Curve elements ([10-100r3], page 22):" + gmlElement);
            }
            lineStrings.add(lineString);
        }

        LineString[] lineStringArray = lineStrings.toArray(new LineString[lineStrings.size()]);
        return GEOMETRY_FACTORY.createMultiLineString(lineStringArray);
    }

    private static Geometry buildMultiSurface(Element gmlElement, CoordinateSequenceDimensions dims, SRSInfo srsInfo) {

        List<Element> memberElements = getMembers(gmlElement, "surfaceMember");
        List<Polygon> polygons = new ArrayList<>(memberElements.size());

        for (Element member : memberElements) {
            String shape = member.getName();
            Polygon polygon;
            switch (shape) {
                case "Polygon":
                    polygon = buildPolygon(member, dims);
                    break;
                case "Surface":
                    polygon = buildSurface(member, dims, srsInfo);
                    break;
                default:
                    throw new DatatypeFormatException("GML MultiSurface does not contain Polygon or Surface elements ([10-100r3], page 22):" + gmlElement);
            }
            polygons.add(polygon);
        }

        Polygon[] polygonArray = polygons.toArray(new Polygon[polygons.size()]);
        return GEOMETRY_FACTORY.createMultiPolygon(polygonArray);
    }

    private static Geometry buildMultiGeometry(Element gmlElement, CoordinateSequenceDimensions dims, SRSInfo srsInfo) {

        List<Element> memberElements = getMembers(gmlElement, "geometryMember");
        List<Geometry> geometries = new ArrayList<>(memberElements.size());

        for (Element member : memberElements) {
            String shape = member.getName();
            Geometry geom = buildGeometry(shape, member, dims, srsInfo);
            geometries.add(geom);
        }

        Geometry[] geometryArray = geometries.toArray(new Geometry[geometries.size()]);
        return GEOMETRY_FACTORY.createGeometryCollection(geometryArray);
    }

    private static List<Element> getMembers(Element gmlElement, String memberLabel) {

        String membersLabel = memberLabel + "s";    //All the members labels are consistent format.
        List<Element> memberElements;
        Element membersElement = gmlElement.getChild(membersLabel, GML_NAMESPACE);
        if (membersElement != null) {
            memberElements = membersElement.getChildren();
        } else {
            List<Element> memberElementList = gmlElement.getChildren(memberLabel, GML_NAMESPACE);
            memberElements = new ArrayList<>();

            for (Element memberElement : memberElementList) {
                List<Element> childElements = memberElement.getChildren();   //Should only be one child which is the geometry.
                memberElements.addAll(childElements);
            }
        }
        return memberElements;
    }

    private static final String EMPTY_GML_TEXT = "<gml:Point xmlns:gml='http://www.opengis.net/ont/gml' srsName=\"http://www.opengis.net/def/crs/OGC/1.3/CRS84\" />";

    public static GMLReader extract(String gmlText) throws JDOMException, IOException {

        if (gmlText.isEmpty()) {
            gmlText = EMPTY_GML_TEXT;
        }

        SAXBuilder jdomBuilder = new SAXBuilder();
        InputStream stream = new ByteArrayInputStream(gmlText.getBytes("UTF-8"));
        Document xmlDoc = jdomBuilder.build(stream);

        Element gmlElement = xmlDoc.getRootElement();

        return new GMLReader(gmlElement);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.geometry);
        hash = 89 * hash + Objects.hashCode(this.srsURI);
        hash = 89 * hash + Objects.hashCode(this.dims);
        hash = 89 * hash + Objects.hashCode(this.dimensionInfo);
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
        final GMLReader other = (GMLReader) obj;
        if (!Objects.equals(this.srsURI, other.srsURI)) {
            return false;
        }
        if (!Objects.equals(this.geometry, other.geometry)) {
            return false;
        }
        if (this.dims != other.dims) {
            return false;
        }
        return Objects.equals(this.dimensionInfo, other.dimensionInfo);
    }

    @Override
    public String toString() {
        return "GMLReader{" + "geometry=" + geometry + ", srsURI=" + srsURI + ", dims=" + dims + ", dimensionInfo=" + dimensionInfo + '}';
    }

}
