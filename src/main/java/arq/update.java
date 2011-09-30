/**
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

import java.util.Iterator ;
import java.util.List ;

import org.openjena.riot.out.NQuadsWriter ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdUpdate ;

import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class update extends CmdUpdate
{
    // --service / --remote
    static final ArgDecl updateArg = new ArgDecl(ArgDecl.HasValue, "update", "file") ;
    static final ArgDecl dumpArg = new ArgDecl(ArgDecl.NoValue, "dump") ;       // Write the result to stdout.
    
    List<String> requestFiles = null ;
    boolean dump = false ;
    
    public static void main (String... argv)
    { new update(argv).mainRun() ; }
    
    protected update(String[] argv)
    {
        super(argv) ;
        super.add(updateArg, "--update=FILE", "Update commands to execute") ;
        super.add(dumpArg, "--dump", "Dump the resulting graph store") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        requestFiles = getValues(updateArg) ;   // ????
        dump = contains(dumpArg) ;
        
        super.processModulesAndArgs() ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --desc=assembler [--dump] --update=<request file>" ; }

    // Subclass for specialised commands making common updates more convenient
    @Override
    protected void execUpdate(GraphStore graphStore)
    {
        if ( requestFiles.size() == 0 && getPositional().size() == 0 )
            throw new CmdException("Nothing to do") ;
        
        for ( Iterator<String> iter = requestFiles.iterator() ; iter.hasNext() ; )
        {
            String filename = iter.next();
            execOneFile(filename, graphStore) ;
        }
        
        for ( Iterator<String> iter = super.getPositional().iterator() ; iter.hasNext() ; )
        {
            String requestString = iter.next();
            requestString = indirect(requestString) ;
            execOne(requestString, graphStore) ;
        }
        SystemARQ.sync(graphStore) ;
        if ( dump )
            //SSE.write(graphStore) ;
            NQuadsWriter.write(System.out, graphStore) ;
    }


    private void execOneFile(String filename, GraphStore store)
    {
        UpdateRequest req = UpdateFactory.read(filename, updateSyntax) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }
    
    private void execOne(String requestString, GraphStore store)
    {
        UpdateRequest req = UpdateFactory.create(requestString, updateSyntax) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }
}
