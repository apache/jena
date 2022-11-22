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

import static org.apache.jena.sparql.util.graph.GraphUtils.exactlyOneProperty;

import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.RDFPatchConst;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.RDFChangesN;
import org.apache.jena.rdfpatch.filelog.rotate.ManagedOutput;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembler for a dataset that wraps another and provides change logging to a file.
 * <pre>
 *     &lt;#dataset&gt; rdf:type patch:LoggedDataset;
 *         patch:logFile "Dir/BaseFilename";
 *         patch:logPolicy "
 *
 * </pre>
 * Policies: "date", "timestamp", "index", "rotate", "shift", "fixed" and "none".
 * <ul>
 * <li>"date" - add the date to the base name, rotate at midnight.
 * <li>"timestamp" - add a timestamp, rotate on every file.
 * <li>"index" - add a counter, rotate on every file. Highest numbered file is latest written.
 * <li>"rotate" or "shift"- move indexed files up one; write the baseFilename. Highest numbered file is the oldest written.
 * <li>"fixed" or "none" - only the base file name is used.
 * </ul>
 */
public class AssemblerFileLog extends AssemblerBase {
    private static Logger LOG = LoggerFactory.getLogger(AssemblerFileLog.class);

    public AssemblerFileLog() {}

    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        if ( !exactlyOneProperty(root, VocabPatch.pDataset) )
            throw new AssemblerException(root, "No dataset to be logged");
        if ( !root.hasProperty(VocabPatch.pLogFile) )
            throw new AssemblerException(root, "No log file");

        Resource dataset = GraphUtils.getResourceValue(root, VocabPatch.pDataset);
        List<String> destLogs =  GraphUtils.multiValueAsString(root, VocabPatch.pLogFile);

        String logPolicy = GraphUtils.getStringValue(root, VocabPatch.pLogPolicy);
        FilePolicy policy = logPolicy == null ? FilePolicy.FIXED : FilePolicy.policy(logPolicy);

        DatasetGraph dsgBase;;
        try {
            Dataset dsBase = (Dataset)a.open(dataset);
            dsgBase = dsBase.asDatasetGraph();
        } catch (Exception ex) {
            FmtLog.error(this.getClass(), "Failed to build the dataset to adding change logging to: %s",dataset);
            throw ex;
        }

        RDFChanges changes = null;

        // It would be better if each file had a policy.
        //   patch:logFile [ patch:filename ; patch:policy ];
        //   patch:logFile ("FILE" "FIXED");
        for ( String x : destLogs ) {
            FmtLog.info(LOG, "Log file: '%s'", x);
            if ( x.startsWith("file:") )
                x = IRILib.IRIToFilename(x);

            ManagedOutput output = OutputMgr.create(x, policy);
            // --------------
            String ext = FileUtils.getFilenameExt(x);
//            if ( ext.equals("gz") ) {
//                String fn2 = x.substring(0, ".gz".length());
//                ext = FileUtils.getFilenameExt(fn2);
//            }
//            OutputStream out = IO.openOutputFile(x);

            boolean binaryPatches = ext.equalsIgnoreCase(RDFPatchConst.EXT_B);
            RDFChanges sc = binaryPatches
                ? null //RDFPatchOps.binaryWriter(out);
                //: RDFPatchOps.textWriter(out);
                : new RDFChangesManagedOutput(output);

            if ( sc == null )
                throw new AssemblerException(root, "Failed to build the output destination: "+x);
            changes = RDFChangesN.multi(changes, sc);
        }
        DatasetGraph dsg = RDFPatchOps.changes(dsgBase, changes);
        Dataset ds = DatasetFactory.wrap(dsg);
        return ds;
    }
}
