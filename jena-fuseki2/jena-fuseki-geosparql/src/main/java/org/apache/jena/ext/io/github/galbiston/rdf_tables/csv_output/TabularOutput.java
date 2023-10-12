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
package org.apache.jena.ext.io.github.galbiston.rdf_tables.csv_output;

import static org.apache.jena.ext.io.github.galbiston.rdf_tables.file.DefaultValues.CLASS_CHARACTER;
import static org.apache.jena.ext.io.github.galbiston.rdf_tables.file.DefaultValues.HEADER_ITEM_DELIMITER;
import static org.apache.jena.ext.io.github.galbiston.rdf_tables.file.DefaultValues.HEADER_ITEM_DELIMITER_CHARACTER;
import static org.apache.jena.ext.io.github.galbiston.rdf_tables.file.DefaultValues.INVERT_CHARACTER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.Datatypes;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 *
 *
 */
public abstract class TabularOutput {

    protected static String BASE_URI = "";
    protected static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static void setBaseURI(String baseURI) {
        BASE_URI = baseURI;
    }

    public static String getBaseURI() {
        return BASE_URI;
    }

    protected static void countProperties(ArrayListValuedHashMap<Property, ? extends RDFNode> propertyListMap, HashMap<Property, Integer> countMap) {
        for (Property property : propertyListMap.keySet()) {
            Integer value = propertyListMap.get(property).size();
            if (countMap.containsKey(property)) {
                Integer maxValue = countMap.get(property);
                if (value > maxValue) {
                    countMap.put(property, value);
                }
            } else {
                countMap.put(property, value);
            }
        }
    }

    protected static void writeBaseHeader(List<String> header, Resource classResource) {
        header.add(getBaseHeader(BASE_URI, classResource));
    }

    public static String getBaseHeader(String baseURI, Resource classResource) {
        return getBaseHeader(baseURI, classResource.getURI());
    }

    public static String getBaseHeader(String baseURI, String classURI) {
        return getPropertyDatatypeColumnHeader(baseURI, classURI, null);
    }

    public static String getPropertyClassHeader(String propertyURI, String classURI) {
        return getPropertyDatatypeColumnHeader(propertyURI, classURI, null);
    }

    public static String getInvertedPropertyClassHeader(String propertyURI, String classURI) {
        String header = getPropertyDatatypeColumnHeader(propertyURI, classURI, null);
        return INVERT_CHARACTER + header;
    }

    public static String getPropertyDatatypeHeader(String propertyURI, String datatypeURI) {
        return getPropertyDatatypeColumnHeader(propertyURI, datatypeURI, null);
    }

    public static String getPropertyDatatypeColumnHeader(String propertyURI, String datatypeURI, Integer columnPosition) {
        if (columnPosition != null && columnPosition < 0) {
            return null;
        }
        String header = propertyURI + HEADER_ITEM_DELIMITER + datatypeURI;
        if (columnPosition != null) {
            return header + HEADER_ITEM_DELIMITER + columnPosition;
        }

        return header;
    }

    protected static void writeHeader(List<String> header, Property property, Datatypes propertyDatatype) {
        writeHeader(header, property, propertyDatatype, null);
    }

    protected static void writeHeader(List<String> header, Property property, Datatypes propertyDatatype, Integer columnPosition) {
        HashMap<Property, Datatypes> propertyDatatypes = new HashMap<>();
        propertyDatatypes.put(property, propertyDatatype);
        writeHeader(header, property, 1, propertyDatatypes, columnPosition);
    }

    protected static void writeHeader(List<String> header, Property property) {
        writeHeader(header, property, 1, new HashMap<>(), null);
    }

    protected static void writeHeader(List<String> header, Property property, Resource classResource) {
        writeHeader(header, property, classResource, 1);
    }

    protected static void writeHeader(List<String> header, Property property, Resource classResource, Integer maxCount) {
        String headerLabel = property.getURI() + HEADER_ITEM_DELIMITER_CHARACTER + CLASS_CHARACTER + classResource.getURI();
        for (int i = 0; i < maxCount; i++) {
            header.add(headerLabel);
        }
    }

    protected static void writeHeader(List<String> header, Property property, Resource classResource, Integer maxCount, Integer columnPosition) {
        String headerLabel = property.getURI() + HEADER_ITEM_DELIMITER_CHARACTER + CLASS_CHARACTER + classResource.getURI() + HEADER_ITEM_DELIMITER_CHARACTER + columnPosition;
        for (int i = 0; i < maxCount; i++) {
            header.add(headerLabel);
        }
    }

    protected static void writeHeader(List<String> header, Property property, Integer maxCount) {
        writeHeader(header, property, maxCount, new HashMap<>(), null);
    }

    protected static void writeHeader(List<String> header, Property property, Integer maxCount, Integer columnPosition) {
        writeHeader(header, property, maxCount, new HashMap<>(), columnPosition);
    }

    protected static void writeHeader(List<String> header, Property property, Integer maxCount, HashMap<Property, Datatypes> propertyDatatypes, Integer columnPosition) {
        String headerLabel;
        if (propertyDatatypes.containsKey(property)) {
            Datatypes datatype = propertyDatatypes.get(property);
            headerLabel = property.getURI() + HEADER_ITEM_DELIMITER_CHARACTER + datatype;
        } else {
            headerLabel = property.getURI();
        }

        if (columnPosition != null) {
            headerLabel += HEADER_ITEM_DELIMITER_CHARACTER + columnPosition;
        }

        for (int i = 0; i < maxCount; i++) {
            header.add(headerLabel);
        }

    }

    protected static void writeHeader(List<String> header, HashMap<Property, Integer> countMap) {
        writeHeader(header, countMap, new HashMap<>());
    }

    protected static void writeHeader(List<String> header, HashMap<Property, Integer> countMap, HashMap<Property, Datatypes> propertyDatatypes) {
        for (Map.Entry<Property, Integer> entry : countMap.entrySet()) {
            //Build the header label from either the datatype and property or just the property.
            writeHeader(header, entry.getKey(), entry.getValue(), propertyDatatypes, null);
        }
    }

    protected void write(List<String> line, RDFNode rdfNode) {
        String label;
        if (rdfNode.isLiteral()) {
            Literal literal = rdfNode.asLiteral();
            label = literal.getLexicalForm();
        } else {
            Resource resource = rdfNode.asResource();
            String namespace = resource.getNameSpace();
            if (namespace.equals(BASE_URI)) {
                label = resource.getLocalName();
            } else {
                label = resource.getURI();
            }
        }
        line.add(label);
    }

    protected void write(List<String> line, List<? extends RDFNode> values, Integer maxCount) {
        for (int i = 0; i < maxCount; i++) {
            if (i < values.size()) {
                RDFNode rdfNode = values.get(i);
                write(line, rdfNode);
            } else {
                line.add("");
            }
        }
    }

    protected void write(List<String> line, ArrayListValuedHashMap<Property, ? extends RDFNode> propertyListMap, HashMap<Property, Integer> countMap) {
        for (Map.Entry<Property, Integer> entry : countMap.entrySet()) {
            Property property = entry.getKey();
            Integer maxCount = entry.getValue();
            List<? extends RDFNode> rdfNodes = propertyListMap.get(property);
            for (int i = 0; i < maxCount; i++) {
                if (i < rdfNodes.size()) {
                    RDFNode rdfNode = rdfNodes.get(i);
                    write(line, rdfNode);
                } else {
                    line.add("");
                }
            }
        }
    }

}
