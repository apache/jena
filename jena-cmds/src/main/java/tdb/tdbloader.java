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

package tdb ;

import java.util.List ;

import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBLoader ;
import org.apache.jena.tdb.store.GraphTDB;
import tdb.cmdline.CmdTDB ;
import tdb.cmdline.CmdTDBGraph ;

public class tdbloader extends CmdTDBGraph {
    // private static final ArgDecl argParallel = new ArgDecl(ArgDecl.NoValue, "parallel") ;
    // private static final ArgDecl argIncremental = new ArgDecl(ArgDecl.NoValue, "incr", "incremental") ;
    private static final ArgDecl argNoStats = new ArgDecl(ArgDecl.NoValue, "nostats") ;
    private static final ArgDecl argStats = new ArgDecl(ArgDecl.HasValue,  "stats") ;

    private boolean showProgress  = true ;
    private boolean generateStats  = true ;
    // private boolean doInParallel = false ;
    // private boolean doIncremental = false ;

    static public void main(String... argv) {
        CmdTDB.init() ;
        TDB.setOptimizerWarningFlag(false) ;
        new tdbloader(argv).mainRun() ;
    }

    protected tdbloader(String[] argv) {
        super(argv) ;
//        super.getUsage().startCategory("Stats") ;
        super.add(argNoStats, "--nostats", "Switch off statistics gathering") ;
        super.add(argStats) ;   // Hidden argument
        // super.add(argParallel, "--parallel",
        // "Do rebuilding of secondary indexes in a parallel") ;
        // super.add(argIncremental, "--incremental",
        // "Do an incremental load (keep indexes during data load)") ;
        // super.add(argStats, "--stats",
        // "Generate statistics while loading (new graph only)") ;
        // addModule(modRDFS) ;
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs() ;
        // doInParallel = super.contains(argParallel) ;
        // doIncremental = super.contains(argIncremental) ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " [--desc DATASET | -loc DIR] FILE ..." ;
    }

    @Override
    protected void exec() {
        if ( isVerbose() ) {
            System.out.println("Java maximum memory: " + Runtime.getRuntime().maxMemory()) ;
            System.out.println(ARQ.getContext()) ;
        }
        if ( isVerbose() )
            showProgress = true ;
        if ( isQuiet() )
            showProgress = false ;
        if ( super.contains(argStats) ) {
            if ( ! hasValueOfTrue(argStats) && ! hasValueOfFalse(argStats) )
                throw new CmdException("Not a boolean value: "+getValue(argStats)) ;
            generateStats = super.hasValueOfTrue(argStats) ;
        }

        if ( super.contains(argNoStats))
            generateStats = false ;
        
        List<String> urls = getPositional() ;
        if ( urls.size() == 0 )
            urls.add("-") ;

        if ( graphName == null ) {
            loadQuads(urls) ;
            return ;
        }
        
        // There's a --graph.
        // Check/warn that there are no quads formats mentioned
        // (RIOT will take the default graph from quads).  
        
        for ( String url : urls ) {
            Lang lang = RDFLanguages.filenameToLang(url) ;
            if ( lang != null && RDFLanguages.isQuads(lang) ) {
                System.err.println("Warning: Quads format given - only the default graph is loaded into the graph for --graph") ;
                break ;
            }
        }
        
        loadNamedGraph(urls) ;
    }

//    void loadDefaultGraph(List<String> urls) {
//        GraphTDB graph = getGraph() ;
//        TDBLoader.load(graph, urls, showProgress) ;
//        return ;
//    }

    void loadNamedGraph(List<String> urls) {
        GraphTDB graph = getGraph() ;
        TDBLoader.load(graph, urls, showProgress) ;
        return ;
    }

    void loadQuads(List<String> urls) {
        TDBLoader.load(getDatasetGraphTDB(), urls, showProgress, generateStats) ;
        return ;
    }
}
