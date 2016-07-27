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

package sdb;

import java.util.List ;

import jena.cmd.ArgDecl;
import jena.cmd.CmdException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.ProgressMonitor ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ProgressStreamRDF ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.SDBFactory ;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.store.StoreBaseHSQL ;
import org.apache.jena.sdb.store.StoreLoaderPlus ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.Quad ;
import sdb.cmd.CmdArgsDB ;
import sdb.cmd.ModGraph ;
 
 /** Load data files into an SDB model in a database.
  * 
  *  <p>
  *  Usage:<pre>
  *    sdbload [db spec] file [file ...]
  *  </pre>
  *  The syntax of a file is determimed by its extension (.n3, .nt) and defaults to RDF/XML. 
  *  </p>
  */ 
 
public class sdbload extends CmdArgsDB {
    private static final String usage           = "sdbload --sdb <SPEC> [--graph=IRI] file" ;

    private static ModGraph     modGraph        = new ModGraph() ;
    private static ArgDecl      argDeclTruncate = new ArgDecl(false, "truncate") ;
    private static ArgDecl      argDeclReplace  = new ArgDecl(false, "replace") ;

    public static void main(String... argv) {
        SDB.init() ;
        new sdbload(argv).mainRun() ;
    }

    String filename = null ;

    public sdbload(String... args) {
        super(args) ;
        addModule(modGraph) ;
        add(argDeclTruncate) ;
        add(argDeclReplace) ;
    }

    @Override
    protected String getCommandName() {
        return Lib.className(this) ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " <SPEC> [--graph IRI] file ..." ;
    }

    @Override
    protected void processModulesAndArgs() {
        if ( getNumPositional() == 0 )
            cmdError("Need filenames of RDF data to load", true) ;
    }

    @Override
    protected void execCmd(List<String> args) {
        if ( contains(argDeclTruncate) )
            getStore().getTableFormatter().truncate() ;
        for ( String x : args )
            loadOne(x, contains(argDeclReplace)) ;
        StoreBaseHSQL.close(getStore()) ;
    }
    
    private static ProgressMonitor.Output output = (fmt, args)-> {
        System.out.printf(fmt, args) ;
        if ( ! fmt.endsWith("\n") )
            System.out.println() ;
    } ;

    private void loadOne(String filename, boolean replace) {
        Model model = null ;
        Dataset dataset = null ;
        PrefixMapping pmap ;
        
        Lang lang = RDFLanguages.filenameToLang(filename) ;
        if ( lang == null )
            throw new CmdException("Data syntax not recognized: " + filename) ;

        // --graph or not
        if ( modGraph.getGraphName() != null ) {
            model = modGraph.getModel(getStore()) ;
            pmap = model;
        } else {
            dataset = SDBFactory.connectDataset(getStore()) ;
            pmap = dataset.asDatasetGraph().getDefaultGraph().getPrefixMapping() ;
        }

        // For monitoring only.
        Graph monitorGraph = (model == null) ? null : model.getGraph() ;

        if ( replace ) {
            if ( model != null )
                model.removeAll() ;
            else
                dataset.asDatasetGraph().clear() ;
        }
        
        boolean showProgress = isVerbose() || getModTime().timingEnabled() ;

        if ( showProgress )
            output.print("Start load: %s", filename) ;
        
        StreamRDF stream = streamToStore(pmap, getStore()) ;
        if ( modGraph.getGraphName() != null ) {
            Node gn = NodeFactory.createURI(modGraph.getGraphName()) ;
            stream = StreamRDFLib.extendTriplesToQuads(gn, stream) ;
        }
        
        ProgressMonitor progress = null ;
        if ( showProgress ) {
            progress = new ProgressMonitor(filename, 100_000, 10, output) ;
            stream = new ProgressStreamRDF(stream, progress) ;
        }
        
        if ( progress != null )
            progress.start(); 
        
        // Load!
        RDFDataMgr.parse(stream, filename, lang) ;
        
        if ( progress != null ) {
            progress.finish() ; 
            progress.finishMessage();
        }
    }

    private StreamRDF streamToStore(PrefixMapping pmap, Store store) {
        StoreLoaderPlus sl = (StoreLoaderPlus)store.getLoader() ;
        return new StreamRDF() {

            @Override
            public void start() {
                sl.startBulkUpdate();
            }

            @Override
            public void triple(Triple triple) {
                sl.addTriple(triple);
            }

            @Override
            public void quad(Quad quad) {
                sl.addQuad(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            }

            @Override
            public void base(String base) {}

            @Override
            public void prefix(String prefix, String iri) {
                pmap.setNsPrefix(prefix, iri) ;
            }

            @Override
            public void finish() {
                sl.finishBulkUpdate();
            }
        } ;
    }
}
