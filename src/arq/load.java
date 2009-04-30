/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.util.Iterator;
import java.util.List;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdUpdate;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.modify.op.UpdateLoad;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.util.graph.GraphLoadMonitor;

import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateFactory;

public class load extends CmdUpdate
{
    ArgDecl graphNameArg = new ArgDecl(ArgDecl.HasValue, "--graph") ;
    ArgDecl dumpArg = new ArgDecl(ArgDecl.NoValue, "--dump") ;       // Write the result to stdout.
    
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
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --desc=assembler [--dump] --update=<request file>" ; }

    @Override
    protected void execUpdate(GraphStore graphStore)
    {
        if ( loadFiles.size() == 0 )
            throw new CmdException("Nothing to do") ;
        
        UpdateLoad loadReq = new UpdateLoad() ;
        if ( graphName != null )
            loadReq.setGraphName(graphName) ;
        
        for ( Iterator<String> iter = loadFiles.iterator() ; iter.hasNext() ; )
        {
            String filename = iter.next();
            loadReq.addLoadIRI(filename) ;
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
        
        
        UpdateFactory.create(loadReq, graphStore).execute() ;
        
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
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */