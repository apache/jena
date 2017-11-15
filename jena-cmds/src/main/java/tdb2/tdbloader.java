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

package tdb2;

import java.util.List;

import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.ProgressStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tdb2.cmdline.CmdTDB;
import tdb2.cmdline.CmdTDBGraph;

public class tdbloader extends CmdTDBGraph {
    // private static final ArgDecl argParallel = new ArgDecl(ArgDecl.NoValue, "parallel");
    // private static final ArgDecl argIncremental = new ArgDecl(ArgDecl.NoValue, "incr", "incremental");
    private static final ArgDecl argNoStats = new ArgDecl(ArgDecl.NoValue, "nostats");
    private static final ArgDecl argStats = new ArgDecl(ArgDecl.HasValue,  "stats");

    private boolean showProgress  = true;
    private boolean generateStats  = true;

    static public void main(String... argv) {
        CmdTDB.init();
        new tdbloader(argv).mainRun();
    }

    protected tdbloader(String[] argv) {
        super(argv);
        super.add(argNoStats, "--nostats", "Switch off statistics gathering");
        super.add(argStats);   // Hidden argument
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " [--desc DATASET | -loc DIR] FILE ...";
    }

    @Override
    protected void exec() {
        if ( isVerbose() ) {
            System.out.println("Java maximum memory: " + Runtime.getRuntime().maxMemory());
            System.out.println(ARQ.getContext());
        }
        if ( isVerbose() )
            showProgress = true;
        if ( isQuiet() )
            showProgress = false;
        if ( super.contains(argStats) ) {
            if ( ! hasValueOfTrue(argStats) && ! hasValueOfFalse(argStats) )
                throw new CmdException("Not a boolean value: "+getValue(argStats));
            generateStats = super.hasValueOfTrue(argStats);
        }

        if ( super.contains(argNoStats))
            generateStats = false;
        
        List<String> urls = getPositional();
        if ( urls.size() == 0 )
            urls.add("-");

        if ( graphName == null ) {
            loadQuads(urls);
            return;
        }
        
        // There's a --graph.
        // Check/warn that there are no quads formats mentioned
        // (RIOT will take the default graph from quads).  
        
        for ( String url : urls ) {
            Lang lang = RDFLanguages.filenameToLang(url);
            if ( lang != null && RDFLanguages.isQuads(lang) ) {
                System.err.println("Warning: Quads format given - only the default graph is loaded into the graph for --graph");
                break;
            }
        }
        
        loadOneGraph(urls);
    }

    private void loadOneGraph(List<String> urls) {
        Graph graph = getGraph();
        TDBLoader.load(graph, urls, showProgress);
        return;
    }

    private void loadQuads(List<String> urls) {
        TDBLoader.load(getDatasetGraph(), urls, showProgress, generateStats);
        return;
    }
    
    /** Tick point for messages during loading of data */
    public static int       DataTickPoint         = 100 * 1000;
    /** Tick point for messages during secondary index creation */
    public static long      IndexTickPoint        = 500 * 1000;

    /** Number of ticks per super tick */
    public static int       superTick             = 10;
    
    private static Logger LOG = LoggerFactory.getLogger("TDB2");
    
    /**
     *  For now, this is a simple loader that parses the input and adds triples/quads via {@code .add}.
     *  @see org.apache.jena.tdb.TDBLoader TDB1 Loader.
     */
    
    static class TDBLoader {

        public static void load(DatasetGraph dsg, List<String> urls, boolean showProgress, boolean generateStats) {
            StreamRDF dest = StreamRDFLib.dataset(dsg);
            Txn.executeWrite(dsg, ()->urls.forEach( (x)->loadOne(dest, x, showProgress) ));
        }

        public static void load(Graph graph, List<String> urls, boolean showProgress) {
            StreamRDF dest = StreamRDFLib.graph(graph);
            graph.getTransactionHandler().execute(()->urls.forEach((x) -> loadOne(dest, x, showProgress)));
        }
        
        private static void loadOne(StreamRDF dest, String x, boolean showProgress) {
            StreamRDF sink = dest;
            ProgressMonitor monitor = null;
            if ( showProgress ) { 
                String basename = FileOps.splitDirFile(x).get(1);
                monitor = ProgressMonitor.create(LOG, basename, DataTickPoint, superTick); 
                sink = new ProgressStreamRDF(sink, monitor);
            }
            if ( monitor!= null )
                monitor.start();
            sink.start();
            RDFDataMgr.parse(sink, x);
            sink.finish();
            if ( monitor!= null ) {
                monitor.finish();
                monitor.finishMessage();
            }
        }
    }
}
