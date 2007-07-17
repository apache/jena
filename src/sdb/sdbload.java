/*
 * (c) Copyright 2006, 2007 Hewlett--Packard Development Company, LP
 * [See end of file]
 */

package sdb;


import java.util.Iterator;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import sdb.cmd.ModGraph;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.store.StoreBaseHSQL;
import com.hp.hpl.jena.sparql.util.Timer;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileUtils;
 
 /** Load data files into an SDB model in a database.
  * 
  *  <p>
  *  Usage:<pre>
  *    sdbload [db spec] file [file ...]
  *  </pre>
  *  The syntax of a file is determimed by its extension (.n3, .nt) and defaults to RDF/XML. 
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: sdbload.java,v 1.27 2006/04/22 19:51:11 andy_seaborne Exp $
  */ 
 
public class sdbload extends CmdArgsDB
{
    private static final String usage = "sdbload --sdb <SPEC> [--graph=IRI] file" ;
    
    private static ModGraph modGraph = new ModGraph() ;
    private static ArgDecl argDeclTruncate = new ArgDecl(false, "truncate") ;
    
    public static void main(String... argv)
    {
        new sdbload(argv).main() ;
    }
    
    String filename = null ;

    public sdbload(String... args)
    {
        super(args);
        addModule(modGraph) ;
        add(argDeclTruncate) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> [--graph IRI] file ..."; }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( getNumPositional() == 0 )
            cmdError("Need filenames of RDF data to load", true) ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        if ( contains(argDeclTruncate) ) 
            getStore().getTableFormatter().truncate() ;
        for ( String x : args )
            loadOne(x) ;
        StoreBaseHSQL.close(getStore()) ;
    }
    
    private void loadOne(String filename)
    {
        Monitor monitor = null ;
        
        Model model = modGraph.getModel(getStore()) ;
        Graph graph = model.getGraph() ;    
        
        if ( isVerbose() )
            System.out.println("Start load: "+filename) ;
        if ( getModTime().timingEnabled() )
        {
            monitor = new Monitor(getStore().getLoader().getChunkSize(), isVerbose()) ;
            graph.getEventManager().register(monitor) ;
        }

        // Crude but convenient
        if ( filename.indexOf(':') == -1 )
            filename = "file:"+filename ;

        String lang = FileUtils.guessLang(filename) ;
        
        // Always time, only print if enabled. 
        getModTime().startTimer() ;
        
        // Load here
        model.read(filename, lang) ;

        long timeMilli = getModTime().endTimer() ;
            
        if ( monitor != null )
        {
            System.out.println("Added "+monitor.addCount+" triples") ; 
        
            if ( getModTime().timingEnabled() && !isQuiet() )
                System.out.printf("Loaded in %.3f seconds [%d triples/s]\n", 
                                  timeMilli/1000.0, (1000*monitor.addCount/timeMilli)) ;
            graph.getEventManager().unregister(monitor) ;
        }
    }
        
    static class Monitor implements GraphListener
    {
        int addNotePoint ;
        long addCount = 0 ;
        int outputCount = 0 ;
        
        private Timer timer = null ;
		private long lastTime = 0 ;
        private boolean displayMemory = false ; 
            
        Monitor(int addNotePoint, boolean displayMemory)
        {
            this.addNotePoint = addNotePoint ;
            this.displayMemory = displayMemory ;
            this.timer = new Timer() ;
            this.timer.startTimer() ;
        }
        
        
        public void notifyAddTriple(Graph g, Triple t) { addEvent(t) ; }

        public void notifyAddArray(Graph g, Triple[] triples)
        { 
            for ( Triple t : triples )
                addEvent(t) ;
        }

        @SuppressWarnings("unchecked")
        public void notifyAddList(Graph g, List triples) 
        { 
            notifyAddIterator(g, triples.iterator()) ;
        }

        @SuppressWarnings("unchecked")
        public void notifyAddIterator(Graph g, Iterator it)
        {
            for ( ; it.hasNext() ; )
                addEvent((Triple)it.next()) ;
        }

        public void notifyAddGraph(Graph g, Graph added)
        {}

        public void notifyDeleteTriple(Graph g, Triple t)
        {}

        @SuppressWarnings("unchecked")
        public void notifyDeleteList(Graph g, List L)
        {}

        public void notifyDeleteArray(Graph g, Triple[] triples)
        {}

        @SuppressWarnings("unchecked")
        public void notifyDeleteIterator(Graph g, Iterator it)
        {}

        public void notifyDeleteGraph(Graph g, Graph removed)
        {}

        public void notifyEvent(Graph source, Object value)
        {}

        private void addEvent(Triple t)
        {
            addCount++ ;
            if ( addNotePoint > 0 && (addCount%addNotePoint) == 0 )
            {
                outputCount++ ;
                long soFar = timer.readTimer() ;
                long thisTime = soFar - lastTime ;
                
                // *1000L is milli to second conversion
                //   addNotePoint/ (thisTime/1000L)
                long tpsBatch = (addNotePoint * 1000L) / thisTime;
                long tpsAvg = (addCount * 1000L) / soFar;
                
                String msg = String.format("Add: %,d triples  (Batch: %d / Run: %d)", addCount, tpsBatch, tpsAvg) ;
                if ( displayMemory )
                {
                  long mem = Runtime.getRuntime().totalMemory() ;
                  long free = Runtime.getRuntime().freeMemory() ;
                  msg = msg+String.format("   [M:%,d/F:%,d]", mem,free) ;
                }
                System.out.println(msg) ;
                if ( outputCount > 0 && (outputCount%10) == 0 )
                    System.out.printf("  Elapsed: %.1f seconds\n", (soFar/1000F)) ;
                lastTime = soFar ;
            }
        }
        
    }
}
 
/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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
