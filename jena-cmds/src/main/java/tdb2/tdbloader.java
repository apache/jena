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
import java.util.Objects;

import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.loader.DataLoader;
import org.apache.jena.tdb2.loader.LoaderFactory;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import tdb2.cmdline.CmdTDB;
import tdb2.cmdline.CmdTDBGraph;

public class tdbloader extends CmdTDBGraph {
    private static final ArgDecl argStats = new ArgDecl(ArgDecl.HasValue,  "stats");
    private static final ArgDecl argLoader = new ArgDecl(ArgDecl.HasValue, "loader");
    
    private enum LoaderEnum { Basic, Parallel, Sequential }
    
    private boolean showProgress = true;
    private boolean generateStats = false;
    private LoaderEnum loader = null;
    
    public static void main(String... args) {
        CmdTDB.init();
        new tdbloader(args).mainRun();
    }

    protected tdbloader(String[] argv) {
        super(argv);
//        super.add(argStats, "Generate statistics");
        super.add(argLoader, "--loader", "Loader to use");
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        
        if ( contains(argLoader) ) {
            String loadername = getValue(argLoader).toLowerCase();
            if ( loadername.matches("basic.*") )
                loader = LoaderEnum.Basic;
            else if ( loadername.matches("seq.*") )
                loader = LoaderEnum.Sequential;
            else if ( loadername.matches("para.*") )
                loader = LoaderEnum.Parallel;
            else
                throw new CmdException("Unrecognized value for --loader: "+loadername);
        }

        if ( super.contains(argStats) ) {
            if ( ! hasValueOfTrue(argStats) && ! hasValueOfFalse(argStats) )
                throw new CmdException("Not a boolean value: "+getValue(argStats));
            generateStats = super.hasValueOfTrue(argStats);
        }
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " [--desc DATASET | --loc DIR] FILE ...";
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
        
        List<String> urls = getPositional();
        if ( urls.size() == 0 )
            urls.add("-");

        if ( graphName == null ) {
            loadQuads(urls);
            return;
        }
        
        // There's a --graph.
        // Check/warn that there are no quads formats mentioned
        
        for ( String url : urls ) {
            Lang lang = RDFLanguages.filenameToLang(url);
            if ( lang != null && RDFLanguages.isQuads(lang) ) {
                throw new CmdException("Warning: Quads format given - only the default graph is loaded into the graph for --graph");
            }
        }
        
        loadTriples(graphName, urls);
    }

    private void loadTriples(String graphName, List<String> urls) {
        execBulkLoad(super.getDatasetGraph(), graphName, urls, showProgress);
    }

    private void loadQuads(List<String> urls) {
        // generateStats
        execBulkLoad(super.getDatasetGraph(), null, urls, showProgress);
    }
    
    private long execBulkLoad(DatasetGraph dsg, String graphName, List<String> urls, boolean showProgress) {
        DataLoader loader = chooseLoader(dsg, graphName);
        long elapsed = Timer.time(()->{
                    loader.startBulk();
                    loader.load(urls);
                    loader.finishBulk();
        });
        return elapsed;
    }

    /** Decide on the bulk loader. */ 
    private DataLoader chooseLoader(DatasetGraph dsg, String graphName) {
        Objects.requireNonNull(dsg);
        Node gn = null;
        if ( graphName != null )
            gn = NodeFactory.createURI(graphName);
        
        LoaderEnum useLoader = loader; 
        if ( useLoader == null )
            useLoader = LoaderEnum.Parallel;
        
        MonitorOutput output = isQuiet() ? LoaderOps.nullOutput() : LoaderOps.outputToLog();
        DataLoader loader = createLoader(useLoader, dsg, gn, output);
        if ( output != null )
            output.print("Loader = %s", loader.getClass().getSimpleName());
        return loader ;
    }
        
    private DataLoader createLoader(LoaderEnum useLoader, DatasetGraph dsg, Node gn, MonitorOutput output) {
        switch(useLoader) {
            case Parallel :
                return LoaderFactory.parallelLoader(dsg, gn, output);
            case Sequential :
                return LoaderFactory.sequentialLoader(dsg, gn, output);
            case Basic :
                return LoaderFactory.basicLoader(dsg, gn, output);
            default :
                throw new InternalErrorException("Unrecognized loader: "+useLoader);
        }
    }
}
