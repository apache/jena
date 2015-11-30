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

import java.util.List ;

import jena.cmd.ArgDecl;
import jena.cmd.CmdException;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Transactional ;
import org.apache.jena.sparql.core.TransactionalNull ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

import arq.cmdline.CmdUpdate ;

public class update extends CmdUpdate
{
    static final ArgDecl updateArg = new ArgDecl(ArgDecl.HasValue, "update", "file") ;
    static final ArgDecl dumpArg = new ArgDecl(ArgDecl.NoValue, "dump") ;       // Write the result to stdout.
    
    List<String> requestFiles = null ;
    boolean dump = false ;
    
    public static void main (String... argv)
    { new update(argv).mainRun() ; }
    
    protected update(String[] argv) {
        super(argv) ;
        super.add(updateArg, "--update=FILE", "Update commands to execute") ;
        super.add(dumpArg, "--dump", "Dump the resulting graph store") ;
    }

    @Override
    protected void processModulesAndArgs() {
        requestFiles = getValues(updateArg) ; // ????
        dump = contains(dumpArg) ;
        super.processModulesAndArgs() ;
    }

    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --desc=assembler [--dump] --update=<request file>" ; }

    // Subclass for specialised commands making common updates more convenient
    @Override
    protected void execUpdate(DatasetGraph graphStore) {
        if ( requestFiles.size() == 0 && getPositional().size() == 0 )
            throw new CmdException("Nothing to do") ;

        Transactional transactional = (graphStore instanceof Transactional) ? (Transactional)graphStore : new TransactionalNull() ;

        for ( String filename : requestFiles ) {
            try {
                transactional.begin(ReadWrite.WRITE) ;
                execOneFile(filename, graphStore) ;
                transactional.commit() ;
            }
            catch (Throwable ex) { 
                try { transactional.abort() ; } catch (Exception ex2) {}
                throw ex ;
            }
            finally { transactional.end() ; }
        }

        for ( String requestString : super.getPositional() ) {
            requestString = indirect(requestString) ;

            try {
                transactional.begin(ReadWrite.WRITE) ;
                execOne(requestString, graphStore) ;
                transactional.commit() ;
            }
            catch (Throwable ex) { 
                try { transactional.abort() ; } catch (Exception ex2) {}
                throw ex ;
            }
            finally { transactional.end() ; }
        }
        SystemARQ.sync(graphStore) ;

        if ( dump )
            RDFDataMgr.write(System.out, graphStore, Lang.NQUADS) ;
    }

    private void execOneFile(String filename, DatasetGraph store) {
        UpdateRequest req = UpdateFactory.read(filename, updateSyntax) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }

    private void execOne(String requestString, DatasetGraph store) {
        UpdateRequest req = UpdateFactory.create(requestString, updateSyntax) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }

    @Override
    protected DatasetGraph dealWithNoDataset() {
        return DatasetGraphFactory.create() ;
    }
}
