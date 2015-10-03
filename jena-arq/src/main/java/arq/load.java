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

import java.util.Iterator ;
import java.util.List ;

import arq.cmdline.CmdUpdate ;
import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.modify.request.UpdateLoad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.graph.GraphLoadMonitor ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateRequest ;

public class load extends CmdUpdate
{
    static private final ArgDecl graphNameArg = new ArgDecl(ArgDecl.HasValue, "--graph") ;
    static private final ArgDecl dumpArg = new ArgDecl(ArgDecl.NoValue, "--dump") ;
    
    String graphName = null ;
    List<String> loadFiles = null ;
    boolean dump = false ;
    
    public static void main (String... argv)
    { new load(argv).mainRun() ; }
    
    protected load(String[] argv)
    {
        super(argv) ;
        super.add(graphNameArg, "--graph=IRI", "Graph IRI (loads default graph if absent)") ;
        super.add(dumpArg, "--dump", "Dump the resulting graph store") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        if ( containsMultiple(graphNameArg) )
            throw new CmdException("At most one --graph allowed") ;
        
        graphName = getValue(graphNameArg) ;
        loadFiles = super.getPositional() ;
        dump = contains(dumpArg) ;
        super.processModulesAndArgs() ;
    }
    
    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --desc=assembler [--dump] --update=<request file>" ; }

    @Override
    protected void execUpdate(DatasetGraph graphStore)
    {
        if ( loadFiles.size() == 0 )
            throw new CmdException("Nothing to do") ;
        
        UpdateRequest req = new UpdateRequest() ;
        for ( String filename : loadFiles )
        {
            UpdateLoad loadReq = new UpdateLoad( filename, graphName );
            req.add( loadReq );
        }
        
        if ( true )
        {
            // Need a better way
            monitor(graphStore.getDefaultGraph()) ;
            for ( Iterator<Node> iter = graphStore.listGraphNodes() ; iter.hasNext() ; )
            {
                Graph g = graphStore.getGraph(iter.next()) ;
                monitor(g) ;
            }
        }
        
        UpdateExecutionFactory.create(req, graphStore).execute() ;
        
        if ( dump )
        {
            IndentedWriter out = IndentedWriter.stdout ;
            SSE.write(graphStore) ;
            out.flush();
        }
    }

    private void monitor(Graph graph)
    {
        GraphLoadMonitor m = new GraphLoadMonitor(20000,false) ;
        //m.setSummaryLabel(getCommandName()) ;
        graph.getEventManager().register(m)  ;
    }
    
    @Override
    protected DatasetGraph dealWithNoDataset() {
        throw new CmdException("No dataset provided") ;
    }
}
