/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import java.io.PrintStream;
import java.util.Iterator;


import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.modify.lang.ParserSPARUL;
import com.hp.hpl.jena.sparql.modify.op.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsert;
import com.hp.hpl.jena.sparql.modify.op.UpdateLoad;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.FileManager;

public class DEV
{
    protected static Node s = Node.create("http://example/r") ;
    protected static Node p = Node.create("http://example/p") ;
    protected static Node o1 = Node.create("2007") ;
    protected static Triple triple1 =  new Triple(s,p,o1) ;
    protected static Node o2 = Node.create("1066") ;
    protected static Triple triple2 =  new Triple(s,p,o2) ;
    
    public static void main(String[] argv) 
    {
        Graph graph = Factory.createDefaultGraph() ;
        graph.add(triple1) ;
        graph.add(triple2) ;
        UpdateInsert ins = new UpdateInsert(graph) ;
        ins.addGraphName("http://foo") ;
        System.out.println(ins) ;
        
        
        System.exit(0) ;
        
        String filename = null ;
        if ( argv.length > 1 )
        {
            System.err.println("Usage: SPARUL [file|-]") ;
        }
        
        if ( argv.length == 0 )
            argv = new String[]{"MOD"} ;
        filename = argv[0] ;
        
        //reader(filename) ; 
        buildRequest() ; System.exit(0) ;
    }
        
    public static void reader(String filename) 
    {
        UpdateRequest update = UpdateFactory.read(filename) ;
        
        if ( false )
        {
            System.out.println("Finished parsing") ;
            String serialForm = update.toString() ;
            System.out.print(serialForm) ;
            update = UpdateFactory.create(serialForm) ;
        }
        
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.execute(update) ;
        
        if ( false )
            printGraphStore(gStore, update) ;
    }
        
    public static void buildRequest()
    {
        UpdateRequest update = UpdateFactory.create() ;
        update.addUpdate(new UpdateCreate("http://example/progGraph")) ;
        update.addUpdate(new UpdateLoad("file:D.ttl")) ;
        
        
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.execute(update) ;
        printGraphStore(gStore, update) ;
    }
    
    public static void printGraphStore(GraphStore gStore, UpdateRequest update)
    {
        PrintStream out = System.out ;
            /*IndentedWriter out = new IndentedWriter(System.out) ; */
        out.println("**** Default graph") ;
        printGraph(out, gStore.getDefaultGraph()) ;
        
//        for ( String n : gStore.graphNames() )
        for ( Iterator iter = gStore.listNames() ; iter.hasNext() ; )
        {
            String n = (String)iter.next() ;
            out.println("**** Graph: "+FmtUtils.stringForURI(n, update.getPrefixMapping())) ;
            printGraph(out, gStore.getNamedGraph(n)) ;
        }
    }
    
    
    public static void printGraph(PrintStream out, Graph graph)
    {
        Model m = ModelFactory.createModelForGraph(graph) ;
        m.write(out, "TTL") ;
    }
    
    public static void simple(String[] argv)
    {
        String str = null ;

        if ( argv[0].equals("-") )
            str = FileManager.get().readWholeFileAsUTF8(System.in);
        else
            str = FileManager.get().readWholeFileAsUTF8(argv[0]) ;
        ParserSPARUL p = new ParserSPARUL() ;
        UpdateRequest update = new UpdateRequest() ;
        p.parse(update, str) ;
        System.out.println("Finished parsing") ;
        String serialForm = update.toString() ;
        System.out.print(serialForm) ;
        
        // Check
        p = new ParserSPARUL() ;
        update = new UpdateRequest() ;
        p.parse(update, serialForm) ;
        
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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