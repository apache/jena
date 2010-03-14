/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.Date ;
import java.util.List ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkSplit ;
import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.riot.ParserFactory ;
import com.hp.hpl.jena.riot.RiotException ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.lib.SinkQuadsToDataset ;
import com.hp.hpl.jena.tdb.sys.Names ;

/** Mechanism for loading datasets */
public class BulkLoaderDataset
{
    private final DatasetGraphTDB dataset ;
    private final boolean showProgress ;
    private final boolean generateStats ;
    private long count = 0 ;
    private Timer timer ;

    public BulkLoaderDataset(DatasetGraphTDB dataset, boolean showProgress, boolean generateStats)
    {
        if ( ! dataset.isEmpty() )
        {
            generateStats = false ;
            println("** Load data into existing dataset") ;
        }
        else
        {
            if ( showProgress )
                println("** Load empty dataset") ;
        }

        this.dataset = dataset ;
        this.showProgress = showProgress ;
        this.generateStats = generateStats ;
        
    }
    
    public void load(List<String> urls)
    {
        setupLoad() ;
        
        //dataset.getStats() ;
        
        SinkQuadsToDataset sink = new SinkQuadsToDataset(dataset) ;
        
        for ( String url : urls )
        {
            //statsStart(model) ;
            now("-- Start loading phase") ;
            InputStream input = IO.openFile(url) ;
            Lang lang = Lang.guess(url) ;
            load(sink, input, lang, url, showProgress) ;
            now("-- Finish loading phase") ;
            //statsFinish(model) ;
        }

        finalizeLoad() ;
    }
    
    public void load(InputStream input)
    {
        load(input, Lang.NQUADS, null) ;
    }
    
    public void load(InputStream input, Lang lang, String baseURI)
    {
        setupLoad() ;
        SinkQuadsToDataset sink = new SinkQuadsToDataset(dataset) ;
        load(sink, input, lang, baseURI, showProgress) ;
        finalizeLoad() ;
    }

    // MONITOR.
    
    private static final int tickInterval = 5000 ; 
    
    // Worker to do the actual reading of input 
    private void load(Sink<Quad> destination, InputStream input, Lang lang, String baseURI, boolean showProgress) 
    {
        Sink<Quad> sink = destination ;

        SinkProgress<Quad> progress = new SinkProgress<Quad>(null, "quads", tickInterval, showProgress) ;
        sink = new SinkSplit<Quad>(sink, progress) ;
        
        // Stats sink.
        
        LangRIOT parser = parser(sink, input, lang, baseURI);
        progress.startMonitor() ;
        parser.parse() ;
        progress.finishMonitor() ;
        sink.flush() ;
        sink.close() ;
        count = progress.getCount() ;
        return ;
    }
    
    private static LangRIOT parser(Sink<Quad> sink, InputStream input, Lang lang, String baseURI)
    {
        if ( lang == null )
        {
            Log.warn(BulkLoaderDataset.class, "Lang argument is null - guessing "+Lang.NQUADS.getName()) ;
            lang = Lang.NQUADS ;
        }
        switch(lang)
        {
            case NQUADS :
                return ParserFactory.createParserNQuads(input, sink) ;
            case TRIG :
                return ParserFactory.createParserTriG(input, baseURI, sink) ;
            default:
                throw new RiotException("Language not supported for quads: "+lang) ;
        }
    }
    
    // Placeholders for index manipulation.
    
    private void setupLoad()
    {
        timer = new Timer() ;
        timer.startTimer() ;
    }
    
    private void finalizeLoad()
    {
        // Fake stats
        if ( generateStats )
        {
            if ( ! dataset.getLocation().exists(Names.optStats) )  
            {
                String fn = dataset.getLocation().absolute(Names.optFixed) ;
                try {
                    new File(fn).createNewFile() ;
                } catch (IOException ex)
                {
                    ALog.fatal(this, "Failed to write stats file: "+ex.getLocalizedMessage(), ex) ;
                }
            }
        }
        
        timer.endTimer() ;
        long time = timer.getTimeInterval() ;
        if ( showProgress )
        {
            long qps = 1000*count/time ;
            println() ;
            printf("Time for load: %.2fs [%,d quads/s]\n", time/1000.0, qps) ;
        }
    }

    // ---- Misc utilities
    private synchronized void printf(String fmt, Object... args)
    {
        if (!showProgress) return ;
        System.out.printf(fmt, args) ;
    }

    private synchronized void println()
    {
        if (!showProgress) return ;
        System.out.println() ;
    }

    private synchronized void println(String str)
    {
        if (!showProgress) return ;
        System.out.println(str) ;
    }

    private synchronized void now(String str)
    {
        if (!showProgress) return ;

        if (str != null)
        {
            System.out.print(str) ;
            System.out.print(" : ") ;
        }
        System.out.println(StringUtils.str(new Date())) ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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