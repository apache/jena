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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Greg Albiston
 */
public class PrefixReader {

    //TODO - load default prefixes and from file.
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static HashMap<String, String> read(File inputFile) {

        LOGGER.info("Prefix Parsing Started: {}", inputFile.getPath());
        HashMap<String, String> map = new HashMap<>();
        int lineNumber = 1;

        CSVParserBuilder parserBuilder = new CSVParserBuilder().withSeparator(DefaultValues.COLUMN_DELIMITER);
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(inputFile)).withCSVParser(parserBuilder.build()).build()) {

            if (reader.readNext().length != 2) {
                LOGGER.error("Should only be two columns");
            } else {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    lineNumber++;
                    String prefix = line[0].trim();
                    String uri = line[1].trim();

                    map.put(prefix, uri);
                }
            }

        } catch (IOException | RuntimeException | CsvValidationException ex) {
            LOGGER.error("PrefixReader: Line - {}, File - {}, Exception - {}", lineNumber, inputFile.getAbsolutePath(), ex.getMessage());
        }

        return map;
    }

}
