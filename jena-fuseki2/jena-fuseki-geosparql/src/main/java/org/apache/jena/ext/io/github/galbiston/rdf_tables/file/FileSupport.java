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
import java.lang.invoke.MethodHandles;

import org.apache.jena.ext.io.github.galbiston.rdf_tables.cli.FormatParameter;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class FileSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static File checkOutputFile(File inputFile, File outputFile, RDFFormat rdfFormat) {

        if (outputFile == null) {
            if (inputFile.isDirectory()) {
                outputFile = inputFile;
            } else {
                String outputFilename = FormatParameter.buildFilename(inputFile, rdfFormat);
                outputFile = new File(inputFile.getParentFile(), outputFilename);
            }
        }
        return outputFile;
    }

    private static final String CSV_EXTENSION = ".csv";

    public static File insertFileCount(File sourceFile, int fileCount) {
        return insertFileCount(sourceFile, fileCount, CSV_EXTENSION);
    }

    public static File insertFileCount(File sourceFile, int fileCount, String fileExtension) {
        String sourceFilename = sourceFile.getAbsolutePath();
        int endIndex = sourceFilename.indexOf(fileExtension);
        //No CSV file extension so set to the end of the file.
        if (endIndex == -1) {
            endIndex = sourceFilename.length();
        }
        String targetFilename = sourceFilename.substring(0, endIndex) + fileCount + fileExtension;
        return new File(targetFilename);
    }

}
