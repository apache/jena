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
package org.apache.jena.geosparql.configuration;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class GeoSPARQLOperations {

    private static final String GEOSPARQL_SCHEMA_FILE = "schema/geosparql_vocab_all_v1_0_1_updated.rdf";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Every subProperty of hasGeometry is made a subProperty of
     * hasDefaultGeometry.<br>
     * Assumption that each Feature has a single hasGeometry property.<br>
     * Requires RDFS inferencing to propagate through the data.
     *
     * @param model
     */
    public static final void applySubPropertyDefaultGeometry(Model model) {

        try {
            ResIterator resIt = model.listResourcesWithProperty(RDFS.subPropertyOf, Geo.HAS_GEOMETRY_PROP);
            while (resIt.hasNext()) {
                Resource res = resIt.nextResource();
                res.addProperty(RDFS.subPropertyOf, Geo.HAS_DEFAULT_GEOMETRY_PROP);
            }

        } catch (Exception ex) {
            LOGGER.error("Inserting GeoSPARQL predicates error: {}", ex.getMessage());
        }
    }

    /**
     * Apply hasDefaultGeometry for every Feature with a single hasGeometry
     * property.
     *
     * @param dataset
     */
    public static final void applyDefaultGeometry(Dataset dataset) {

        try {
            LOGGER.info("Applying hasDefaultGeometry - Started");
            //Default Model
            dataset.begin(ReadWrite.WRITE);
            Model defaultModel = dataset.getDefaultModel();
            GeoSPARQLOperations.applyDefaultGeometry(defaultModel);

            //Named Models
            Iterator<String> graphNames = dataset.listNames();
            while (graphNames.hasNext()) {
                String graphName = graphNames.next();
                Model namedModel = dataset.getNamedModel(graphName);
                GeoSPARQLOperations.applyDefaultGeometry(namedModel);
            }

            dataset.commit();
            LOGGER.info("Applying hasDefaultGeometry - Completed");
        } catch (Exception ex) {
            LOGGER.error("Write Error: {}", ex.getMessage());
        } finally {
            dataset.end();
        }

    }

    /**
     * Apply hasDefaultGeometry for every Feature with a single hasGeometry
     * property.
     *
     * @param model
     */
    public static final void applyDefaultGeometry(Model model) {

        ResIterator featureIt = model.listResourcesWithProperty(Geo.HAS_GEOMETRY_PROP);
        while (featureIt.hasNext()) {
            Resource feature = featureIt.nextResource();
            if (!feature.hasProperty(Geo.HAS_DEFAULT_GEOMETRY_PROP)) {
                List<Statement> statement = feature.listProperties(Geo.HAS_GEOMETRY_PROP).toList();
                if (statement.size() == 1) {
                    try {
                        Resource geometry = statement.get(0).getResource();
                        feature.addProperty(Geo.HAS_DEFAULT_GEOMETRY_PROP, geometry);
                    } catch (Exception ex) {
                        LOGGER.error("Error creating default geometry: {}", ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Apply hasDefaultGeometry for every Feature with a single hasGeometry
     * property.
     *
     * @param inputFile
     * @param inputLang
     * @param outputFile
     * @param outputLang
     */
    public static final void applyDefaultGeometry(File inputFile, Lang inputLang, File outputFile, Lang outputLang) {

        LOGGER.info("Applying Predicates from File: {} to {} - Started", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());

        Model model = applyDefaultGeometry(inputFile, inputLang);
        //Write the output.
        writeOutputModel(model, outputFile, outputLang, inputFile);

        LOGGER.info("Applying Predicates from File: {} to {} - Completed", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
    }

    /**
     * Apply hasDefaultGeometry for every Feature with a single hasGeometry
     * property.
     *
     * @param inputFile
     * @param inputLang
     * @return Output model.
     */
    public static final Model applyDefaultGeometry(File inputFile, Lang inputLang) {
        Model model = ModelFactory.createDefaultModel();
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            RDFDataMgr.read(model, inputStream, inputLang);
            applyDefaultGeometry(model);
        } catch (IOException ex) {
            LOGGER.error("Input File IO Exception: {} - {}", inputFile.getAbsolutePath(), ex.getMessage());
        }

        return model;
    }

    /**
     * Apply (to a folder of RDF files) hasDefaultGeometry for every Feature
     * with a single hasGeometry property.
     * <br> Only RDF files should be in the input folder and must all be the
     * same RDF * language.
     *
     * @param inputFolder
     * @param inputLang
     * @param outputFolder
     * @param outputLang
     */
    public static final void applyDefaultGeometryFolder(File inputFolder, Lang inputLang, File outputFolder, Lang outputLang) {

        LOGGER.info("Applying Predicates from Folder {} to {} - Started", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath());
        if (inputFolder.exists()) {
            File[] inputFiles = inputFolder.listFiles();

            if (inputFiles.length > 0) {
                outputFolder.mkdir();

                for (File inputFile : inputFiles) {
                    File outputFile = new File(outputFolder, inputFile.getName());
                    try {
                        applyDefaultGeometry(inputFile, inputLang, outputFile, outputLang);
                    } catch (Exception ex) {
                        LOGGER.error("{} for input {}. The output file {} may not be created.", ex.getMessage(), inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
                    }
                }

            } else {
                LOGGER.warn("{} is empty. {} is not created.", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath());
            }

        } else {
            LOGGER.warn("{} does not exist. {} is not created.", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath());
        }
        LOGGER.info("Applying Predicates from Folder {} to {} - Completed", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath());
    }

    /**
     * Load GeoSPARQL v1.0 (corrected version) as a Model.
     *
     * @return Model containing the schema.
     */
    public static final Model loadGeoSPARQLSchema() {
        Model geosparqlSchema = ModelFactory.createDefaultModel();
        InputStream inputStream = GeoSPARQLOperations.class.getClassLoader().getResourceAsStream(GEOSPARQL_SCHEMA_FILE);
        RDFDataMgr.read(geosparqlSchema, inputStream, Lang.RDFXML);

        return geosparqlSchema;
    }

    /**
     * Apply GeoSPARQL inferencing using GeoSPARPQL v1.0 (corrected version) and
     * RDFS reasoner.<br>
     * Statements will be added to the dataset.
     *
     * @param dataset
     */
    public static final void applyInferencing(Dataset dataset) {
        Model geosparqlSchema = loadGeoSPARQLSchema();
        applyInferencing(geosparqlSchema, dataset);
    }

    /**
     * Apply GeoSPARQL inferencing using schema model and RDFS reasoner.<br>
     * Statements will be added to the Dataset.
     *
     * @param geosparqlSchema
     * @param dataset
     */
    public static final void applyInferencing(Model geosparqlSchema, Dataset dataset) {

        LOGGER.info("Applying GeoSPARQL Schema - Started");

        try {
            //Default Model
            dataset.begin(ReadWrite.WRITE);
            Model defaultModel = dataset.getDefaultModel();
            applyInferencing(geosparqlSchema, defaultModel, "default");

            //Named Models
            Iterator<String> graphNames = dataset.listNames();
            while (graphNames.hasNext()) {
                String graphName = graphNames.next();
                Model namedModel = dataset.getNamedModel(graphName);
                applyInferencing(geosparqlSchema, namedModel, graphName);
            }

            dataset.commit();
            LOGGER.info("Applying GeoSPARQL Schema - Completed");
        } catch (Exception ex) {
            LOGGER.error("Inferencing Error: {}", ex.getMessage());
        } finally {
            dataset.end();
        }
    }

    /**
     * Apply GeoSPARQL inferencing using GeoSPARPQL v1.0 (corrected version) and
     * RDFS reasoner.<br>
     * Statements will be added to the dataModel.
     *
     * @param dataModel
     */
    public static final void applyInferencing(Model dataModel) {
        Model geosparqlSchema = loadGeoSPARQLSchema();
        applyInferencing(geosparqlSchema, dataModel);
    }

    /**
     * Apply GeoSPARQL inferencing using schema model and RDFS reasoner.<br>
     * Statements will be added to the Model.
     *
     * @param geosparqlSchema
     * @param model
     */
    public static final void applyInferencing(Model geosparqlSchema, Model model) {
        applyInferencing(geosparqlSchema, model, "unnamed");
    }

    /**
     * Apply GeoSPARQL inferencing using schema model and RDFS reasoner.<br>
     * Statements will be added to the Model.<br>
     * Graph name supplied for logging purposes only.
     *
     * @param geosparqlSchema
     * @param model
     * @param graphName
     */
    public static final void applyInferencing(Model geosparqlSchema, Model model, String graphName) {
        if (!model.isEmpty()) {
            InfModel infModel = ModelFactory.createRDFSModel(geosparqlSchema, model);
            model.add(infModel);
            LOGGER.info("GeoSPARQL schema applied to graph: {}", graphName);
        } else {
            LOGGER.info("GeoSPARQL schema not applied to empty graph: {}", graphName);
        }
    }

    /**
     * Prepare an empty GeoSPARQL model with RDFS reasoning.
     * <br> In-memory indexing applied by default.
     * <br> This can be changed by calling GeoSPARQLConfig methods.
     *
     * @return Output model.
     */
    public static final InfModel prepare() {
        return prepareRDFS(ModelFactory.createDefaultModel());
    }

    /**
     * Prepare a GeoSPARQL model from an existing model with RDFS reasoning.
     * <br> In-memory indexing applied by default.
     * <br> This can be changed by calling GeoSPARQLConfig methods.
     *
     * @param model
     * @return Output model.
     */
    public static final InfModel prepareRDFS(Model model) {
        return prepare(model, ReasonerRegistry.getRDFSReasoner());
    }

    /**
     * Prepare a GeoSPARQL model from an existing model with alternative
     * Reasoner, e.g. OWL.
     * <br> In-memory indexing applied by default.
     * <br> This can be changed by calling GeoSPARQLConfig methods.
     *
     * @param model
     * @param reasoner
     * @return Output model.
     */
    public static final InfModel prepare(Model model, Reasoner reasoner) {
        InputStream geosparqlSchemaInputStream = GeoSPARQLOperations.class.getClassLoader().getResourceAsStream(GEOSPARQL_SCHEMA_FILE);
        return prepare(geosparqlSchemaInputStream, model, reasoner);
    }

    /**
     * Prepare a GeoSPARQL model from file with RDFS reasoning.
     * <br> In-memory indexing applied by default.
     * <br> This can be changed by calling GeoSPARQLConfig methods.
     *
     * @param inputStream
     * @return Output model.
     */
    public static final InfModel prepareRDFS(InputStream inputStream) {
        return prepare(inputStream, ReasonerRegistry.getRDFSReasoner());
    }

    /**
     * Prepare a GeoSPARQL model from file with alternative Reasoner, e.g. OWL.
     * <br> In-memory indexing applied by default.
     * <br> This can be changed by calling GeoSPARQLConfig methods.
     *
     * @param inputStream
     * @param reasoner
     * @return Output model.
     */
    public static final InfModel prepare(InputStream inputStream, Reasoner reasoner) {
        Model model = ModelFactory.createDefaultModel();
        model.read(inputStream, null);

        return prepare(model, reasoner);
    }

    /**
     * Prepare a model from an existing model with alternative GeoSPARQL schema
     * and Reasoner, e.g. OWL.
     * <br> In-memory indexing applied by default.
     * <br> This can be changed by calling GeoSPARQLConfig methods.
     *
     * @param geosparqlSchemaInputStream
     * @param model
     * @param reasoner
     * @return Output model.
     */
    public static final InfModel prepare(InputStream geosparqlSchemaInputStream, Model model, Reasoner reasoner) {

        //Load GeoSPARQL Schema
        Model schema = ModelFactory.createDefaultModel();
        schema.read(geosparqlSchemaInputStream, null);

        //Apply the schema to the reasoner.
        reasoner = reasoner.bindSchema(schema);

        //Setup inference model.
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);

        return infModel;
    }

    /**
     * Validate Geometry Literal in Dataset.
     *
     * @param dataset
     * @return Whether dataset is valid.
     */
    public static final boolean validateGeometryLiteral(Dataset dataset) {

        boolean isValid = true;

        LOGGER.info("Validate Geometry Literal - Started");
        //Default Model
        dataset.begin(ReadWrite.READ);
        Model defaultModel = dataset.getDefaultModel();
        GeoSPARQLOperations.validateGeometryLiteral(defaultModel);

        //Named Models
        Iterator<String> graphNames = dataset.listNames();
        while (graphNames.hasNext()) {
            String graphName = graphNames.next();
            Model namedModel = dataset.getNamedModel(graphName);
            boolean isModelValid = GeoSPARQLOperations.validateGeometryLiteral(namedModel);

            if (!isModelValid) {
                isValid = false;
            }
        }

        LOGGER.info("Validate Geometry Literal - Completed");
        dataset.end();

        return isValid;
    }

    /**
     * Validate Geometry Literal in Model.
     *
     * @param model
     * @return Whether model is valid.
     */
    public static final boolean validateGeometryLiteral(Model model) {

        //Get current state of index and switch it off temporarily.
        boolean isIndexActive = GeometryLiteralIndex.isIndexActive();
        GeometryLiteralIndex.setIndexActive(false);

        boolean isValid = true;
        NodeIterator nodeIt = model.listObjectsOfProperty(Geo.HAS_DEFAULT_GEOMETRY_PROP);
        while (nodeIt.hasNext()) {
            RDFNode node = nodeIt.nextNode();

            try {
                Literal geometryLiteral = node.asLiteral();
                GeometryWrapper.extract(geometryLiteral);
            } catch (DatatypeFormatException ex) {
                //Error messages should already have been issued. Catch exception so can continue on whole dataset.
                isValid = false;
            }
        }

        //Switch index back on if it was on.
        GeometryLiteralIndex.setIndexActive(isIndexActive);

        return isValid;
    }

    /**
     * Find the most frequent SRS URI of Geometry Literals in the dataset.
     *
     * @param dataset
     * @return SRS URI
     */
    public static final String findModeSRS(Dataset dataset) throws SrsException {
        LOGGER.info("Find Mode SRS - Started");
        ModeSRS modeSRS = new ModeSRS();
        //Default Model
        dataset.begin(ReadWrite.READ);
        Model defaultModel = dataset.getDefaultModel();
        modeSRS.search(defaultModel);

        //Named Models
        Iterator<String> graphNames = dataset.listNames();
        while (graphNames.hasNext()) {
            String graphName = graphNames.next();
            Model namedModel = dataset.getNamedModel(graphName);
            modeSRS.search(namedModel);
        }

        LOGGER.info("Find Mode SRS - Completed");
        dataset.end();

        return modeSRS.getModeURI();
    }

    /**
     * Find the most frequent SRS URI of Geometry Literals in the model.
     *
     * @param model
     * @return SRS URI
     */
    public static final String findModeSRS(Model model) throws SrsException {
        ModeSRS modeSRS = new ModeSRS();
        modeSRS.search(model);
        return modeSRS.getModeURI();
    }

    /**
     * Converts all geometry literals (WKT or GML) from current SRS to the
     * specified SRS.
     *
     * @param inputFile
     * @param inputLang
     * @param outputFile
     * @param outputLang
     * @param outputSrsURI
     */
    public static final void convertFile(File inputFile, Lang inputLang, File outputFile, Lang outputLang, String outputSrsURI) {
        convertFileSRSDatatype(inputFile, inputLang, outputFile, outputLang, outputSrsURI, null);
    }

    /**
     * Converts file between serialisations.
     *
     * @param inputFile
     * @param inputLang
     * @param outputFile
     * @param outputLang
     */
    public static final void convertFile(File inputFile, Lang inputLang, File outputFile, Lang outputLang) {
        convertFileSRSDatatype(inputFile, inputLang, outputFile, outputLang, null, null);
    }

    /**
     * Converts all geometry literals (WKT or GML) from current SRS to the
     * specified SRS and datatype.
     *
     * @param inputFile
     * @param inputLang
     * @param outputFile
     * @param outputLang
     * @param outputSrsURI
     * @param outputDatatype
     */
    public static final void convertFile(File inputFile, Lang inputLang, File outputFile, Lang outputLang, String outputSrsURI, GeometryDatatype outputDatatype) {
        convertFileSRSDatatype(inputFile, inputLang, outputFile, outputLang, outputSrsURI, outputDatatype);
    }

    /**
     * Converts all geometry literals (WKT or GML) to the specified datatype.
     *
     * @param inputFile
     * @param inputLang
     * @param outputFile
     * @param outputLang
     * @param outputDatatype
     */
    public static final void convertFile(File inputFile, Lang inputLang, File outputFile, Lang outputLang, GeometryDatatype outputDatatype) {
        convertFileSRSDatatype(inputFile, inputLang, outputFile, outputLang, null, outputDatatype);
    }

    private static Model convertSRSDatatype(Model inputModel, String outputSrsURI, GeometryDatatype outputDatatype) {
        if (outputSrsURI == null) {
            outputSrsURI = GeoSPARQLOperations.findModeSRS(inputModel);
        }
        if (outputDatatype == null || !GeometryDatatype.check(outputDatatype)) {
            LOGGER.warn("Output datatype {} is not a recognised for Geometry Literal. Defaulting to {}.", outputDatatype, WKTDatatype.URI);
            outputDatatype = WKTDatatype.INSTANCE;
        }

        //Iterate through all statements: toNodeValue geometry literals and just add the rest.
        Model outputModel = ModelFactory.createDefaultModel();
        outputModel.setNsPrefixes(inputModel.getNsPrefixMap());
        Iterator<Statement> statementIt = inputModel.listStatements();
        while (statementIt.hasNext()) {
            Statement statement = statementIt.next();
            RDFNode object = statement.getObject();
            if (object.isLiteral()) {
                handleLiteral(statement, outputModel, outputSrsURI, outputDatatype);
            } else {
                //Not a statement of interest so store for output.
                outputModel.add(statement);
            }
        }
        return outputModel;
    }

    /**
     * Convert a list of strings representation of geometry literals to another
     * coordinate reference system.
     *
     * @param geometryLiterals
     * @param outputSrsURI Coordinate reference system URI
     * @param outputDatatype
     * @return Output of conversion.
     */
    public static final List<String> convertGeometryLiterals(List<String> geometryLiterals, String outputSrsURI, GeometryDatatype outputDatatype) {
        List<String> outputGeometryLiterals = new ArrayList<>(geometryLiterals.size());
        for (String geometryLiteral : geometryLiterals) {
            String convertedGeometryLiteral = convertGeometryLiteral(geometryLiteral, outputSrsURI, outputDatatype);
            outputGeometryLiterals.add(convertedGeometryLiteral);
        }
        return outputGeometryLiterals;
    }

    public static void writeOutputModel(Model outputModel, File outputFile, Lang outputLang, File inputFile) {
        if (!outputModel.isEmpty()) {
            try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                RDFDataMgr.write(outputStream, outputModel, outputLang);
            } catch (IOException ex) {
                LOGGER.error("Output File IO Exception: {} - {}", outputFile.getAbsolutePath(), ex.getMessage());
            }
        } else {
            LOGGER.warn("Output Model is empty for {}: Did not create: {}", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
        }
    }

    private static void handleLiteral(Statement statement, Model outputModel, String outputSrsURI, GeometryDatatype outputDatatype) {
        Literal literal = statement.getLiteral();
        RDFDatatype datatype = literal.getDatatype();
        //Check whether a supported geometry literal.
        if (GeometryDatatype.check(datatype)) {
            GeometryWrapper originalGeom = GeometryWrapper.extract(literal);
            GeometryWrapper convertedGeom;
            try {
                if (outputSrsURI != null) {
                    convertedGeom = originalGeom.convertSRS(outputSrsURI);
                } else {
                    convertedGeom = originalGeom;
                }
            } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
                LOGGER.error("SRS Conversion Exception: {} - Literal: {}, Output SRS URI: {}. Reusing original literal for output.", ex.getMessage(), literal, outputSrsURI);
                convertedGeom = originalGeom;
            }
            if (outputDatatype == null) {
                outputDatatype = GeometryDatatype.get(datatype);
            }
            Literal convertedGeometryLiteral = convertedGeom.asLiteral(outputDatatype);
            Statement outputStatement = ResourceFactory.createStatement(statement.getSubject(), statement.getPredicate(), convertedGeometryLiteral);
            outputModel.add(outputStatement);
        } else {
            //Not a statement of interest so store for output.
            outputModel.add(statement);
        }
    }

    /**
     * Only RDF files should be in the input folder and must all be the same RDF
     * language.
     *
     * @param inputFolder
     * @param inputLang
     * @param outputFolder
     * @param outputLang
     * @param outputSrsURI
     */
    public static final void convertFolder(File inputFolder, Lang inputLang, File outputFolder, Lang outputLang, String outputSrsURI) {
        convertFolderSRSDatatype(inputFolder, inputLang, outputFolder, outputLang, outputSrsURI, null);
    }

    /**
     * Only RDF files should be in the input folder and must all be the same RDF
     * language.
     *
     * @param inputFolder
     * @param inputLang
     * @param outputFolder
     * @param outputLang
     */
    public static final void convertFolder(File inputFolder, Lang inputLang, File outputFolder, Lang outputLang) {
        convertFolderSRSDatatype(inputFolder, inputLang, outputFolder, outputLang, null, null);
    }

    /**
     * Only RDF files should be in the input folder and must all be the same RDF
     * language. Output will be in the specified SRS and datatype/serialisation.
     *
     * @param inputFolder
     * @param inputLang
     * @param outputFolder
     * @param outputLang
     * @param outputSrsURI
     * @param outputDatatype
     */
    public static final void convertFolder(File inputFolder, Lang inputLang, File outputFolder, Lang outputLang, String outputSrsURI, GeometryDatatype outputDatatype) {
        convertFolderSRSDatatype(inputFolder, inputLang, outputFolder, outputLang, outputSrsURI, outputDatatype);
    }

    /**
     * Only RDF files should be in the input folder and must all be the same RDF
     * language. Output will be in the specified datatype/serialisation.
     *
     * @param inputFolder
     * @param inputLang
     * @param outputFolder
     * @param outputLang
     * @param outputDatatype
     */
    public static final void convertFolder(File inputFolder, Lang inputLang, File outputFolder, Lang outputLang, GeometryDatatype outputDatatype) {
        convertFolderSRSDatatype(inputFolder, inputLang, outputFolder, outputLang, null, outputDatatype);
    }

    /**
     * Convert Geo Predicates (Lat/Lon) in Dataset to WKT Geometry Literal.<br>
     * Option to remove Lat and Lon predicates after combining.
     *
     * @param dataset
     * @param isRemoveGeoPredicate
     * @return Converted dataset.
     *
     */
    public static final Dataset convertGeoPredicates(Dataset dataset, boolean isRemoveGeoPredicate) {
        LOGGER.info("Convert Geo Predicates - Started");
        Dataset outputDataset = DatasetFactory.createTxnMem();
        outputDataset.begin(ReadWrite.WRITE);
        //Default Model
        dataset.begin(ReadWrite.READ);
        Model defaultModel = dataset.getDefaultModel();
        Model convertedModel = convertGeoPredicates(defaultModel, isRemoveGeoPredicate);
        convertedModel.setNsPrefixes(defaultModel.getNsPrefixMap());
        outputDataset.setDefaultModel(convertedModel);
        //Named Models
        Iterator<String> graphNames = dataset.listNames();
        while (graphNames.hasNext()) {
            String graphName = graphNames.next();
            Model namedModel = dataset.getNamedModel(graphName);
            Model convertedNamedModel = convertGeoPredicates(namedModel, isRemoveGeoPredicate);
            convertedNamedModel.setNsPrefixes(namedModel.getNsPrefixMap());
            outputDataset.addNamedModel(graphName, convertedNamedModel);
        }
        LOGGER.info("Convert Geo Predicates - Completed");
        dataset.end();
        outputDataset.commit();
        outputDataset.end();
        return outputDataset;
    }

    /**
     * Convert Geo Predicates (Lat/Lon) in Model to WKT Geometry Literal.<br>
     * Option to remove Lat and Lon predicates after combining.
     *
     * @param model
     * @param isRemoveGeoPredicates
     * @return Converted model.
     */
    public static final Model convertGeoPredicates(Model model, boolean isRemoveGeoPredicates) {
        Model outputModel = ModelFactory.createDefaultModel();
        outputModel.add(model);
        outputModel.setNsPrefixes(model.getNsPrefixMap());
        if (outputModel.containsResource(SpatialExtension.GEO_LAT_PROP)) {
            ResIterator resIt = outputModel.listSubjectsWithProperty(SpatialExtension.GEO_LAT_PROP);
            while (resIt.hasNext()) {
                Resource feature = resIt.nextResource();
                if (feature.hasProperty(SpatialExtension.GEO_LON_PROP) && feature.hasProperty(SpatialExtension.GEO_LAT_PROP)) {
                    //Create a GeometryLiteral from Lat/Lon
                    Literal lat = feature.getProperty(SpatialExtension.GEO_LAT_PROP).getLiteral();
                    Literal lon = feature.getProperty(SpatialExtension.GEO_LON_PROP).getLiteral();
                    try {
                        Literal latLonPoint = ConvertLatLon.toLiteral(lat.getFloat(), lon.getFloat());
                        Resource geometry = createGeometry(feature);

                        //Add Geometry to Feature and GeometryLiteral to Geometry.
                        outputModel.add(feature, Geo.HAS_GEOMETRY_PROP, geometry);
                        outputModel.add(geometry, Geo.HAS_SERIALIZATION_PROP, latLonPoint);
                    } catch (DatatypeFormatException ex) {
                        LOGGER.error("Feature: {} has geo lat/lon out of bounds. Lat: {}, Lon: {}", feature, lat, lon);
                    }
                }
            }
            if (isRemoveGeoPredicates) {
                outputModel.removeAll(null, SpatialExtension.GEO_LAT_PROP, null);
                outputModel.removeAll(null, SpatialExtension.GEO_LON_PROP, null);
            }
        }
        return outputModel;
    }

    private static Resource createGeometry(Resource feature) {
        //Create a Geometry - re-use Feature if a URI or build a URI for blank node.
        String geometryURI;
        if (feature.isURIResource()) {
            geometryURI = feature.getURI() + "-Geom-" + UUID.randomUUID().toString();
        } else {
            geometryURI = GeoSPARQL_URI.GEO_URI + "Geom-" + UUID.randomUUID().toString();
        }
        Resource geometry = ResourceFactory.createResource(geometryURI);

        return geometry;
    }

    /**
     * Convert Geometry Datatypes (WKT, GML, etc.) in Model to GeoSPARQL
     * structure.<br>
     * (Subject-property-GeometryLiteral) becomes (Feature-hasGeometry-Geometry)
     * and (Geometry-hasSerialization-GeometryLiteral).<br>
     * Original property will be removed from resulting Dataset.
     *
     *
     * @param dataset
     * @return Converted dataset.
     *
     */
    public static final Dataset convertGeometryStructure(Dataset dataset) {
        LOGGER.info("Convert Geometry Structure - Started");
        Dataset outputDataset = DatasetFactory.createTxnMem();
        outputDataset.begin(ReadWrite.WRITE);
        //Default Model
        dataset.begin(ReadWrite.READ);
        Model defaultModel = dataset.getDefaultModel();
        Model convertedModel = convertGeometryStructure(defaultModel);
        convertedModel.setNsPrefixes(defaultModel.getNsPrefixMap());
        outputDataset.setDefaultModel(convertedModel);
        //Named Models
        Iterator<String> graphNames = dataset.listNames();
        while (graphNames.hasNext()) {
            String graphName = graphNames.next();
            Model namedModel = dataset.getNamedModel(graphName);
            Model convertedNamedModel = convertGeometryStructure(namedModel);
            convertedNamedModel.setNsPrefixes(namedModel.getNsPrefixMap());
            outputDataset.addNamedModel(graphName, convertedNamedModel);
        }
        LOGGER.info("Convert Geometry Structure - Completed");
        dataset.end();
        outputDataset.commit();
        outputDataset.end();
        return outputDataset;
    }

    /**
     * Convert Geometry Datatypes (WKT, GML, etc.) in Model to GeoSPARQL
     * structure.<br>
     * (Subject-property-GeometryLiteral) becomes (Feature-hasGeometry-Geometry)
     * and (Geometry-hasSerialization-GeometryLiteral).<br>
     * Original property will be removed from resulting Model.
     *
     * @param model
     * @return Converted model.
     */
    public static final Model convertGeometryStructure(Model model) {

        Model outputModel = ModelFactory.createDefaultModel();
        outputModel.add(model);
        outputModel.setNsPrefixes(model.getNsPrefixMap());

        List<Statement> additionalStatements = new ArrayList<>();

        StmtIterator stmtIter = outputModel.listStatements();

        while (stmtIter.hasNext()) {
            Statement stmt = stmtIter.nextStatement();
            RDFNode object = stmt.getObject();
            if (object.isLiteral()) {
                Literal literal = object.asLiteral();
                RDFDatatype datatype = literal.getDatatype();
                if (GeometryDatatype.check(datatype)) {

                    Property property = stmt.getPredicate();
                    if (property.equals(Geo.HAS_SERIALIZATION_PROP) || property.equals(Geo.AS_WKT_PROP) || property.equals(Geo.AS_GML_PROP)) {
                        //Model already contains the GeoSPARQL properties for this literal so skipping.
                        continue;
                    }

                    if (outputModel.contains(property, RDFS.subPropertyOf, Geo.HAS_SERIALIZATION_PROP)) {
                        //The property is a sub property of hasSerialization so skipping. Only RDFS inferencing needs to be applied.
                        continue;
                    }

                    Resource feature = stmt.getSubject();
                    Resource geometry = createGeometry(feature);

                    additionalStatements.add(ResourceFactory.createStatement(feature, Geo.HAS_GEOMETRY_PROP, geometry));
                    additionalStatements.add(ResourceFactory.createStatement(geometry, Geo.HAS_SERIALIZATION_PROP, literal));
                    stmtIter.remove();
                }
            }
        }

        outputModel.add(additionalStatements);

        return outputModel;
    }

    private static void convertFileSRSDatatype(File inputFile, Lang inputLang, File outputFile, Lang outputLang, String outputSrsURI, GeometryDatatype outputDatatype) {
        LOGGER.info("Converting File: {} to {} in srs URI: {} - Started", inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), outputSrsURI);
        Model inputModel = ModelFactory.createDefaultModel();
        try (final FileInputStream inputStream = new FileInputStream(inputFile)) {
            RDFDataMgr.read(inputModel, inputStream, inputLang);
        } catch (IOException ex) {
            LOGGER.error("Input File IO Exception: {} - {}", inputFile.getAbsolutePath(), ex.getMessage());
        }
        Model outputModel = convertSRSDatatype(inputModel, outputSrsURI, outputDatatype);
        //Write the output.
        writeOutputModel(outputModel, outputFile, outputLang, inputFile);
        LOGGER.info("Converting File: {} to {} in srs URI: {} - Completed", inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), outputSrsURI);
    }

    private static void convertFolderSRSDatatype(File inputFolder, Lang inputLang, File outputFolder, Lang outputLang, String outputSrsURI, GeometryDatatype outputDatatype) {
        LOGGER.info("Converting Folder {} to {} in srs URI: {} - Started", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath(), outputSrsURI);
        if (inputFolder.exists()) {
            File[] inputFiles = inputFolder.listFiles();
            if (inputFiles.length > 0) {
                outputFolder.mkdir();
                for (File inputFile : inputFiles) {
                    File outputFile = new File(outputFolder, inputFile.getName());
                    try {
                        convertFile(inputFile, inputLang, outputFile, outputLang, outputSrsURI, outputDatatype);
                    } catch (Exception ex) {
                        LOGGER.error("{} for input {}. The output file {} may not be created.", ex.getMessage(), inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
                    }
                }
            } else {
                LOGGER.warn("{} is empty. {} is not created.", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath());
            }
        } else {
            LOGGER.warn("{} does not exist. {} is not created.", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath());
        }
        LOGGER.info("Converting Folder {} to {} in srs URI: {} - Completed", inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath(), outputSrsURI);
    }

    /**
     * Convert a string representation of a geometry literal to another
     * coordinate reference system.
     *
     * @param geometryLiteral
     * @param outputSrsURI Coordinate reference system URI
     * @param outputDatatype
     * @return Output of conversion.
     */
    public static final String convertGeometryLiteral(String geometryLiteral, String outputSrsURI, GeometryDatatype outputDatatype) {
        Literal lit = ResourceFactory.createTypedLiteral(geometryLiteral, outputDatatype);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(lit);
        try {
            GeometryWrapper transformedGeometryWrapper = geometryWrapper.convertSRS(outputSrsURI);
            Literal transformedLit = transformedGeometryWrapper.asLiteral();
            return transformedLit.getLexicalForm();
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            LOGGER.error("{} : {} : {}", ex.getMessage(), geometryLiteral, outputSrsURI);
            return null;
        }
    }

    /**
     * Convert the input model to the most frequent coordinate reference system
     * and default datatype.
     *
     * @param inputModel
     * @return Output of conversion.
     */
    public static final Model convert(Model inputModel) {
        return convertSRSDatatype(inputModel, null, null);
    }

    /**
     * Convert the input model to the output coordinate reference system.
     *
     * @param inputModel
     * @param outputSrsURI
     * @return Output of conversion.
     */
    public static final Model convert(Model inputModel, String outputSrsURI) {
        return convertSRSDatatype(inputModel, outputSrsURI, null);
    }

    /**
     * Convert the input model to the output geometry literal datatype.
     *
     * @param inputModel
     * @param outputDatatype
     * @return Output of conversion.
     */
    public static final Model convert(Model inputModel, GeometryDatatype outputDatatype) {
        return convertSRSDatatype(inputModel, null, outputDatatype);
    }

    /**
     * Convert the input model to the output coordinate reference system and
     * geometry literal datatype.
     *
     * @param inputModel
     * @param outputSrsURI
     * @param outputDatatype
     * @return Output of conversion.
     */
    public static final Model convert(Model inputModel, String outputSrsURI, GeometryDatatype outputDatatype) {
        return convertSRSDatatype(inputModel, outputSrsURI, outputDatatype);
    }

    /**
     * Convert the input model to the output coordinate reference system and
     * geometry literal datatype.
     *
     * @param inputModel
     * @param outputSrsURI
     * @param outputDatatypeURI
     * @return Output of conversion.
     */
    public static final Model convert(Model inputModel, String outputSrsURI, String outputDatatypeURI) {
        return convertSRSDatatype(inputModel, outputSrsURI, GeometryDatatype.get(outputDatatypeURI));
    }

    /**
     * Convert the input dataset to the most frequent coordinate reference
     * system and default datatype.
     *
     * @param dataset
     * @return Converted dataset.
     */
    public static final Dataset convert(Dataset dataset) {
        return convert(dataset, null, null);
    }

    /**
     * Convert the input dataset to the output coordinate reference system.
     *
     * @param dataset
     * @param outputSrsURI
     * @return Converted dataset.
     */
    public static final Dataset convert(Dataset dataset, String outputSrsURI) {
        return convert(dataset, outputSrsURI, null);
    }

    /**
     * Convert the input dataset to the output geometry literal datatype.
     *
     * @param dataset
     * @param outputDatatype
     * @return Converted dataset.
     */
    public static final Dataset convert(Dataset dataset, GeometryDatatype outputDatatype) {
        return convert(dataset, null, outputDatatype);
    }

    /**
     * Convert the input dataset to the output coordinate reference system and
     * geometry literal datatype.
     *
     * @param inputDataset
     * @param outputSrsURI
     * @param outputDatatype
     * @return Converted dataset.
     */
    public static final Dataset convert(Dataset inputDataset, String outputSrsURI, GeometryDatatype outputDatatype) {
        LOGGER.info("Convert Dataset - Started SRS: {}, Datatype: {}", outputSrsURI, outputDatatype);
        if (outputSrsURI == null) {
            outputSrsURI = GeoSPARQLOperations.findModeSRS(inputDataset);
            LOGGER.info("SRS URI not specified. Defaulting to most frequent SRS URI: {}", outputSrsURI);
        }
        if (outputDatatype == null || !GeometryDatatype.check(outputDatatype)) {
            LOGGER.warn("Output datatype {} is not a recognised for Geometry Literal. Defaulting to {}.", outputDatatype, WKTDatatype.URI);
            outputDatatype = WKTDatatype.INSTANCE;
        }
        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.begin(ReadWrite.WRITE);
        //Default Model
        inputDataset.begin(ReadWrite.READ);
        Model defaultModel = inputDataset.getDefaultModel();
        Model convertedModel = convertSRSDatatype(defaultModel, outputSrsURI, outputDatatype);
        dataset.setDefaultModel(convertedModel);
        //Named Models
        Iterator<String> graphNames = inputDataset.listNames();
        while (graphNames.hasNext()) {
            String graphName = graphNames.next();
            Model namedModel = inputDataset.getNamedModel(graphName);
            Model convertedNamedModel = convertSRSDatatype(namedModel, outputSrsURI, outputDatatype);
            dataset.addNamedModel(graphName, convertedNamedModel);
        }
        LOGGER.info("Convert Dataset - Completed SRS: {}, Datatype: {}", outputSrsURI, outputDatatype);
        dataset.commit();
        dataset.end();
        inputDataset.end();
        return dataset;
    }

    /**
     *
     * @param model
     * @param graphName Name of graph for logging purposes.
     * @return Number of Geometry Literals contained in Model.
     */
    public static final int countGeometryLiterals(Model model, String graphName) {
        Set<String> literalStrings = new TreeSet<>();
        Iterator<Statement> iterator = model.listStatements(null, Geo.HAS_SERIALIZATION_PROP, (RDFNode) null);
        int count = 0;
        while (iterator.hasNext()) {
            Statement st = iterator.next();
            String literalString = st.getLiteral().getString();
            literalStrings.add(literalString);
            count++;
        }
        LOGGER.info("Graph: {} has {} unique out of {} Geometry Literals.", graphName, literalStrings.size(), count);
        return count;
    }

    /**
     *
     * @param dataset
     * @return Count of Geometry Literals in whole Dataset.
     */
    public static final int countGeometryLiterals(Dataset dataset) {
        dataset.begin(ReadWrite.READ);
        Model defaultModel = dataset.getDefaultModel();
        int count = countGeometryLiterals(defaultModel, "Default Model");
        Iterator<String> iterator = dataset.listNames();
        while (iterator.hasNext()) {
            String graphName = iterator.next();
            Model namedModel = dataset.getNamedModel(graphName);
            count += countGeometryLiterals(namedModel, graphName);
        }
        dataset.end();
        return count;
    }

    /**
     * Apply a set of commonly used prefixes for GeoSPARQL URIs to the Model.
     *
     * @param model
     */
    public static final void applyPrefixes(Model model) {
        HashMap<String, String> geoPrefixes = GeoSPARQL_URI.getPrefixes();
        model.setNsPrefixes(geoPrefixes);
    }

    /**
     * Apply a set of commonly used prefixes for GeoSPARQL URIs to the whole
     * Dataset.
     *
     * @param dataset
     */
    public static final void applyPrefixes(Dataset dataset) {
        dataset.begin(ReadWrite.READ);
        Model defaultModel = dataset.getDefaultModel();
        applyPrefixes(defaultModel);
        Iterator<String> iterator = dataset.listNames();
        while (iterator.hasNext()) {
            String graphName = iterator.next();
            Model namedModel = dataset.getNamedModel(graphName);
            applyPrefixes(namedModel);
        }
        dataset.commit();
        dataset.end();
    }

}
