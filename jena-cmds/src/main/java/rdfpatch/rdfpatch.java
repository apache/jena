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

package rdfpatch;

import java.io.InputStream ;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.PatchSummary;
import org.apache.jena.rdfpatch.changes.RDFChangesCounter;
import org.apache.jena.rdfpatch.changes.RDFChangesN;
import org.apache.jena.rdfpatch.changes.RDFChangesWriteUpdate;
import org.apache.jena.rdfpatch.text.RDFChangesWriterText;
import org.apache.jena.sys.JenaSystem;

/** Parse patches as validation */
public class rdfpatch extends CmdRDFPatch
{
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    public static void main(String... args) {
        new rdfpatch(args).mainRun();
    }

    public rdfpatch(String[] argv) {
        super(argv) ;
    }

    @Override
    protected String getCommandName() {
        return "rdfpatch";
    }

    @Override
    protected void execStart() {}

    @Override
    protected void execFinish() {}

    @Override
    protected void execOne(String source, InputStream input) {
        RDFPatch patch = RDFPatchOps.read(input);
        AWriter out = IO.wrapUTF8(System.out);
        // Later ...
        boolean outputAsUpdate = false;
        boolean printStats = ! outputAsUpdate;

        RDFChanges target = outputAsUpdate
                ? new RDFChangesWriteUpdate(out)
                : RDFChangesWriterText.create(System.out);

        RDFChangesCounter counter = new RDFChangesCounter();
        RDFChanges dest = RDFChangesN.multi(target, counter);
        dest.start();
        patch.apply(dest);
        dest.finish();
        out.flush();

        if ( printStats ) {
            PatchSummary summary = counter.summary();
            System.err.printf("# Data:     Adds=%,d Deletes=%,d\n", summary.getCountAddData(),summary.getCountDeleteData());
            System.err.printf("# Prefixes: Adds=%,d Deletes=%,d\n", summary.getCountAddPrefix(),summary.getCountDeletePrefix());
            if ( summary.getCountTxnBegin() > 0 || summary.getCountTxnCommit() > 0 || summary.getCountTxnAbort() > 0 )
                System.err.printf("# Txn:      TX=%,d, TC=%,d, TA=%,d\n", summary.getCountTxnBegin(), summary.getCountTxnCommit(), summary.getCountTxnAbort());
        }
    }
}
