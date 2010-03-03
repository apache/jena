/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.store.BulkLoader ;
import com.hp.hpl.jena.tdb.store.BulkLoaderDataset ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

/** Public interfacr to the loader functionality */ 
public class TDBLoader
{
    
    // ---- Load dataset

    /** Load the contents of URL into a dataset.  URL must name a quads format file (NQuads or TriG).
     *  To a triples format, use @link{#load(GraphTDB, String)} 
    */
    public static void load(DatasetGraphTDB dataset, String url)
    {
        load(dataset, url, false) ;
    }

    /** Load the contents of URL into a dataset.  URL must name a quads format file (NQuads or TriG).
     *  To a triples format, use @link{#load(GraphTDB, String, boolean)} 
    */
    public static void load(DatasetGraphTDB dataset, String url, boolean showProgress)
    {
        load(dataset, asList(url), showProgress) ;
    }

    /** Load the contents of URL into a dataset.  URL must name a quads format file (NQuads or TriG).
     *  To load a triples format, use @link{#load(GraphTDB, List<String>, boolean)} 
    */
    public static void load(DatasetGraphTDB dataset, List<String> urls, boolean showProgress)
    {
        TDBLoader loader = new TDBLoader() ;
        loader.setShowProgress(showProgress) ;
        loader.loadDataset(dataset, urls) ;
    }

    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, String url)
    {
        load(graph, url, false) ;
    }
    
    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, String url, boolean showProgress)
    {
        load(graph, asList(url), showProgress) ;
    }


    /** Load the contents of URL into a graph */
    public static void load(GraphTDB graph, List<String> urls, boolean showProgress)
    {
        TDBLoader loader = new TDBLoader() ;
        loader.setShowProgress(showProgress) ;
        loader.loadGraph(graph, urls) ;
    }
    
    /** Load the contents of URL into a model - may not be as efficient as bulk loading into a TDB graph  */
    public static long loadModel(Model model, String url, boolean showProgress)
    {
        return BulkLoader.load(model, url, showProgress) ;
    }

    /** Load the contents of a listy of URLs into a model - may not be as efficient as bulk loading into a TDB graph  */
    public static void loadModel(Model model, List<String> urls, boolean showProgress)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        long count = 0 ;

        for ( String s : urls )
        {
            if ( showProgress ) 
                System.out.printf("Load: %s\n", s) ;
            count += loadModel(model, s, showProgress) ;
        }

        //long time = timer.endTimer() ;
        //System.out.printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, (triples/time)) ;
        model.close();
    }
    
    // ---- The class itself.
    
    private boolean showProgress = true ;
    private boolean generateStats = false ;
    
    public TDBLoader() {}
    
    private static List<String> asList(String string)
    {
        List<String> list = new ArrayList<String>() ;
        list.add(string) ;
        return list ;
    }

    /** Load a graph from a URL - assumes URL names a triples format */
    public void loadGraph(GraphTDB graph, String url)
    {
        loadGraph(graph, asList(url)) ;
    }
    
    /** Load a graph from a list of URL - assumes the URLs name triples format documents */
    public void loadGraph(GraphTDB graph, List<String> urls)
    {
        if ( false )
        {
            for ( String url : urls )
            {
                Lang lang = Lang.guess(url) ;
                if ( lang != null && ! lang.isQuads() )
                    throw new TDBException("Not a triples language") ;
            }
        }
        BulkLoader bulkLoader = new BulkLoader(graph, showProgress, false, false, generateStats) ;
        bulkLoader.load(urls) ;
    }
    
    /** Load a graph from an input stream which must be N-Triples */
    public void loadGraph(GraphTDB graph, InputStream input)
    {
        BulkLoader bulkLoader = new BulkLoader(graph, showProgress, false, false, generateStats) ;
        bulkLoader.load(input) ;
    }


    /** Load a dataset from a URL - assumes URL names a quads format */
    public void loadDataset(DatasetGraphTDB dataset, String url)
    {
        loadDataset(dataset, asList(url)) ;
    }
    
    /** Load a dataset from a list of URL - assumes the URLs name quads format documents */
    public void loadDataset(DatasetGraphTDB dataset, List<String> urls)
    {
        for ( String url : urls )
        {
            if ( url.equals("-") )
                continue ;
            Lang lang = Lang.guess(url) ;
            if ( lang == null || lang.isTriples() )
                throw new TDBException("Not a quads language") ;
        }

        BulkLoaderDataset loader = new BulkLoaderDataset(dataset, showProgress, generateStats) ;
        loader.load(urls) ;
        
//        // Placeholder for something clever
//        for ( String url : urls )
//        {
//            if ( url.equals("-") )
//            {
//                loadDataset(dataset, System.in) ;
//                continue ;
//            }
//            Lang lang = Lang.guess(url) ;
//            InputStream input = IO.openFile(url) ; 
//            DatasetLib.read(input,  dataset, lang, IRIResolver.chooseBaseURI(url).toString()) ; 
//        }
    }
    
    /** Load a dataset from an input steram which must be in N-Quads form */
    public void loadDataset(DatasetGraphTDB dataset, InputStream input)
    {
        BulkLoaderDataset loader = new BulkLoaderDataset(dataset, showProgress, generateStats) ;
        loader.load(input, Lang.NQUADS, null) ; 
    }

    public boolean getShowProgress()  
    { return showProgress ; }

    public final void setShowProgress(boolean showProgress)
    { this.showProgress = showProgress ; }

    public final boolean getGenerateStats()
    { return generateStats ; }

    public final void setGenerateStats(boolean generateStats)
    { this.generateStats = generateStats ; }
    
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd
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