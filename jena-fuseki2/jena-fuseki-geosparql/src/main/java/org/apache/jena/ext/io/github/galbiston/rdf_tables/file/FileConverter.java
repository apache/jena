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
package org.apache.jena.ext.io.github.galbiston.rdf_tables.file;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import static org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.DatatypeController.HTTP_PREFIX;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.DatatypeController;
import org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.PrefixController;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read a Tabular file of triples.
 *
 * First column is subject and subsequent columns are objects.
 *
 * Header row provides 1) datatype and 2) property in each column (space
 * separated by default). e.g. "INTEGER http://example.org/tom/schema#age"
 *
 * Datatype can be upper or lower case and optional (assumed to be a Resource if
 * datatype is absent).
 *
 * Header row first column specifies the BASE URI of the document and the type
 * of the subject. e.g. "http://example.org/tom/my-data#
 * http://example.org/tom/schema#Person"
 *
 * BASE URI is prefixed for any property or resource which does not have
 * "http://" at start.
 *
 * Empty fields will be ignored so that irregular rows can be used.
 *
 * @author Greg Albiston
 */
public class FileConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void writeToModel(File inputFile, Model model) {
        writeToModel(inputFile, model, ',', true);
    }

    public static void writeToModel(File inputFile, Model model, char delimiter, Boolean isNamedIndividual) {

        LOGGER.info("File Conversion Started: {}", inputFile.getPath());

        HashMap<Integer, String> datatypeURIs = new HashMap<>();
        HashMap<Integer, Property> propertyURIs = new HashMap<>();
        HashMap<Integer, Resource> classURIs = new HashMap<>();
        List<Integer> targetColumns = new ArrayList<>();
        List<Boolean> propertyDirections = new ArrayList<>();
        int lineNumber = 1;

        CSVParserBuilder parserBuilder = new CSVParserBuilder().withSeparator(delimiter);
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(inputFile)).withCSVParser(parserBuilder.build()).build()) {

            String baseURI = readHeader(reader.readNext(), datatypeURIs, propertyURIs, classURIs, targetColumns, propertyDirections);

            String[] line;
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                readData(line, baseURI, datatypeURIs, propertyURIs, classURIs, targetColumns, propertyDirections, model, isNamedIndividual);
            }
            model.setNsPrefixes(PrefixController.getPrefixes());

        } catch (IOException | RuntimeException | CsvValidationException ex) {

            LOGGER.error("FileConverter: Line - {}, File - {}, Exception - {}", lineNumber, inputFile.getAbsolutePath(), ex.getMessage());
            throw new AssertionError("Error loading file: " + inputFile.getAbsolutePath());
        }

        LOGGER.info("File Conversion Completed: {}", inputFile.getPath());
    }

    private static String readHeader(String[] headerLine, HashMap<Integer, String> datatypeURIs, HashMap<Integer, Property> propertyURIs, HashMap<Integer, Resource> classURIs, List<Integer> targetColumns, List<Boolean> propertyDirections) {
        String baseURI;
        String header = null;
        String[] parts;
        try {
            //First column: BASE_URI CLASS_URI
            header = headerLine[0];
            parts = header.split(DefaultValues.HEADER_ITEM_DELIMITER);
            baseURI = parts[0];
            createClass(0, parts[1].startsWith(DefaultValues.CLASS_CHARACTER) ? parts[1].substring(1) : parts[1], baseURI, classURIs);
            targetColumns.add(0);
            propertyDirections.add(Boolean.TRUE);
        } catch (Exception ex) {
            LOGGER.error("{} - Header column zero: {}", ex.getMessage(), header);
            throw new AssertionError();
        }
        //Remaining Columns
        for (int i = 1; i < headerLine.length; i++) {
            try {
                header = headerLine[i];
                parts = header.split(DefaultValues.HEADER_ITEM_DELIMITER);

                //Extract datatype and propertyURI from header field.
                String datatypeLabel = null;
                String propertyLabel = parts[0];
                String classLabel = null;
                int targetColumn = 0;

                switch (parts.length) {
                    case 1:
                        //Indicate that no class was specified.
                        classLabel = DefaultValues.NO_CLASS_ANON.toString();
                        break;
                    case 2:
                        if (integerCheck(parts[1])) {
                            targetColumn = Integer.parseInt(parts[1]);
                        } else {

                            if (parts[1].startsWith(DefaultValues.CLASS_CHARACTER)) {
                                classLabel = parts[1].substring(1);
                            } else {
                                datatypeLabel = parts[1];
                            }
                        }
                        break;
                    default:
                        if (parts[1].startsWith(DefaultValues.CLASS_CHARACTER)) {
                            classLabel = parts[1].substring(1);
                        } else {
                            datatypeLabel = parts[1];
                        }

                        if (parts[2].startsWith(DefaultValues.CLASS_CHARACTER)) {
                            classLabel = parts[2].substring(1);
                        } else {
                            targetColumn = Integer.parseInt(parts[2]);
                        }
                        break;
                }

                //Record the target column
                targetColumns.add(targetColumn);

                //Check the property direction
                if (propertyLabel.charAt(0) == DefaultValues.INVERT_CHARACTER) {

                    if (datatypeLabel == null) {
                        propertyLabel = propertyLabel.substring(1);
                        propertyDirections.add(Boolean.FALSE);
                    } else {
                        LOGGER.error("Cannot invert from Datatype {} : {}", i, header);
                        throw new AssertionError();
                    }
                } else {
                    propertyDirections.add(Boolean.TRUE);
                }

                createProperty(i, propertyLabel, baseURI, propertyURIs);
                createDatatype(i, datatypeLabel, baseURI, datatypeURIs);
                createClass(i, classLabel, baseURI, classURIs);
            } catch (NumberFormatException ex) {
                LOGGER.error("{} - Header column {}: {}", ex.getMessage(), i, header);
                throw new AssertionError();
            }
        }
        return baseURI;
    }

    private static void createProperty(Integer index, String propertyLabel, String baseURI, HashMap<Integer, Property> propertyURIs) {
        String uri = PrefixController.lookupURI(propertyLabel, baseURI);
        Property property = ResourceFactory.createProperty(uri);
        propertyURIs.put(index, property);
    }

    private static void createClass(Integer index, String classLabel, String baseURI, HashMap<Integer, Resource> classURIs) {

        if (classLabel != null) {
            //Case when no class specified.
            if (classLabel.equals(DefaultValues.NO_CLASS_ANON.toString())) {
                classURIs.put(index, DefaultValues.NO_CLASS_ANON);
            }

            String uri = PrefixController.lookupURI(classLabel, baseURI);
            Resource resource = ResourceFactory.createResource(uri);
            classURIs.put(index, resource);
        }
    }

    private static void createDatatype(int index, String datatypeLabel, String baseURI, HashMap<Integer, String> datatypeURIs) {
        if (datatypeLabel != null) {
            datatypeURIs.put(index, DatatypeController.lookupDatatypeURI(datatypeLabel, baseURI));
        }
    }

    private static boolean integerCheck(String checkString) {
        try {
            Integer.parseInt(checkString);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private static void readData(String[] dataLine, String baseURI, HashMap<Integer, String> datatypeURIs, HashMap<Integer, Property> propertyURIs, HashMap<Integer, Resource> classURIs, List<Integer> targetColumns, List<Boolean> propertyDirections, Model model, Boolean isNamedIndividual) {

        //Map of subject encountered in each row.
        HashMap<Integer, Resource> indviduals = new HashMap<>();

        //Find all individuals
        for (Integer index : classURIs.keySet()) {
            //Ensure that the line has the item, i.e. ragged.
            if (index >= dataLine.length) {
                continue;
            }

            //Create subject as individual with specified class.
            String data = dataLine[index];
            //Skip early on empty or whitespace string, i.e. sparse.
            if (data.isEmpty()) {
                continue;
            }
            Resource subject = createIndividual(data, model, baseURI, classURIs.get(index), isNamedIndividual);
            indviduals.put(index, subject);
        }

        //Extract the objects.
        for (int i = 1; i < dataLine.length; i++) {
            String data = dataLine[i];
            try {

                //Skip early on empty or whitespace string.
                if (data.isEmpty()) {
                    continue;
                }

                Property property = propertyURIs.get(i);
                RDFNode object;
                if (datatypeURIs.containsKey(i)) {
                    String datatypeURI = datatypeURIs.get(i);
                    object = DatatypeController.createLiteral(data, datatypeURI);
                } else if (indviduals.containsKey(i)) {
                    //No datatype so must be an individual.
                    object = indviduals.get(i);
                } else {
                    LOGGER.warn("Cannot find: {} in index: {}. Class URI may be missing from column header. Creating as an Object as provided by input.", data, i);
                    object = ResourceFactory.createResource(data);
                }

                Resource targetSubject;
                int targetColumn = targetColumns.get(i);
                if (indviduals.containsKey(targetColumn)) {
                    targetSubject = indviduals.get(targetColumn);
                    Boolean isSubjectObject = propertyDirections.get(i);
                    if (isSubjectObject) {
                        targetSubject.addProperty(property, object);
                    } else {
                        if (object instanceof Resource) {
                            Resource targetObject = (Resource) object;
                            targetObject.addProperty(property, targetSubject);
                        } else {
                            LOGGER.error("Literal: {} cannot be inverted: {}", data, dataLine);
                            throw new AssertionError();
                        }
                    }
                } else {
                    LOGGER.error("Target column {} for item: {} is empty on line: {}", targetColumn, data, dataLine);
                    throw new AssertionError();
                }
            } catch (Exception ex) {
                LOGGER.error("{} - Reading item: {} on line: {}", ex.getMessage(), data, dataLine);
            }
        }
    }

    private static Resource createIndividual(String tidyData, Model model, String baseURI, Resource classURI, Boolean isNamedIndividual) {
        //Check whether the subject contains an explicit URI already.
        String uri;
        String label;
        if (tidyData.startsWith(HTTP_PREFIX)) {
            uri = tidyData;
            label = tidyData.substring(tidyData.indexOf("#") + 1);
        } else {
            uri = baseURI + tidyData;
            label = tidyData;
        }

        //Create the individual with specified class and owl:NamedIndividual class
        Resource subject = model.createResource(uri);
        if (classURI != null) {
            //Check whether the individual actually has a class specified.
            if (!classURI.equals(DefaultValues.NO_CLASS_ANON)) {
                subject.addProperty(RDF.type, classURI);
            }
        }
        if (isNamedIndividual) {
            subject.addProperty(RDF.type, DefaultValues.NAMED_INDIVIDUAL);
        }

        //Apply a label to the subject.
        if (DefaultValues.IS_RDFS_LABEL) {
            Literal labelLiteral = model.createLiteral(label);
            subject.addLiteral(RDFS.label, labelLiteral);
        }
        return subject;
    }

}
