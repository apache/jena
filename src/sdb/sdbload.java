/*
 * (c) Copyright 2006 Hewlett--Packard Development Company, LP
 * [See end of file]
 */

package sdb;


import java.util.Iterator;
import java.util.List;

import sdb.cmd.CmdArgsDB;

import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.util.Utils;
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
    private static final String usage = "sdbload <SPEC> file" ;
    
    private static ArgDecl argDeclTruncate = new ArgDecl(false, "truncate") ;
    
    public static void main (String [] argv)
    {
        new sdbload(argv).mainAndExit() ;
    }
    
    String filename = null ;

    public sdbload(String[] args)
    {
        super(args);
        add(argDeclTruncate) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> file ..."; }
    
    @Override
    protected void checkCommandLine()
    {
        if ( getNumPositional() == 0 )
            cmdError("Need filenames of RDF data to load", true) ;
    }
    
    @Override
    protected void exec0() { return ; }

    boolean first = true ; 
    
    @Override
    protected boolean exec1(String arg)
    {
        if ( first )
        {
            if ( contains(argDeclTruncate) ) 
                getModStore().getStore().getTableFormatter().truncate() ;
            first = false ;
        }
        
        Monitor monitor = null ;
        
        if ( verbose )
        {
        	//System.out.println(TableUtils.dumpDB(getConnection().getSqlConnection()));
            System.out.println("Start load: "+arg) ;
            monitor = new Monitor(getModStore().getStore().getLoader().getChunkSize()) ;
            getModStore().getGraph().getEventManager().register(monitor) ;
        }

        // Crude but convenient
        if ( arg.indexOf(':') == -1 )
            arg = "file:"+arg ;

        String lang = FileUtils.guessLang(arg) ;
        getModTime().startTimer() ;
        
        // Load here
        if ( true )
            getModStore().getModel().read(arg, lang) ;
        else
            // Use bulkloader directly
            ;
        
        long timeMilli = getModTime().endTimer() ;
            
        if ( monitor != null )
        {
            System.out.println("Added "+monitor.addCount+" triples") ; 
        
            if ( getModTime().timingEnabled() && !quiet )
                System.out.printf("Loaded in %.3f seconds [%d triples/s]\n", 
                                  timeMilli/1000.0, (1000*monitor.addCount/timeMilli)) ;
        }
        
        return true ;
    }
        
    static class Monitor implements GraphListener
    {
        int addNotePoint ;
        int addCount = 0 ;
		private long startTime; 
            
        Monitor(int addNotePoint)
        {
            this.addNotePoint = addNotePoint ;
            this.startTime = System.currentTimeMillis();
        }
        
        
        public void notifyAddTriple(Graph g, Triple t) { addEvent(t) ; }

        public void notifyAddArray(Graph g, Triple[] triples)
        { 
            for ( Triple t : triples )
                addEvent(t) ;
        }

        public void notifyAddList(Graph g, List triples) 
        { 
            notifyAddIterator(g, triples.iterator()) ;
        }

        public void notifyAddIterator(Graph g, Iterator it)
        {
            for ( ; it.hasNext() ; )
                addEvent((Triple)it.next()) ;
        }

        public void notifyAddGraph(Graph g, Graph added)
        {}

        public void notifyDeleteTriple(Graph g, Triple t)
        {}

        public void notifyDeleteList(Graph g, List L)
        {}

        public void notifyDeleteArray(Graph g, Triple[] triples)
        {}

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
                long mem = Runtime.getRuntime().totalMemory() ;
                long free = Runtime.getRuntime().freeMemory() ;
                long tps = ((long) addCount * 1000l) / (System.currentTimeMillis() - startTime);
                System.out.println("Add: "+addCount+" triples"+" ["+mem+"/"+free+"] (" + tps + ")") ;
            }
        }
        
    }
}
 


/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
