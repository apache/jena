/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.List ;

import org.openjena.atlas.logging.Log ;

import tdb.cmdline.CmdTDB ;
import tdb.cmdline.ModModel ;
import arq.cmdline.ArgDecl ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.tdb.TDBLoader ;
import com.hp.hpl.jena.tdb.store.BulkLoader ;
import com.hp.hpl.jena.tdb.store.BulkLoaderDataset ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

public class tdbloader extends CmdTDB
{
//    private static final ArgDecl argGraphDeafult = new ArgDecl(ArgDecl.NoValue, "default") ;
    
    private static final ArgDecl argParallel         = new ArgDecl(ArgDecl.NoValue, "parallel") ;
    private static final ArgDecl argIncremental      = new ArgDecl(ArgDecl.NoValue, "incr", "incremental") ;
    private static final ArgDecl argStats            = new ArgDecl(ArgDecl.NoValue, "stats") ;
    
    private static final ModModel modRDFS            = new ModModel("rdfs") ;
    
    private boolean timing = true ;
    private boolean doInParallel = false ;
    private boolean doIncremental = false ;
    private boolean generateStats = false ;
    
    static public void main(String... argv)
    { 
        Log.setLog4j() ;
        new tdbloader(argv).mainRun() ;
    }

    protected tdbloader(String[] argv)
    {
        super(argv) ;
        
        super.add(argParallel, "--parallel", "Do rebuilding of secondary indexes in a parallel") ;
        super.add(argIncremental, "--incremental", "Do an incremental load (keep indexes during load, don't rebuild)") ;
        super.add(argStats, "--stats", "Generate statistics while loading (new graph only)") ;
        //super.addModule(modRDFS) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        doInParallel = super.contains(argParallel) ;
        doIncremental = super.contains(argIncremental) ;
        generateStats = super.contains(argStats) ;
    }
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--desc DATASET | -loc DIR] FILE ..." ;
    }

    @Override
    protected void exec()
    {
        if ( isVerbose())
        {
            System.out.println("Java maximum memory: "+Runtime.getRuntime().maxMemory());
            System.out.println(ARQ.getContext()) ;
        }
        if ( isVerbose() )
            timing = true ;
        if ( isQuiet() )
            timing = false ;
        
        List<String> urls = getPositional() ;
        if ( urls.size() == 0 )
            urls.add("-") ;
        
        boolean allTriples = true ;
        for ( String url : urls )
        {
            Lang lang = Lang.guess(url) ;
            if ( lang != null && lang.isQuads() )
            {
                allTriples = false ;
                break ; 
            }
        }
        
        if ( allTriples && graphName == null )
        {
            Graph graph = getGraph() ;
            BulkLoader loader = new BulkLoader((GraphTDB)graph, timing, doInParallel, doIncremental, generateStats) ;
            loader.load(urls) ;
            return ;
        }
        
        if ( graphName == null )
        {
            BulkLoaderDataset loader = new BulkLoaderDataset(getDatasetGraph(), timing, generateStats) ;
            loader.load(urls) ;
            return ;
//            // Quads
//            for ( String url : urls )
//            {
//                
//                Lang lang = Lang.guess(url) ;
//                DatasetLib.read(url, getDatasetGraph(), lang, url) ;
//            }
//            return ;
        }

        // Explicitly named graph
        Model model = getModel() ;
        for ( String url : urls )
        {
            Lang lang = Lang.guess(url) ;
            if ( lang != null && ! lang.isTriples() )
                cmdError("Can only load triples into a named model: "+url) ;
        }

        TDBLoader.loadModel(model, urls, timing) ;
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