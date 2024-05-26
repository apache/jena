/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rdfpatch.filelog;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAssemblerFileLog {
    static {
        LogCtl.setJavaLogging();
        JenaSystem.init();
    }

    private static String ADIR = "testing/filelog";

//    @Rule
//    public TemporaryFolder tempFolder = new TemporaryFolder();

    // We want to leave the evidence around on test failures.
    private static Path DIR = Paths.get("target/filelog");

    @BeforeClass
    public static void beforeClass() {
        FileOps.ensureDir(DIR.toString());
        FileOps.clearAll(DIR.toString());
    }

    @Test public void assem() {
        Dataset ds = (Dataset)AssemblerUtils.build(ADIR+"/assem-logged.ttl", VocabPatch.tLoggedDataset);
    }

    @Test public void assemData() {
        Dataset ds = (Dataset)AssemblerUtils.build(ADIR+"/assem-logged.ttl", VocabPatch.tLoggedDataset);
        Txn.executeWrite(ds, ()->RDFDataMgr.read(ds, ADIR+"/data.ttl"));

        String patchfile = "target/filelog/log.rdfp.0001";
        assertTrue("Patch file does not exist: "+patchfile, FileOps.exists(patchfile));
        RDFPatch patch = RDFPatchOps.read(patchfile);
        DatasetGraph dsg1 = DatasetGraphFactory.createTxnMem();
        RDFPatchOps.applyChange(dsg1, patch);
        // Same term, no bnode isomorphism.
        boolean b = IsoMatcher.isomorphic(ds.asDatasetGraph(), dsg1);
        assertTrue(b);
    }

}
