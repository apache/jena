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
package org.apache.jena.geosparql.implementation.datatype;

import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.parsers.gml.GMLReader;
import org.apache.jena.geosparql.implementation.parsers.gml.GMLWriter;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import java.io.IOException;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.jdom2.JDOMException;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 *
 */
public class GMLDatatype extends GeometryDatatype {

    private static final Logger LOGGER = LoggerFactory.getLogger(GMLDatatype.class);

    /**
     * The default GML type URI.
     */
    public static final String URI = Geo.GML;

    /**
     * A static instance of GMLDatatype.
     */
    public static final GMLDatatype INSTANCE = new GMLDatatype();

    /**
     * XML element tag "gml" is defined for the convenience of GML generation.
     */
    public static final String GML_PREFIX = "gml";

    /**
     * private constructor - single global instance.
     */
    public GMLDatatype() {
        super(URI);
    }

    /**
     * This method Un-parses the JTS Geometry to the GML literal
     *
     * @param geometry - the JTS Geometry to be un-parsed
     * @return GML - the returned GML Literal.
     * <br> Notice that the Spatial Reference System
     * "urn:ogc:def:crs:OGC::CRS84" is predefined in the returned GML literal.
     */
    @Override
    public String unparse(Object geometry) {
        if (geometry instanceof GeometryWrapper) {
            GeometryWrapper geometryWrapper = (GeometryWrapper) geometry;
            return GMLWriter.write(geometryWrapper);
        } else {
            throw new DatatypeFormatException("Object to unparse GMLDatatype is not a GeometryWrapper: " + geometry);
        }
    }

    @Override
    public GeometryWrapper read(String geometryLiteral) {
        try {
            GMLReader gmlReader = GMLReader.extract(geometryLiteral);
            Geometry geometry = gmlReader.getGeometry();
            String srsURI = gmlReader.getSrsURI();
            DimensionInfo dimensionInfo = gmlReader.getDimensionInfo();

            return new GeometryWrapper(geometry, srsURI, URI, dimensionInfo, geometryLiteral);
        } catch (JDOMException | IOException ex) {
            throw new DatatypeFormatException("Illegal GML literal:" + geometryLiteral + ". " + ex.getMessage());
        }
    }

    @Override
    public String toString() {
        return "GMLDatatype{" + URI + '}';
    }

}
