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
import com.hp.hpl.jena.sdb.SDB ;
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
  */ 
 
public class sdbload extends CmdArgsDB
{
    private static final String usage = "sdbload --sdb <SPEC> [--graph=IRI] file" ;
    
    private static ModGraph modGraph = new ModGraph() ;
    private static ArgDecl argDeclTruncate = new ArgDecl(false, "truncate") ;
    private static ArgDecl argDeclReplace = new ArgDecl(false, "replace") ;
    
    public static void main(String... argv)
    {
        SDB.init();
        new sdbload(argv).mainRun() ;
    }
    
    String filename = null ;

    public sdbload(String... args)
    {
        super(args);
        addModule(modGraph) ;
        add(argDeclTruncate) ;
        add(argDeclReplace) ;
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
            loadOne(x, contains(argDeclReplace)) ;
        StoreBaseHSQL.close(getStore()) ;
    }
    
    private void loadOne(String filename, boolean replace)
    {
        Monitor monitor = null ;
        
        Model model = modGraph.getModel(getStore()) ;
        Graph graph = model.getGraph() ;    

        if ( isVerbose() && replace )
            System.out.println("Emptying: "+filename) ;
        if (replace)
            model.removeAll();

        if ( isVerbose() || getModTime().timingEnabled() )
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
        
        
        @Override
        public void notifyAddTriple(Graph g, Triple t) { addEvent(t) ; }

        @Override
        public void notifyAddArray(Graph g, Triple[] triples)
        { 
            for ( Triple t : triples )
                addEvent(t) ;
        }

        @Override
        public void notifyAddList(Graph g, List<Triple> triples) 
        { 
            notifyAddIterator(g, triples.iterator()) ;
        }

        @Override
        public void notifyAddIterator(Graph g, Iterator<Triple> it)
        {
            for ( ; it.hasNext() ; )
                addEvent(it.next()) ;
        }

        @Override
        public void notifyAddGraph(Graph g, Graph added)
        {}

        @Override
        public void notifyDeleteTriple(Graph g, Triple t)
        {}

        @Override
        public void notifyDeleteList(Graph g, List<Triple> L)
        {}

        @Override
        public void notifyDeleteArray(Graph g, Triple[] triples)
        {}

        @Override
        public void notifyDeleteIterator(Graph g, Iterator<Triple> it)
        {}

        @Override
        public void notifyDeleteGraph(Graph g, Graph removed)
        {}

        @Override
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
