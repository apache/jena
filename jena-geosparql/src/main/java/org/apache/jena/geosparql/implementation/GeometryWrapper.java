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

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.great_circle.CoordinatePair;
import org.apache.jena.geosparql.implementation.great_circle.GreatCircleDistance;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex.GeometryIndex;
import org.apache.jena.geosparql.implementation.index.GeometryTransformIndex;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.registry.MathTransformRegistry;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.implementation.registry.UnitsRegistry;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.sis.geometry.DirectPosition2D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class GeometryWrapper implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DimensionInfo dimensionInfo;
    private final SRSInfo srsInfo;
    private final Geometry xyGeometry;
    private final Geometry parsingGeometry;
    private PreparedGeometry preparedGeometry;
    private Envelope envelope;
    private Geometry translateXYGeometry;
    private final String geometryDatatypeURI;
    private GeometryDatatype geometryDatatype;
    private String lexicalForm;
    private String utmURI = null;
    private Double latitude = null;

    /**
     *
     * @param geometry In X/Y or Y/X coordinate order of the SRS URI.
     * @param srsURI
     * @param geometryDatatypeURI
     * @param dimensionInfo
     */
    public GeometryWrapper(Geometry geometry, String srsURI, String geometryDatatypeURI, DimensionInfo dimensionInfo) {
        this(geometry, srsURI, geometryDatatypeURI, dimensionInfo, null);
    }

    /**
     *
     * @param geometry In X/Y or Y/X coordinate order of the SRS URI.
     * @param srsURI
     * @param geometryDatatypeURI
     * @param dimensionInfo
     * @param geometryLiteral
     */
    public GeometryWrapper(Geometry geometry, String srsURI, String geometryDatatypeURI, DimensionInfo dimensionInfo, String geometryLiteral) {
        this(geometry, GeometryReverse.check(geometry, srsURI.isEmpty() ? SRS_URI.DEFAULT_WKT_CRS84 : srsURI), srsURI.isEmpty() ? SRS_URI.DEFAULT_WKT_CRS84 : srsURI, geometryDatatypeURI, dimensionInfo, geometryLiteral);
    }

    protected GeometryWrapper(Geometry parsingGeometry, Geometry xyGeometry, String srsURI, String geometryDatatypeURI, DimensionInfo dimensionInfo) {
        this(parsingGeometry, xyGeometry, srsURI, geometryDatatypeURI, dimensionInfo, null);
    }

    protected GeometryWrapper(Geometry parsingGeometry, Geometry xyGeometry, String srsURI, String geometryDatatypeURI, DimensionInfo dimensionInfo, String lexicalForm) {

        this.parsingGeometry = parsingGeometry;
        this.xyGeometry = xyGeometry;
        this.preparedGeometry = null; //Initialised when required by spatial relations checkPreparedGeometry.
        this.envelope = null; //Initialised when required by getEnvelope().
        this.translateXYGeometry = null; //Initialised when required by translateGeometry().
        this.geometryDatatypeURI = geometryDatatypeURI;
        this.geometryDatatype = null; //Inilialised when required by getGeometryDatatype().

        if (srsURI.isEmpty()) {
            srsURI = SRS_URI.DEFAULT_WKT_CRS84;
        }

        this.srsInfo = SRSRegistry.getSRSInfo(srsURI);

        this.dimensionInfo = dimensionInfo;
        this.lexicalForm = lexicalForm; //If not Initialised then required by asLiteral() etc.
    }

    /**
     * Default to WGS84 geometry and XY coordinate dimensions.
     *
     * @param geometry In X/Y or Y/X coordinate order of WGS84.
     * @param geometryDatatypeURI
     */
    public GeometryWrapper(Geometry geometry, String geometryDatatypeURI) {
        this(geometry, "", geometryDatatypeURI, DimensionInfo.XY_POINT);
    }

    /**
     * Default to XY coordinate dimensions.
     *
     * @param geometry In X/Y or Y/X coordinate order of the SRS URI.
     * @param srsURI
     * @param geometryDatatypeURI
     */
    public GeometryWrapper(Geometry geometry, String srsURI, String geometryDatatypeURI) {
        this(geometry, srsURI, geometryDatatypeURI, DimensionInfo.XY_POINT);
    }

    /**
     * Empty geometry with specified parameters.
     *
     * @param srsURI
     * @param geometryDatatypeURI
     */
    public GeometryWrapper(String srsURI, String geometryDatatypeURI) {
        this(new CustomCoordinateSequence(DimensionInfo.XY_POINT.getDimensions()), geometryDatatypeURI, srsURI);
    }

    /**
     * Point geometry with specified SRS.
     *
     * @param pointCoordinateSequence
     * @param geometryDatatypeURI
     * @param srsURI
     */
    public GeometryWrapper(CustomCoordinateSequence pointCoordinateSequence, String geometryDatatypeURI, String srsURI) {
        this(CustomGeometryFactory.theInstance().createPoint(pointCoordinateSequence), srsURI, geometryDatatypeURI, DimensionInfo.XY_POINT);
    }

    /**
     * Copy GeometryWrapper.
     *
     * @param geometryWrapper
     */
    public GeometryWrapper(GeometryWrapper geometryWrapper) {

        this.xyGeometry = geometryWrapper.xyGeometry;
        this.parsingGeometry = geometryWrapper.parsingGeometry;
        this.preparedGeometry = geometryWrapper.preparedGeometry;
        this.envelope = geometryWrapper.envelope;
        this.translateXYGeometry = geometryWrapper.translateXYGeometry;
        this.utmURI = geometryWrapper.utmURI;
        this.latitude = geometryWrapper.latitude;
        this.geometryDatatypeURI = geometryWrapper.geometryDatatypeURI;
        this.geometryDatatype = geometryWrapper.geometryDatatype;

        this.srsInfo = geometryWrapper.srsInfo;
        this.dimensionInfo = geometryWrapper.dimensionInfo;
        this.lexicalForm = geometryWrapper.lexicalForm;
    }

    /**
     * Transforms, if necessary, the provided target GeometryWrapper according
     * to this GeometryWrapper SRS_URI.
     *
     * @param targetGeometryWrapper
     * @return GeometryWrapper after transformation.
     * @throws FactoryException
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    public GeometryWrapper checkTransformSRS(GeometryWrapper targetGeometryWrapper) throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper transformedGeometryWrapper;
        String srsURI = srsInfo.getSrsURI();
        if (srsURI.equals(targetGeometryWrapper.srsInfo.getSrsURI())) {
            transformedGeometryWrapper = targetGeometryWrapper;
        } else {
            if (!GeoSPARQLConfig.ALLOW_GEOMETRY_SRS_TRANSFORMATION) {
                throw new TransformException("GeometryLiteral SRS transformation is disabled, see GeoSPARQLConfig.allowGeometrySRSTransformation(...).");
            }

            transformedGeometryWrapper = targetGeometryWrapper.transform(srsURI);
        }

        return transformedGeometryWrapper;
    }

    /**
     * Transform the GeometryWrapper into another spatial reference system.<br>
     *
     * @param srsURI
     * @return New GeometryWrapper after transformation, or this GeometryWrapper
     * if no transformation.
     * @throws MismatchedDimensionException
     * @throws TransformException
     * @throws FactoryException
     */
    public GeometryWrapper transform(String srsURI) throws MismatchedDimensionException, TransformException, FactoryException {
        return transform(srsURI, true);
    }

    /**
     * Transform the GeometryWrapper into another spatial reference system.<br>
     *
     * @param srsInfo
     * @return New GeometryWrapper after transformation, or this GeometryWrapper
     * if no transformation.
     * @throws MismatchedDimensionException
     * @throws TransformException
     * @throws FactoryException
     */
    public GeometryWrapper transform(SRSInfo srsInfo) throws MismatchedDimensionException, TransformException, FactoryException {
        return transform(srsInfo.getSrsURI(), true);
    }

    /**
     * Transform the GeometryWrapper into another spatial reference system.<br>
     * Option to store the resulting GeometryWrapper in the index.
     *
     * @param srsURI
     * @param storeSRSTransform
     * @return GeometryWrapper after transformation.
     * @throws MismatchedDimensionException
     * @throws TransformException
     * @throws FactoryException
     */
    protected GeometryWrapper transform(String srsURI, Boolean storeSRSTransform) throws MismatchedDimensionException, TransformException, FactoryException {
        if (srsInfo.getSrsURI().equals(srsURI)) {
            return this;
        }

        return GeometryTransformIndex.transform(this, srsURI, storeSRSTransform);
    }

    /**
     * Checks whether the prepared geometry has been initialised.
     * <br>Done lazily as expensive.
     */
    private void checkPreparedGeometry() {
        if (preparedGeometry == null) {
            this.preparedGeometry = PreparedGeometryFactory.prepare(xyGeometry);
        }
    }

    /**
     * Returns this geometry wrapper converted to the SRS_URI URI.
     *
     * @param srsURI
     * @return GeometryWrapper after conversion.
     * @throws FactoryException
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    public GeometryWrapper convertSRS(String srsURI) throws FactoryException, MismatchedDimensionException, TransformException {
        return transform(srsURI);
    }

    /**
     *
     * @return Coordinate/Spatial reference system of the GeometryWrapper.
     */
    public CoordinateReferenceSystem getCRS() {
        return srsInfo.getCrs();
    }

    /**
     *
     * @return Geometry with coordinates in x,y order, regardless of SRS_URI.
     */
    public Geometry getXYGeometry() {
        return xyGeometry;
    }

    /**
     *
     * @return Geometry with coordinates as originally provided.
     */
    public Geometry getParsingGeometry() {
        return parsingGeometry;
    }

    /**
     * XY geometry translated by the domain range of the SRS, if a Geographic
     * SRS.<br>
     * Returns XY geometry if not a Geographic SRS.
     *
     * @return Geometry after translation in X direction.
     */
    public Geometry translateXYGeometry() {

        if (translateXYGeometry == null) {

            if (srsInfo.isGeographic()) {
                double xTranslate = srsInfo.getDomainRangeX();
                AffineTransformation translation = AffineTransformation.translationInstance(xTranslate, 0);
                translateXYGeometry = translation.transform(xyGeometry); //Translate seems to be copying Y values into Z and M.
            } else {
                translateXYGeometry = xyGeometry;
            }

        }

        return translateXYGeometry;
    }

    /**
     *
     * @return Coordinate/Spatial reference system URI.
     */
    public String getSrsURI() {
        return srsInfo.getSrsURI();
    }

    /**
     *
     * @return getSRID used in GeoSPARQL Standard page 22 to refer to srsURI.
     * i.e. getSrsURI and getSRID are the same.
     */
    public String getSRID() {
        return srsInfo.getSrsURI();
    }

    /**
     *
     * @return SRS information that the Geometry Wrapper is using.
     */
    public SRSInfo getSrsInfo() {
        return srsInfo;
    }

    /**
     *
     * @return Whether the SRS URI has been recognised. Operations may fail or
     * not perform correctly when false.
     */
    public Boolean isSRSRecognised() {
        return srsInfo.isSRSRecognised();
    }

    /**
     *
     * @return Datatype URI of the literal.
     */
    public String getGeometryDatatypeURI() {
        return geometryDatatypeURI;
    }

    /**
     *
     * @return The name of the parsing Geometry's actual class.
     */
    public String getGeometryType() {
        return parsingGeometry.getGeometryType();
    }

    /**
     *
     * @return GeometryDatatype of the literal.
     */
    public GeometryDatatype getGeometryDatatype() {

        if (geometryDatatype == null) {
            geometryDatatype = GeometryDatatype.get(geometryDatatypeURI);
        }
        return geometryDatatype;
    }

    /**
     * Transformations between SRS and Units will increase the inaccuracy of the
     * results.
     *
     * @param distance
     * @param targetDistanceUnitsURI
     * @return Buffer around GeometryWrapper according the provided distance.
     * @throws FactoryException
     * @throws MismatchedDimensionException
     * @throws TransformException
     * @throws UnitsConversionException
     */
    public GeometryWrapper buffer(double distance, String targetDistanceUnitsURI) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        //Check whether the source geometry is linear units for cartesian calculation. If not then transform to relevant UTM SRS GeometryWrapper.
        Boolean isTargetUnitsLinear = UnitsRegistry.isLinearUnits(targetDistanceUnitsURI);

        if (srsInfo.getUnitsOfMeasure().isLinearUnits() != isTargetUnitsLinear) {
            //Source geometry and target units are not the same.
            throw new UnitsConversionException("Buffer does not support conversion between linear and non-linear units.");
        }

        //Transform distance into current units.
        double transformedDistance = UnitsOfMeasure.conversion(distance, targetDistanceUnitsURI, this.srsInfo.getUnitsOfMeasure().getUnitURI());

        //Buffer the geometry by the transformed distance.
        Geometry xyGeo = this.xyGeometry.buffer(transformedDistance);
        DimensionInfo bufferedDimensionInfo = new DimensionInfo(dimensionInfo.getCoordinate(), dimensionInfo.getSpatial(), xyGeo.getDimension());
        Geometry parsingGeo = GeometryReverse.check(xyGeo, this.srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, this.srsInfo.getSrsURI(), this.geometryDatatypeURI, bufferedDimensionInfo);
    }

    /**
     *
     * @return URI of the GeometryWrapper's UTM zone
     * @throws FactoryException
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    public String getUTMZoneURI() throws FactoryException, MismatchedDimensionException, TransformException {

        if (utmURI == null) {

            //Find a point in the parsing geometry so can directly apply the SRS.
            Point coord = parsingGeometry.getCentroid();
            DirectPosition2D point = new DirectPosition2D(coord.getX(), coord.getY());

            //Convert to WGS84. Use WGS84 and not CRS84 as assuming WGS8 is more prevalent.
            CoordinateReferenceSystem wgs84CRS = SRSRegistry.getCRS(SRS_URI.WGS84_CRS);
            MathTransform transform = MathTransformRegistry.getMathTransform(srsInfo.getCrs(), wgs84CRS);
            
            DirectPosition wgs84Point = transform.transform(point, null);
            
            //Find the UTM zone.
            utmURI = SRSRegistry.findUTMZoneURIFromWGS84(wgs84Point.getOrdinate(0), wgs84Point.getOrdinate(1));

        }
        return utmURI;
    }

    /**
     * Latitude if Geographic SRS or in WGS84.<br>
     * Used to convert between linear and non-linear units of measure.
     *
     * @return Latitude of Geometry.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public Double getLatitude() throws FactoryException, MismatchedDimensionException, TransformException {

        if (latitude == null) {
            GeometryWrapper geoGeometryWrapper;

            if (srsInfo.isGeographic()) {
                //Already a geographic SRS.
                geoGeometryWrapper = this;
            } else {
                //Use WGS84 and not CRS84 as assuming WGS8 is more prevalent.
                geoGeometryWrapper = convertSRS(SRS_URI.WGS84_CRS);
            }

            //Latitude is Y-axis.
            Geometry geometry = geoGeometryWrapper.getXYGeometry();
            Point point = geometry.getCentroid();
            latitude = point.getY();

        }
        return latitude;
    }

    /**
     * Distance (Euclidean) defaulting to metres.
     *
     * @param targetGeometry
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distanceEuclidean(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {
        return distanceEuclidean(targetGeometry, Unit_URI.METRE_URL);
    }

    /**
     * Distance (Euclidean) in the Units of Measure.
     *
     * @param targetGeometry
     * @param unitsOfMeasure
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distanceEuclidean(GeometryWrapper targetGeometry, UnitsOfMeasure unitsOfMeasure) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {
        return distanceEuclidean(targetGeometry, unitsOfMeasure.getUnitURI());
    }

    /**
     * Distance (Euclidean) in the Units of Measure stated in URI.
     *
     * @param targetGeometry
     * @param targetDistanceUnitsURI
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distanceEuclidean(GeometryWrapper targetGeometry, String targetDistanceUnitsURI) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        GeometryWrapper transformedTargetGeometry = checkTransformSRS(targetGeometry);

        double distance = xyGeometry.distance(transformedTargetGeometry.xyGeometry);
        String unitsURI = srsInfo.getUnitsOfMeasure().getUnitURI();
        return UnitsOfMeasure.conversion(distance, unitsURI, targetDistanceUnitsURI);
    }

    /**
     * Distance (Great Circle) defaulting to metres.
     *
     * @param targetGeometry
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distanceGreatCircle(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {
        return distanceGreatCircle(targetGeometry, Unit_URI.METRE_URL);
    }

    /**
     * Distance (Great Circle) in the Units of Measure.
     *
     * @param targetGeometry
     * @param unitsOfMeasure
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distanceGreatCircle(GeometryWrapper targetGeometry, UnitsOfMeasure unitsOfMeasure) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {
        return distanceGreatCircle(targetGeometry, unitsOfMeasure.getUnitURI());
    }

    /**
     * Distance (Great Circle) in the Units of Measure stated in URI.
     *
     * @param targetGeometry
     * @param targetDistanceUnitsURI
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distanceGreatCircle(GeometryWrapper targetGeometry, String targetDistanceUnitsURI) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        GeometryWrapper transformedSourceGeometry;

        //Check the conversion of Great Circle distance from metres into the requested units.
        if (!UnitsRegistry.isLinearUnits(targetDistanceUnitsURI)) {
            throw new UnitsConversionException("Great Circle distance units are metres and only linear conversion supported.");
        }

        if (srsInfo.isGeographic()) {
            //Already a geographic SRS.
            transformedSourceGeometry = this;
        } else {
            //Use WGS84 and not CRS84 as assuming WGS84 is more prevalent.
            if (!GeoSPARQLConfig.ALLOW_GEOMETRY_SRS_TRANSFORMATION) {
                throw new TransformException("GeometryLiteral SRS transformation is disabled, see GeoSPARQLConfig.allowGeometrySRSTransformation(...).");
            }
            transformedSourceGeometry = this.transform(SRS_URI.WGS84_CRS);
        }

        GeometryWrapper transformedTargetGeometry = transformedSourceGeometry.checkTransformSRS(targetGeometry);

        //Find the nearest pair of coordinates from each Geometry using Euclidean distance (adjusting for wrap around).
        //These are then used for Great Circle distance.
        CoordinatePair coordinatePair = CoordinatePair.findNearestPair(transformedSourceGeometry, transformedTargetGeometry);

        //Check whether the nearest pair are the same, i.e. they overlap or are within each other.
        if (coordinatePair.isEqual()) {
            //Exit early as the distance is zero.
            return 0.0;
        }

        Coordinate coord1 = coordinatePair.getCoord1();
        Coordinate coord2 = coordinatePair.getCoord2();

        double distance = GreatCircleDistance.haversineFormula(coord1.getY(), coord1.getX(), coord2.getY(), coord2.getX());

        //Convert the Great Circle distance from metres into the requested units.
        return UnitsOfMeasure.conversion(distance, Unit_URI.METRE_URL, targetDistanceUnitsURI);
    }

    /**
     * Distance (Euclidean or Great Circle depending on Geometry SRS URI)
     * defaulting to metres.
     *
     * @param targetGeometry
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distance(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {
        return distance(targetGeometry, Unit_URI.METRE_URL);
    }

    /**
     * Distance (Euclidean or Great Circle depending on Geometry SRS URI) in the
     * Units of Measure.
     *
     * @param targetGeometry
     * @param unitsOfMeasure
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distance(GeometryWrapper targetGeometry, UnitsOfMeasure unitsOfMeasure) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {
        return distance(targetGeometry, unitsOfMeasure.getUnitURI());
    }

    /**
     * Distance (Euclidean or Great Circle depending on Geometry SRS URI) in the
     * Units of Measure stated in URI.
     *
     * @param targetGeometry
     * @param targetDistanceUnitsURI
     * @return Distance
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     * @throws UnitsConversionException
     */
    public double distance(GeometryWrapper targetGeometry, String targetDistanceUnitsURI) throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        double targetDistance;
        if (srsInfo.isGeographic()) {
            targetDistance = distanceGreatCircle(targetGeometry, targetDistanceUnitsURI);
        } else {
            targetDistance = distanceEuclidean(targetGeometry, targetDistanceUnitsURI);
        }

        return targetDistance;
    }

    /**
     *
     * @return Boundary of GeometryWrapper
     */
    public GeometryWrapper boundary() {
        Geometry xyGeo = this.xyGeometry.getBoundary();
        Geometry parsingGeo = GeometryReverse.check(xyGeo, srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, srsInfo.getSrsURI(), geometryDatatypeURI, dimensionInfo);
    }

    /**
     *
     * @return Convex Hull of GeometryWrapper
     */
    public GeometryWrapper convexHull() {
        Geometry xyGeo = this.xyGeometry.convexHull();
        Geometry parsingGeo = GeometryReverse.check(xyGeo, srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, srsInfo.getSrsURI(), geometryDatatypeURI, dimensionInfo);
    }

    /**
     *
     * @param targetGeometry
     * @return Difference of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public GeometryWrapper difference(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        Geometry xyGeo = this.xyGeometry.difference(transformedGeometry.xyGeometry);
        Geometry parsingGeo = GeometryReverse.check(xyGeo, srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, srsInfo.getSrsURI(), geometryDatatypeURI, dimensionInfo);
    }

    /**
     * Envelope of GeometryWrapper with original coordinate order.
     *
     * @return Envelope of GeometryWrapper
     */
    public GeometryWrapper envelope() {
        GeometryFactory geometryFactory = this.xyGeometry.getFactory();
        Envelope xyEnvelope = this.getEnvelope();
        Geometry xyGeo = geometryFactory.toGeometry(xyEnvelope);
        Geometry parsingGeo = GeometryReverse.check(xyGeo, srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, srsInfo.getSrsURI(), geometryDatatypeURI, dimensionInfo);
    }

    /**
     * Envelope of GeometryWrapper in XY order.
     *
     * @return Envelope of GeometryWrapper
     */
    public Envelope getEnvelope() {
        if (envelope == null) {
            envelope = this.xyGeometry.getEnvelopeInternal();
        }

        return envelope;
    }

    /**
     *
     * @param targetGeometry
     * @return Intersection of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public GeometryWrapper intersection(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        Geometry xyGeo = this.xyGeometry.intersection(transformedGeometry.xyGeometry);
        Geometry parsingGeo = GeometryReverse.check(xyGeo, srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, srsInfo.getSrsURI(), geometryDatatypeURI, dimensionInfo);
    }

    /**
     *
     * @param targetGeometry
     * @return Intersection Matrix of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public IntersectionMatrix relate(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return xyGeometry.relate(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @param intersectionPattern
     * @return Relation of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean relate(GeometryWrapper targetGeometry, String intersectionPattern) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return xyGeometry.relate(transformedGeometry.xyGeometry, intersectionPattern);
    }

    /**
     *
     * @param targetGeometry
     * @return Symmetric Difference of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public GeometryWrapper symDifference(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        Geometry xyGeo = this.xyGeometry.symDifference(transformedGeometry.xyGeometry);
        Geometry parsingGeo = GeometryReverse.check(xyGeo, srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, srsInfo.getSrsURI(), geometryDatatypeURI, dimensionInfo, null);
    }

    /**
     *
     * @param targetGeometry
     * @return Union of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public GeometryWrapper union(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        Geometry xyGeo = this.xyGeometry.union(transformedGeometry.xyGeometry);
        Geometry parsingGeo = GeometryReverse.check(xyGeo, srsInfo);
        return new GeometryWrapper(parsingGeo, xyGeo, srsInfo.getSrsURI(), geometryDatatypeURI, dimensionInfo, null);
    }

    /**
     *
     * @param targetGeometry
     * @return sfContains of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean contains(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        this.checkPreparedGeometry();
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.preparedGeometry.contains(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @return sfCrosses of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean crosses(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        this.checkPreparedGeometry();
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.preparedGeometry.crosses(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @return sfDisjoint of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean disjoint(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        this.checkPreparedGeometry();
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.preparedGeometry.disjoint(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @return Topology equals of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean equalsTopo(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.xyGeometry.equalsTopo(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @return Equals exactly of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean equalsExact(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.xyGeometry.equalsExact(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @param tolerance
     * @return Equals exactly of GeometryWrapper with target using provided
     * tolerance.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean equalsExact(GeometryWrapper targetGeometry, double tolerance) throws FactoryException, MismatchedDimensionException, TransformException {
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.xyGeometry.equalsExact(transformedGeometry.xyGeometry, tolerance);
    }

    /**
     *
     * @param targetGeometry
     * @return sfIntersects of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean intersects(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        this.checkPreparedGeometry();
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.preparedGeometry.intersects(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @return sfOverlaps of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean overlaps(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        this.checkPreparedGeometry();
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.preparedGeometry.overlaps(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @return sfTouches of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean touches(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        this.checkPreparedGeometry();
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.preparedGeometry.touches(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @param targetGeometry
     * @return sfWithin of GeometryWrapper with target.
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public boolean within(GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException {
        this.checkPreparedGeometry();
        GeometryWrapper transformedGeometry = checkTransformSRS(targetGeometry);
        return this.preparedGeometry.within(transformedGeometry.xyGeometry);
    }

    /**
     *
     * @return GeometryWrapper as NodeValue
     */
    public NodeValue asNodeValue() throws DatatypeFormatException {
        Literal literal = asLiteral();
        return NodeValue.makeNode(literal.getLexicalForm(), literal.getDatatype());
    }

    /**
     *
     * @return GeometryWrapper as Node
     */
    public Node asNode() throws DatatypeFormatException {
        return asNodeValue().asNode();
    }

    /**
     *
     * @return GeometryWrapper as Literal
     */
    public Literal asLiteral() throws DatatypeFormatException {

        GeometryDatatype datatype = getGeometryDatatype(); //Datatype is only retrieved when required.
        if (lexicalForm != null) {
            return ResourceFactory.createTypedLiteral(lexicalForm, datatype);
        }

        Literal literal = asLiteral(datatype);
        lexicalForm = literal.getLexicalForm();
        return literal;
    }

    /**
     *
     * @param outputGeometryDatatypeURI
     * @return GeometryWrapper as Literal in datatype form.
     */
    public Literal asLiteral(String outputGeometryDatatypeURI) throws DatatypeFormatException {
        GeometryDatatype datatype = GeometryDatatype.get(outputGeometryDatatypeURI);
        return asLiteral(datatype);
    }

    /**
     *
     * @param datatype
     * @return GeometryWrapper as Literal
     */
    public Literal asLiteral(GeometryDatatype datatype) {
        String tempLexicalForm = datatype.unparse(this);
        return ResourceFactory.createTypedLiteral(tempLexicalForm, datatype);
    }

    /**
     *
     * @return Coordinate dimension, i.e. 2 (x,y), 3 (x,y,z or x,y,m) or 4
     * (x,y,z,m)
     */
    public int getCoordinateDimension() {
        return dimensionInfo.getCoordinate();
    }

    /**
     *
     * @return Spatial dimension, i.e. 2 or 3
     */
    public int getSpatialDimension() {
        return dimensionInfo.getSpatial();
    }

    /**
     *
     * @return Topological dimension, i.e. 0, 1 or 2
     */
    public int getTopologicalDimension() {
        return dimensionInfo.getTopological();
    }

    /**
     *
     * @return Enum of coordinate dimensions.
     */
    public CoordinateSequenceDimensions getCoordinateSequenceDimensions() {
        return dimensionInfo.getDimensions();
    }

    /**
     *
     * @return Units of Measure for the GeometryWrapper SRS.
     */
    public UnitsOfMeasure getUnitsOfMeasure() {
        return srsInfo.getUnitsOfMeasure();
    }

    /**
     *
     * @return GeometryWrapper's coordinate, spatial and topological dimensions.
     */
    public DimensionInfo getDimensionInfo() {
        return dimensionInfo;
    }

    /**
     *
     * @return String literal of Geometry Wrapper.
     */
    public String getLexicalForm() {

        if (lexicalForm != null) {
            return lexicalForm;
        } else {
            Literal literal = asLiteral();
            return literal.getLexicalForm();
        }
    }

    /**
     *
     * @return Geometry is empty of coordinates.
     */
    public boolean isEmpty() {
        return this.xyGeometry.isEmpty();
    }

    /**
     *
     * @return Geometry is in simple form.
     */
    public boolean isSimple() {
        return this.xyGeometry.isSimple();
    }

    /**
     *
     * @return Geometry is topologically valid.
     */
    public boolean isValid() {
        return this.xyGeometry.isValid();
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal.
     *
     * @param geometryLiteral
     * @param targetIndex
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static final GeometryWrapper extract(NodeValue geometryLiteral, GeometryIndex targetIndex) {

        Node node = geometryLiteral.asNode();

        return extract(node, targetIndex);
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal.
     *
     * @param geometryLiteral
     * @param targetIndex
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static final GeometryWrapper extract(Node geometryLiteral, GeometryIndex targetIndex) throws DatatypeFormatException {

        if (!geometryLiteral.isLiteral()) {
            throw new DatatypeFormatException("Not a Literal: " + geometryLiteral);
        }

        String datatypeURI = geometryLiteral.getLiteralDatatypeURI();
        String lexicalForm = geometryLiteral.getLiteralLexicalForm();
        return extract(lexicalForm, datatypeURI, targetIndex);
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal.
     *
     * @param geometryLiteral
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static final GeometryWrapper extract(NodeValue geometryLiteral) {
        return extract(geometryLiteral, GeometryIndex.PRIMARY);
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal.
     *
     * @param geometryLiteral
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static final GeometryWrapper extract(Node geometryLiteral) {
        return extract(geometryLiteral, GeometryIndex.PRIMARY);
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal. Returns null if invalid
     * literal provided.
     *
     * @param geometryLiteral
     * @param targetIndex
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static final GeometryWrapper extract(Literal geometryLiteral, GeometryIndex targetIndex) {
        return extract(geometryLiteral.getLexicalForm(), geometryLiteral.getDatatypeURI(), targetIndex);
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal.
     *
     * @param geometryLiteral
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static final GeometryWrapper extract(Literal geometryLiteral) {
        return extract(geometryLiteral, GeometryIndex.PRIMARY);
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal.
     *
     * @param lexicalForm
     * @param datatypeURI
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static GeometryWrapper extract(String lexicalForm, String datatypeURI) {
        return extract(lexicalForm, datatypeURI, GeometryIndex.PRIMARY);
    }

    /**
     * Extract Geometry Wrapper from Geometry Literal.
     *
     * @param lexicalForm
     * @param datatypeURI
     * @param targetIndex
     * @return Geometry Wrapper of the Geometry Literal.
     */
    public static GeometryWrapper extract(String lexicalForm, String datatypeURI, GeometryIndex targetIndex) throws DatatypeFormatException {

        if (lexicalForm == null || datatypeURI == null) {
            throw new DatatypeFormatException("GeometryWrapper extraction: arguments cannot be null - " + lexicalForm + ", " + datatypeURI);
        }

        GeometryDatatype datatype = GeometryDatatype.get(datatypeURI);
        GeometryWrapper geometry = datatype.parse(lexicalForm, targetIndex);
        return geometry;
    }

    /**
     * Builds a WKT Point of Geometry Wrapper.<br>
     * This method does not use the GeometryLiteralIndex and so is best used for
     * one of Geometry Wrappers.
     *
     * @param x
     * @param y
     * @param srsURI
     *
     * @return Geometry Wrapper of WKT Point.
     */
    public static final GeometryWrapper fromPoint(double x, double y, String srsURI) {
        CustomCoordinateSequence coordSequence = CustomCoordinateSequence.createPoint(x, y);
        GeometryWrapper geometryWrapper = new GeometryWrapper(coordSequence, WKTDatatype.URI, srsURI);
        return geometryWrapper;
    }

    /**
     *
     * @return Empty GeometryWrapper in WKT datatype.
     */
    public static final GeometryWrapper getEmptyWKT() {
        return WKTDatatype.INSTANCE.read("");
    }

    /**
     *
     * @return Empty GeometryWrapper in GML datatype.
     */
    public static final GeometryWrapper getEmptyGML() {
        return GMLDatatype.INSTANCE.read("");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.dimensionInfo);
        hash = 23 * hash + Objects.hashCode(this.srsInfo);
        hash = 23 * hash + Objects.hashCode(this.xyGeometry);
        hash = 23 * hash + Objects.hashCode(this.geometryDatatypeURI);
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
        final GeometryWrapper other = (GeometryWrapper) obj;
        if (!Objects.equals(this.geometryDatatypeURI, other.geometryDatatypeURI)) {
            return false;
        }
        if (!Objects.equals(this.dimensionInfo, other.dimensionInfo)) {
            return false;
        }
        if (!Objects.equals(this.srsInfo, other.srsInfo)) {
            return false;
        }
        return Objects.equals(this.xyGeometry, other.xyGeometry);
    }

    @Override
    public String toString() {
        return "GeometryWrapper{" + "dimensionInfo=" + dimensionInfo + ", geometryDatatypeURI=" + geometryDatatypeURI + ", lexicalForm=" + lexicalForm + ", utmURI=" + utmURI + ", latitude=" + latitude + ", xyGeometry=" + xyGeometry + ", parsingGeometry=" + parsingGeometry + ", preparedGeometry=" + preparedGeometry + ", srsInfo=" + srsInfo + '}';
    }

}
