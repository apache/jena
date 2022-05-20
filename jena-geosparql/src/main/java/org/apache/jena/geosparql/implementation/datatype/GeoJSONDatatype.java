package org.apache.jena.geosparql.implementation.datatype;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * this GeoJSONDatatype does not yet do anything other than wrap a literal
 */
public class GeoJSONDatatype extends BaseDatatype {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJSONDatatype.class);

    /**
     * The default GML type URI.
     */
    public static final String URI = Geo.GEO_JSON;

    /**
     * A static instance of GeoJSONDatatype.
     */
    public static final GeoJSONDatatype INSTANCE = new GeoJSONDatatype();

    /**
     * private constructor - single global instance.
     */
    private GeoJSONDatatype() {
        super(URI);
    }

}
