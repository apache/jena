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

import atlas.io.IO ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.riot.IRIResolver ;
import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.lib.DatasetLib ;
import com.hp.hpl.jena.tdb.store.BulkLoader ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

/** Public interfacr to the loader functionality */ 
public class TDBLoader
{
    // ---- Load dataset
    public static void load(DatasetGraphTDB dataset, List<String> urls, boolean showProgress)
    {
        // Placeholder for something clever
        for ( String f : urls )
        {
            Lang lang = determineQuadLang(f) ;
            InputStream input = IO.openFile(f) ; 
            DatasetLib.read(input,  dataset, lang, IRIResolver.chooseBaseURI(f).toString()) ; 
        }
    }

    private static Lang determineQuadLang(String f)
    {
        if ( f.endsWith(".trig") )
            return Lang.TRIG ;
        if ( f.endsWith(".nq") )
            return Lang.NQUADS ;
        return null ;
    }

    // ---- Load graph
    public static void load(GraphTDB graph, List<String> urls, boolean showProgress)
    {
        BulkLoader loader = new BulkLoader(graph, showProgress) ;
        loader.load(urls) ;
    }
    
    public static void load(GraphTDB graph, String url, boolean showProgress)
    {
        List<String> list = new ArrayList<String>() ;
        list.add(url) ;
        BulkLoader loader = new BulkLoader(graph, showProgress) ;
        loader.load(list) ;
    }

    // ---- Load any model.
    
    public static long load(Model model, String url, boolean showProgress)
    {
        return BulkLoader.load(model, url, showProgress) ;
    }
    
    public static void loadSimple(Model model, List<String> urls, boolean showProgress)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        long count = 0 ;
        
        for ( String s : urls )
        {
            if ( showProgress ) 
                System.out.printf("Load: %s\n", s) ;
            count += load(model, s, showProgress) ;
        }

        //long time = timer.endTimer() ;
        //System.out.printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, (triples/time)) ;
        model.close();
    }
    
    
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