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

package arq;

import java.util.List;

import arq.cmdline.CmdUpdate;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class update extends CmdUpdate
{
    static final ArgDecl updateArg = new ArgDecl(ArgDecl.HasValue, "update", "file");
    static final ArgDecl dumpArg = new ArgDecl(ArgDecl.NoValue, "dump");       // Write the result to stdout.

    List<String> requestFiles = null;
    boolean dump = false;

    public static void main (String... argv)
    { new update(argv).mainRun(); }

    protected update(String[] argv) {
        super(argv);
        super.add(updateArg, "--update=FILE", "Update commands to execute");
        super.add(dumpArg, "--dump", "Dump the resulting graph store");
    }

    @Override
    protected void processModulesAndArgs() {
        requestFiles = getValues(updateArg); // ????
        dump = contains(dumpArg);
        super.processModulesAndArgs();
    }

    @Override
    protected String getCommandName() { return Lib.className(this); }

    @Override
    protected String getSummary() { return getCommandName()+" --desc=assembler [--dump] --update=<request file>"; }

    // Subclass for specialised commands making common updates more convenient
    @Override
    protected void execUpdate(DatasetGraph graphStore) {
        if ( requestFiles.size() == 0 && getPositional().size() == 0 )
            throw new CmdException("Nothing to do");

        Transactional transactional = graphStore;

        for ( String filename : requestFiles )
            Txn.executeWrite(transactional, ()->execOneFile(filename, graphStore));

        for ( String requestString : super.getPositional() ) {
            String requestString2 = indirect(requestString);
            Txn.executeWrite(transactional, ()->execOne(requestString2, graphStore));
        }

        if ( ! ( transactional instanceof DatasetGraph ) )
            // Unlikely/impossible in Jena 3.7.0 onwards.
            SystemARQ.sync(graphStore);

        if ( dump )
            Txn.executeRead(transactional, ()->RDFDataMgr.write(System.out, graphStore, Lang.TRIG) );
    }

    protected void execOneFile(String filename, DatasetGraph store) {
        UpdateRequest req = UpdateFactory.read(filename, updateSyntax);
        UpdateExecutionFactory.create(req, store).execute();
    }

    protected void execOne(String requestString, DatasetGraph store) {
        UpdateRequest req = UpdateFactory.create(requestString, updateSyntax);
        UpdateExecutionFactory.create(req, store).execute();
    }

    @Override
    protected DatasetGraph dealWithNoDataset() {
        return DatasetGraphFactory.create();
    }
}
