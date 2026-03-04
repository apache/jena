/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.text.cmd;

import org.apache.jena.atlas.logging.LogCtlJUL;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.text.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import arq.cmdline.CmdARQ;

/**
 * CLI tool that bulk-indexes a TDB2 dataset into a Lucene text index
 * using SHACL entity-per-document profiles.
 * <p>
 * Use this after loading data with {@code tdb2.tdbloader}, which bypasses
 * the normal change listener.
 * <p>
 * Usage: {@code shacltextindexer --desc=assembler.ttl}
 */
public class shacltextindexer extends CmdARQ {

    private static Logger log = LoggerFactory.getLogger(shacltextindexer.class);

    public static final ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset");

    protected DatasetGraphText dataset = null;
    protected TextIndexLucene textIndex = null;
    protected ShaclIndexMapping shaclMapping = null;

    static public void main(String... argv) {
        LogCtlJUL.routeJULtoSLF4J();
        new shacltextindexer(argv).mainRun();
    }

    static public void testMain(String... argv) {
        new shacltextindexer(argv).mainMethod();
    }

    protected shacltextindexer(String[] argv) {
        super(argv);
        super.add(assemblerDescDecl, "--desc=", "Assembler description file");
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        String file;

        if (!super.contains(assemblerDescDecl) && getNumPositional() == 0)
            throw new CmdException("No assembler description given");

        if (super.contains(assemblerDescDecl)) {
            if (getValues(assemblerDescDecl).size() != 1)
                throw new CmdException("Multiple assembler descriptions given via --desc");
            if (getPositional().size() != 0)
                throw new CmdException("Additional assembler descriptions given");
            file = getValue(assemblerDescDecl);
        } else {
            if (getNumPositional() != 1)
                throw new CmdException("Multiple assembler descriptions given as positional arguments");
            file = getPositionalArg(0);
        }

        if (file == null)
            throw new CmdException("No dataset specified");

        Dataset ds = TextDatasetFactory.create(file);
        if (ds == null)
            throw new CmdException("No dataset description found");

        dataset = (DatasetGraphText)(ds.asDatasetGraph());
        TextIndex idx = dataset.getTextIndex();
        if (idx == null)
            throw new CmdException("Dataset has no text index");
        if (!(idx instanceof TextIndexLucene))
            throw new CmdException("Text index is not a Lucene index");

        textIndex = (TextIndexLucene) idx;
        if (!textIndex.isShaclMode())
            throw new CmdException("Text index is not configured with SHACL shapes (text:shapes). " +
                "Use 'textindexer' for classic triple-per-document indexes.");

        shaclMapping = textIndex.getShaclMapping();
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " --desc=assemblerFile";
    }

    @Override
    protected void exec() {
        try {
            if (dataset.supportsTransactions()) {
                dataset.begin(ReadWrite.READ);
            }

            log.info("Starting SHACL bulk indexing...");
            log.info("  Profiles: {}", shaclMapping.getProfiles().size());
            for (ShaclIndexMapping.IndexProfile profile : shaclMapping.getProfiles()) {
                log.info("    {} -> {}", profile.getShapeNode(), profile.getTargetClasses());
            }

            long startTime = System.currentTimeMillis();

            ShaclBulkIndexer indexer = new ShaclBulkIndexer(
                dataset, textIndex, shaclMapping);
            indexer.index();

            textIndex.close();

            long elapsed = System.currentTimeMillis() - startTime;
            long seconds = Math.max(elapsed / 1000, 1);
            log.info("Indexing complete: {} entities in {} seconds ({} per second)",
                indexer.getEntityCount(), seconds, indexer.getEntityCount() / seconds);

            if (dataset.supportsTransactions()) {
                dataset.commit();
            }
            dataset.close();
        } finally {
            if (dataset.supportsTransactions()) {
                dataset.end();
            }
        }
    }
}
