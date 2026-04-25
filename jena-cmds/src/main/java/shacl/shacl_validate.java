/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package shacl;

import java.util.List;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdMain;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.shacl.Imports;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;

/** SHACL validation.
 * <pre>
 * Usage: <code>shacl validate [--text] [-v|--verbose] [--target=] --shapes SHAPES --data DATA</code>
 * Usage: <code>shacl validate [--text] [-v|--verbose] [--target=] FILE</code>
 * </pre>
 */
public class shacl_validate extends CmdMain {

    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    private ArgDecl argOutputText  = new ArgDecl(false, "--text");
    //private ArgDecl argOutputRDF   = new ArgDecl(false, "--rdf");
    private ArgDecl argData        = new ArgDecl(true, "--data", "--datafile", "-d");
    private ArgDecl argShapes      = new ArgDecl(true, "--shapes", "--shapesfile", "--shapefile", "-s");
    private ArgDecl argTargetNode  = new ArgDecl(true, "--target", "--node", "-n", "-t");

    // Allow multiple files for each - combine into one graph each for data and for shapes.
    private List<String> datafiles = null;
    private List<String> shapesfiles = null;
    private String shapesURL = null;
    private String  targetNode = null;  // Parse later.
    private boolean textOutput = false;

    public static void main (String... argv) {
        new shacl_validate(argv).mainRun() ;
    }

    public shacl_validate(String[] argv) {
        super(argv) ;
        super.add(argShapes,        "--shapes", "Shapes file");
        super.add(argData,          "--data",   "Data file");
        super.add(argTargetNode,    "--target", "Validate specific node [may use prefixes from the data]");
        super.add(argOutputText,    "--text",   "Output in concise text format");
        //super.add(argOutputRDF,  "--rdf", "Output in RDF (Turtle) format");
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" [--target URI] [--shapes shapesFile --data dataFile] [FILE ...]";
    }

    @Override
    protected void processModulesAndArgs() {
         super.processModulesAndArgs();

         // --data can be empty in which case shapes and data are in the shapes files.
         datafiles = super.getValues(argData);
         shapesfiles = super.getValues(argShapes);

         // If there are no arguments, act the commandline positional arguments as shapes and data.
         if ( datafiles.isEmpty() && shapesfiles.isEmpty() ) {
             if ( positionals.isEmpty() )
                 throw new CmdException("No input");
             shapesfiles = positionals;
         }
         if ( shapesfiles == null || shapesfiles.isEmpty() )
             throw new CmdException("No shapes files");

         textOutput = super.hasArg(argOutputText);

         if ( contains(argTargetNode) )
             targetNode = getValue(argTargetNode);

         // For imports.
         shapesURL = shapesfiles.getFirst();
    }

    @Override
    protected void exec() {
        Graph shapesGraph = load(shapesfiles);
        Graph dataGraph;
        if ( datafiles.isEmpty() )
            dataGraph = shapesGraph;
        else
            dataGraph = load(datafiles);

        Node node = null;
        if ( targetNode != null ) {
            String x = dataGraph.getPrefixMapping().expandPrefix(targetNode);
            node = NodeFactory.createURI(x);
        }

        shapesGraph = Imports.withImports(shapesURL, shapesGraph);

        if ( isVerbose() )
            ValidationContext.VERBOSE = true;

        ValidationReport report = ( node != null )
            ? ShaclValidator.get().validate(shapesGraph, dataGraph, node)
            : ShaclValidator.get().validate(shapesGraph, dataGraph);

        if ( isVerbose() )
            System.out.println();

        if ( textOutput )
            ShLib.printReport(report);
        else
            RDFDataMgr.write(System.out, report.getGraph(), Lang.TTL);
    }

    private Graph load(List<String> files) {
        Graph graph = GraphFactory.createDefaultGraph();
        files.forEach(fn-> {
            try {
                RDFDataMgr.read(graph, fn);
            } catch (RiotException ex) {
                System.err.println("Error loading "+fn);
                throw ex;
            }
        });
        return graph;
    }

    @Override
    protected String getCommandName() {
        return "shacl_validate";
    }
}
