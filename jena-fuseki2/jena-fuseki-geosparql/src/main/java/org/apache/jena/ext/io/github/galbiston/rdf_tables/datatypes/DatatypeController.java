/**
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gerg
 */
public class DatatypeController {

    //TODO load from file
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final HashMap<String, String> DATATYPE_PREFIXES = new HashMap<>();
    private static final TypeMapper TYPE_MAPPER = TypeMapper.getInstance();

    public static final String HTTP_PREFIX = "http://";
    public static final String BOOLEAN_URI = XSDBaseNumericType.XSDboolean.getURI();

    public static HashMap<String, String> getDatatypePrefixes() {

        if (DATATYPE_PREFIXES.isEmpty()) {
            loadXSDDatatypePrefixes();
        }
        return DATATYPE_PREFIXES;
    }

    private static void loadXSDDatatypePrefixes() {

        DATATYPE_PREFIXES.put("string", XSDBaseNumericType.XSDstring.getURI());
        DATATYPE_PREFIXES.put("int", XSDBaseNumericType.XSDint.getURI());
        DATATYPE_PREFIXES.put("integer", XSDBaseNumericType.XSDinteger.getURI());
        DATATYPE_PREFIXES.put("positiveinteger", XSDBaseNumericType.XSDpositiveInteger.getURI());
        DATATYPE_PREFIXES.put("positive_integer", XSDBaseNumericType.XSDpositiveInteger.getURI());
        DATATYPE_PREFIXES.put("nonnegativeinteger", XSDBaseNumericType.XSDnonNegativeInteger.getURI());
        DATATYPE_PREFIXES.put("non_negative_integer", XSDBaseNumericType.XSDnonNegativeInteger.getURI());
        DATATYPE_PREFIXES.put("nonpositiveinteger", XSDBaseNumericType.XSDnonPositiveInteger.getURI());
        DATATYPE_PREFIXES.put("non_positive_integer", XSDBaseNumericType.XSDnonPositiveInteger.getURI());
        DATATYPE_PREFIXES.put("double", XSDBaseNumericType.XSDdouble.getURI());
        DATATYPE_PREFIXES.put("decimal", XSDBaseNumericType.XSDdecimal.getURI());
        DATATYPE_PREFIXES.put("boolean", XSDBaseNumericType.XSDboolean.getURI());
        DATATYPE_PREFIXES.put("date", XSDBaseNumericType.XSDdate.getURI());
        DATATYPE_PREFIXES.put("datetime", XSDBaseNumericType.XSDdateTime.getURI());
        DATATYPE_PREFIXES.put("time", XSDBaseNumericType.XSDtime.getURI());
        DATATYPE_PREFIXES.put("duration", XSDBaseNumericType.XSDduration.getURI());
    }

    /**
     * Register a map of prefix and datatype URI.<br>
     * This will create a new datatype instance which will overwrite any
     * previously registered datatype instances using the same datatype prefix
     * or URI.
     *
     * @param prefixDatatypes
     */
    public static void addPrefixDatatypeURIs(HashMap<String, String> prefixDatatypes) {
        prefixDatatypes.forEach((k, v) -> addPrefixDatatypeURI(k, v));
    }

    /**
     * Register a prefix and datatype URI.<br>
     * This will create a new datatype instance which will overwrite any
     * previously registered datatype instances using the same datatype prefix
     * or URI.
     *
     * @param prefix
     * @param datatypeURI
     */
    public static void addPrefixDatatypeURI(String prefix, String datatypeURI) {
        addPrefixDatatype(prefix, new BaseDatatype(datatypeURI));
    }

    /**
     * Register a map of prefix and datatype URI.<br>
     * This will create a new datatype instance which will overwrite any
     * previously registered datatype instances using the same datatype prefix.
     *
     * @param prefixDatatypes
     */
    public static void addPrefixDatatypes(HashMap<String, BaseDatatype> prefixDatatypes) {
        prefixDatatypes.forEach((k, v) -> addPrefixDatatype(k, v));
    }

    /**
     * Register a map of prefix and datatype URI.<br>
     * This will create a new datatype instance which will overwrite any
     * previously registered datatype instances using the same datatype prefix.
     *
     * @param prefix
     * @param datatype
     */
    public static void addPrefixDatatype(String prefix, BaseDatatype datatype) {
        getDatatypePrefixes();
        String datatypeURI = datatype.getURI();
        if (PrefixController.checkURI(datatypeURI)) {
            DATATYPE_PREFIXES.put(prefix, datatypeURI);
            TYPE_MAPPER.registerDatatype(datatype);
        } else {
            LOGGER.error("Datatype URI is not a URI: {} {}", prefix, datatypeURI);
            throw new AssertionError();
        }
    }

    /**
     * Lookup the datatypeURI according to the label.<br>
     * The label is used as a prefix reference. If no result is found the label
     * and base URI are used to create a new datatype URI.
     *
     * @param datatypeLabel
     * @param baseURI
     * @return Datatype URI
     */
    public static String lookupDatatypeURI(String datatypeLabel, String baseURI) {
        getDatatypePrefixes();
        //Check if datatypeLabel is a URI and if so return.
        String tidyLabel = datatypeLabel.toLowerCase();
        if (DATATYPE_PREFIXES.containsKey(tidyLabel)) {
            return DATATYPE_PREFIXES.get(tidyLabel);
        } else {
            String datatypeURI = PrefixController.lookupURI(datatypeLabel, baseURI);
            DATATYPE_PREFIXES.put(datatypeLabel, datatypeURI);
            TYPE_MAPPER.registerDatatype(new BaseDatatype(datatypeURI));
            return datatypeURI;
        }
    }

    /**
     * Lookup the datatype associated with a datatype URI.
     *
     * @param datatypeURI
     * @return RDFDatatype
     */
    public static RDFDatatype lookupDatatype(String datatypeURI) {
        getDatatypePrefixes();
        RDFDatatype datatype = TYPE_MAPPER.getTypeByName(datatypeURI);

        if (datatype != null) {
            return datatype;
        } else {
            LOGGER.error("{} datatype URI not registered.", datatypeURI);
            throw new AssertionError();
        }
    }

    /**
     * Create a literal according to the datatype URI.
     *
     * @param data
     * @param datatypeURI
     * @return Literal
     */
    public static Literal createLiteral(String data, String datatypeURI) {
        getDatatypePrefixes();
        RDFDatatype datatype = TYPE_MAPPER.getTypeByName(datatypeURI);

        if (datatype != null) {
            String tidyData = tidyDatatype(data, datatypeURI);
            return ResourceFactory.createTypedLiteral(tidyData, datatype);
        } else {
            LOGGER.error("Datatype URI {} not recognised for data: {}", datatypeURI, data);
            return null;
        }

    }

    /**
     * Format according to xsd restrictions.
     *
     * @param untidyData
     * @param dataTypeURI
     * @return
     */
    private static String tidyDatatype(String untidyData, String dataTypeURI) {
        String tidyData = untidyData.trim();

        if (dataTypeURI.equals(DatatypeController.BOOLEAN_URI)) {
            tidyData = tidyData.toLowerCase();
        }

        return tidyData;
    }

}
