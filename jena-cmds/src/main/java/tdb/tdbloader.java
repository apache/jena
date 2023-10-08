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

package tdb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.TDB1Loader;
import org.apache.jena.tdb1.store.GraphTDB;
import org.apache.jena.util.FileUtils;
import tdb.cmdline.CmdTDB;
import tdb.cmdline.CmdTDBGraph;

public class tdbloader extends CmdTDBGraph {
    private static final ArgDecl argNoStats = new ArgDecl(ArgDecl.NoValue, "nostats") ;
    private static final ArgDecl argStats = new ArgDecl(ArgDecl.HasValue,  "stats") ;
    private static final ArgDecl argSyntax  = new ArgDecl(ArgDecl.HasValue, "syntax") ;

    private boolean showProgress = true;
    private boolean generateStats = true;
    private Lang lang = null;

    static public void main(String...argv) {
        CmdTDB.init();
        TDB1.setOptimizerWarningFlag(false);
        new tdbloader(argv).mainRun();
    }

    protected tdbloader(String[] argv) {
        super(argv);
//        super.getUsage().startCategory("Stats");
        super.add(argNoStats, "--nostats",     "Switch off statistics gathering");
        super.add(argSyntax,  "--syntax=LANG", "Syntax of data from stdin");
        super.add(argStats);   // Hidden argument
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        if ( super.contains(argSyntax) ) {
            String syntax = super.getValue(argSyntax);
            Lang lang$ = RDFLanguages.nameToLang(syntax);
            if ( lang$ == null )
                throw new CmdException("Can not detemine the syntax from '" + syntax + "'");
            this.lang = lang$;
        }
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
            if ( !hasValueOfTrue(argStats) && !hasValueOfFalse(argStats) )
                throw new CmdException("Not a boolean value: " + getValue(argStats));
            generateStats = super.hasValueOfTrue(argStats);
        }

        if ( super.contains(argNoStats) )
            generateStats = false;

        List<String> urls = getPositional();
        if ( urls.size() != 0 )
            checkFiles(urls);

        if ( graphName == null ) {
            if ( urls.size() == 0 ) {
                checkFiles(urls);
                loadQuadsStdin();
            } else {
                loadQuads(urls);
            }
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

        loadNamedGraph(urls);
    }

    // Check files exists before starting.
    private void checkFiles(List<String> urls) {
        List<String> problemFiles = ListUtils.toList(urls.stream().filter(u -> FileUtils.isFile(u))
                                                         // Only check local files.
                                                         .map(Paths::get)
                                                         .filter(p -> !Files.exists(p) || !Files.isRegularFile(p /* follow
                                                                                                                  * links */)
                                                                      || !Files.isReadable(p))
                                                         .map(Path::toString));
        if ( !problemFiles.isEmpty() ) {
            String str = String.join(", ", problemFiles);
            throw new CmdException("Can't read files : " + str);
        }
    }

// void loadDefaultGraph(List<String> urls) {
// GraphTDB graph = getGraph() ;
// TDBLoader.load(graph, urls, showProgress) ;
// return ;
// }

    void loadNamedGraph(List<String> urls) {
        GraphTDB graph = getGraph();
        TDB1Loader.load(graph, urls, showProgress);
        return;
    }

    void loadQuads(List<String> urls) {
        TDB1Loader.load(getDatasetGraphTDB(), urls, showProgress, generateStats);
        return;
    }

    private void loadQuadsStdin() {
        TDB1Loader.load(getDatasetGraphTDB(), System.in, lang, showProgress, generateStats);
    }
}
