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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class FileSplitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int LINE_MAXIMUM = 100000;

    public static void splitFiles(File inputFolder, File outputFolder) {

        List<File> inputFiles = Arrays.asList(inputFolder.listFiles());
        outputFolder.mkdir();

        for (File inputFile : inputFiles) {
            File outputFile = new File(outputFolder, inputFile.getName());
            splitFile(inputFile, outputFile);
        }

    }

    private static void splitFile(File inputFile, File outputFile) {

        LOGGER.info("File Splitting Started: {}", inputFile.getPath());

        //Read file
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {

            List<String> lines = new ArrayList<>(LINE_MAXIMUM);
            int fileNumber = 1;
            String header = br.readLine();
            int lineCount = 0;
            String line;
            while ((line = br.readLine()) != null) {
                lineCount++;
                lines.add(line);

                if (lineCount == LINE_MAXIMUM) {
                    writeToFile(lines, fileNumber, outputFile, header);
                    lines.clear();
                    lineCount = 0;
                    fileNumber++;
                }
            }

            writeToFile(lines, fileNumber, outputFile, header);

        } catch (FileNotFoundException ex) {
            LOGGER.error("FileSplitter: {}, {}", inputFile.getAbsolutePath(), ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error("FileSplitter: File - {}, Exception - {}", inputFile.getAbsolutePath(), ex.getMessage());
        }

        LOGGER.info("File Splitting Completed: {}", inputFile.getPath());

    }

    private static void writeToFile(List<String> lines, int fileNumber, File sourceFile, String header) {
        File targetFile = FileSupport.insertFileCount(sourceFile, fileNumber);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
            bw.write(header);
            bw.newLine();
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException ex) {
            LOGGER.error("FileSplitter: File - {}, Exception - {}", targetFile.getAbsolutePath(), ex.getMessage());
        }

    }

}
