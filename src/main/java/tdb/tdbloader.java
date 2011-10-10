/**
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

package tdb;

import java.util.List ;

import org.openjena.riot.Lang ;
import tdb.cmdline.CmdTDBGraph ;
import tdb.cmdline.ModModel ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBLoader ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

public class tdbloader extends CmdTDBGraph
{
//    private static final ArgDecl argGraphDeafult = new ArgDecl(ArgDecl.NoValue, "default") ;
    
//    private static final ArgDecl argParallel         = new ArgDecl(ArgDecl.NoValue, "parallel") ;
//    private static final ArgDecl argIncremental      = new ArgDecl(ArgDecl.NoValue, "incr", "incremental") ;
    
    private static final ModModel modRDFS            = new ModModel("rdfs") ;
    
//    private  String rdfsVocabFilename   = null ;
//    private  Model  rdfsVocab           = null ;
    
    private boolean showProgress = true ;
//    private boolean doInParallel = false ;
    private boolean doIncremental = false ;
    
    static public void main(String... argv)
    { 
        TDB.setOptimizerWarningFlag(false) ;
        new tdbloader(argv).mainRun() ;
    }

    protected tdbloader(String[] argv)
    {
        super(argv) ;
        
//        super.add(argParallel, "--parallel", "Do rebuilding of secondary indexes in a parallel") ;
//        super.add(argIncremental, "--incremental",  "Do an incremental load (keep indexes during data load)") ;
//        super.add(argStats, "--stats",              "Generate statistics while loading (new graph only)") ;
//        addModule(modRDFS) ;
    }        


    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
//        doInParallel = super.contains(argParallel) ;
//        doIncremental = super.contains(argIncremental) ;
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
            showProgress = true ;
        if ( isQuiet() )
            showProgress = false ;
        
        List<String> urls = getPositional() ;
        if ( urls.size() == 0 )
            urls.add("-") ;
        
        if ( modRDFS.getModel() != null )
        {
            // TODO
        }
        
        boolean allTriples = true ;
        for ( String url : urls )
        {
            Lang lang = Lang.guess(url, Lang.NQUADS) ;
            if ( lang != null && lang.isQuads() )
            {
                allTriples = false ;
                break ; 
            }
        }
        
        if ( allTriples && graphName == null )
        {
            loadDefaultGraph(urls) ;
            return ;
        }
        
        if ( graphName == null )
        {
            loadQuads(urls) ;
            return ; 
        }

        // graphName != null
        if ( ! allTriples )
        {
            for ( String url : urls )
            {
                Lang lang = Lang.guess(url, Lang.NQUADS) ;
                if ( lang == null )
                    // Does not happen due to default above.
                    cmdError("File suffix not recognized: " +url) ;
                if ( lang != null && ! lang.isTriples() )
                    cmdError("Can only load triples into a named model: "+url) ;
            }
            cmdError("Internal error: deteched quad input but can't find it again") ;
            return ;
        }
        
        loadNamedGraph(urls) ;
    }
    
    // RDFS
    
    void loadDefaultGraph(List<String> urls)
    {
        GraphTDB graph = getGraph() ;
        TDBLoader.load(graph, urls, showProgress) ;
        return ;
    }
    
    void loadNamedGraph(List<String> urls)
    {
        GraphTDB graph = getGraph() ;
        TDBLoader.load(graph, urls, showProgress) ;
        return ;
    }

    void loadQuads(List<String> urls)
    {
        TDBLoader.load(getDatasetGraph(), urls, showProgress) ;
        return ;
    }
}
