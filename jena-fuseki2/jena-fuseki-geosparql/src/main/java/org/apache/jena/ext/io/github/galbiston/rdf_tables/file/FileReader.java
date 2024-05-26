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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.ext.io.github.galbiston.rdf_tables.cli.FormatParameter;
import org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.PrefixController;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class FileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Convert the directory to the Jena model.
     *
     * @param inputDirectory
     * @param outputFile
     * @param prefixesFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertDirectory(File inputDirectory, File outputFile, File prefixesFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        return convertDirectory(inputDirectory, Arrays.asList(), outputFile, prefixesFile, rdfFormat, delimiter, isNamedIndividual);
    }

    /**
     * Convert the directory to the Jena model.
     *
     * @param inputDirectory
     * @param excludedFile
     * @param outputFile
     * @param prefixesFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertDirectory(File inputDirectory, File excludedFile, File outputFile, File prefixesFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        return convertDirectory(inputDirectory, Arrays.asList(excludedFile), outputFile, prefixesFile, rdfFormat, delimiter, isNamedIndividual);
    }

    /**
     * Convert the directory to the Jena model.
     *
     * @param inputDirectory
     * @param excludedFiles
     * @param outputFile
     * @param prefixesFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertDirectory(File inputDirectory, List<File> excludedFiles, File outputFile, File prefixesFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        List<File> inputFiles = Arrays.asList(inputDirectory.listFiles());
        return convertFiles(inputFiles, excludedFiles, outputFile, prefixesFile, rdfFormat, delimiter, isNamedIndividual);
    }

    /**
     * Convert the directory to the Jena model.
     *
     * @param inputDirectory
     * @param excludedFiles
     * @param outputFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertDirectory(File inputDirectory, List<File> excludedFiles, File outputFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        List<File> inputFiles = Arrays.asList(inputDirectory.listFiles());
        return convertFiles(inputFiles, excludedFiles, outputFile, null, rdfFormat, delimiter, isNamedIndividual);
    }

    /**
     * Convert the directory to the Jena model.
     *
     * @param inputDirectory
     * @param outputFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertDirectory(File inputDirectory, File outputFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        return convertDirectory(inputDirectory, Arrays.asList(), outputFile, null, rdfFormat, delimiter, isNamedIndividual);
    }

    /**
     * Convert the file to a Jena model.
     *
     * @param inputFile
     * @param delimiter
     * @return Output model
     */
    public static Model convertFile(File inputFile, char delimiter) {
        List<File> inputFiles = Arrays.asList(inputFile);
        return convertFiles(inputFiles, new ArrayList<>(), null, null, RDFFormat.TTL, delimiter, false);
    }

    /**
     * Convert the file to a Jena model and output to a file.
     *
     * @param inputFile
     * @param excludedFiles
     * @param outputFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertFile(File inputFile, List<File> excludedFiles, File outputFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        List<File> inputFiles = Arrays.asList(inputFile);
        return convertFiles(inputFiles, excludedFiles, outputFile, null, rdfFormat, delimiter, isNamedIndividual);
    }

    /**
     * Convert the file to a Jena model and output to a file.
     *
     * @param inputFile
     * @param excludedFiles
     * @param outputFile
     * @param prefixesFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertFile(File inputFile, List<File> excludedFiles, File outputFile, File prefixesFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        List<File> inputFiles = Arrays.asList(inputFile);
        return convertFiles(inputFiles, excludedFiles, outputFile, prefixesFile, rdfFormat, delimiter, isNamedIndividual);
    }

    /**
     * Convert the files in the directory to a Jena model and output a file for
     * each to a target directory.
     *
     * @param inputDirectory
     * @param excludedFiles
     * @param outputDirectory
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static HashMap<String, Model> convertDirectories(File inputDirectory, List<File> excludedFiles, File outputDirectory, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        HashMap<String, Model> models = new HashMap<>();
        List<File> inputFiles = Arrays.asList(inputDirectory.listFiles());
        for (File inputFile : inputFiles) {
            File outputFile = new File(outputDirectory.getAbsoluteFile(), FormatParameter.buildFilename(inputFile, rdfFormat));
            Model model = convertFiles(Arrays.asList(inputFile), excludedFiles, outputFile, null, rdfFormat, delimiter, isNamedIndividual);
            models.put(inputFile.getName(), model);
        }
        return models;
    }

    /**
     * Convert the files to a Jena model and output as a file.
     *
     * @param inputFiles
     * @param excludedFiles
     * @param outputFile
     * @param prefixesFile
     * @param rdfFormat
     * @param delimiter
     * @param isNamedIndividual
     * @return Output model
     */
    public static Model convertFiles(List<File> inputFiles, List<File> excludedFiles, File outputFile, File prefixesFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {

        if (prefixesFile != null) {
            HashMap<String, String> prefixMap = PrefixReader.read(prefixesFile);
            PrefixController.addPrefixes(prefixMap);
        }

        Model model = ModelFactory.createDefaultModel();
        for (File inputFile : inputFiles) {
            if (excludedFiles != null && !excludedFiles.contains(inputFile)) {
                FileConverter.writeToModel(inputFile, model, delimiter, isNamedIndividual);
            }
        }

        if (outputFile != null) {
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                RDFDataMgr.write(out, model, rdfFormat);
            } catch (IOException ex) {
                LOGGER.error("IOException: {}, File: {}", ex.getMessage(), outputFile);
            }
        }
        return model;
    }

}
