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
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.tdb2.TDB2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class TDB2Builder {

    private static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Default input format TTL, comma separated with creation of OWL Named
     * Individuals.
     *
     * @param sourceFolder
     * @param tdbStorageFolder
     * @param targetGraph
     * @param prefixesFile
     */
    public static void compileFolder(File sourceFolder, File tdbStorageFolder, Resource targetGraph, File prefixesFile) {
        compileFolder(sourceFolder, tdbStorageFolder, null, targetGraph, prefixesFile, RDFFormat.TTL, ',', true);
    }

    /**
     * Default input format TTL, comma separated with creation of OWL Named
     * Individuals.
     *
     * @param sourceFolder
     * @param tdbStorageFolder
     * @param outputFile
     * @param targetGraph
     * @param prefixesFile
     */
    public static void compileFolder(File sourceFolder, File tdbStorageFolder, File outputFile, Resource targetGraph, File prefixesFile) {
        compileFolder(sourceFolder, tdbStorageFolder, outputFile, targetGraph, prefixesFile, RDFFormat.TTL, ',', true);
    }

    public static void compileFolder(File sourceFolder, File tdbStorageFolder, Resource targetGraph, File prefixesFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {
        compileFolder(sourceFolder, tdbStorageFolder, null, targetGraph, prefixesFile, rdfFormat, delimiter, isNamedIndividual);
    }

    public static void compileFolder(File sourceFolder, File tdbStorageFolder, File outputFile, Resource targetGraph, File prefixesFile, RDFFormat rdfFormat, char delimiter, Boolean isNamedIndividual) {

        Dataset dataset = TDB2Factory.connectDataset(tdbStorageFolder.getAbsolutePath());
        TDB1Builder.compile(sourceFolder, dataset, outputFile, targetGraph, prefixesFile, rdfFormat, delimiter, isNamedIndividual);
        dataset.close();
    }

}
