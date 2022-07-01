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

package shex;

import java.io.OutputStream;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.shex.*;
import org.apache.jena.shex.sys.ShexLib;
import org.apache.jena.sys.JenaSystem;

/** ShEx validation.
 * <p>
 * Usage: <code>shex validate [--text] --shapes SHAPES --data DATA</code>
 */
public class shex_validate extends CmdGeneral {

    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    private ArgDecl argOutputText  = new ArgDecl(false, "--text");
    //private ArgDecl argOutputRDF   = new ArgDecl(false, "--rdf");
    private ArgDecl argData        = new ArgDecl(true, "--data", "--datafile", "-d");
    private ArgDecl argShapes      = new ArgDecl(true, "--schema", "--shapes", "--shapesfile", "--shapefile", "-s");
    private ArgDecl argShapeMap    = new ArgDecl(true, "--shapesMap", "--shapesmap","--map", "-m");
    private ArgDecl argTargetNode  = new ArgDecl(true, "--target", "--node", "-n");

    private String  datafile = null;
    private String  shapesfile = null;
    private String  mapfile = null;
    private String  targetNode = null;  // Parse later.
    private boolean textOutput = false;

    public static void main (String... argv) {
        new shex_validate(argv).mainRun() ;
    }

    public shex_validate(String[] argv) {
        super(argv) ;
        super.add(argShapes,        "--schema", "Shapes file");
        super.add(argData,          "--data",   "Data file");
        super.add(argShapeMap,      "--map",    "ShEx shapes map");
        super.add(argTargetNode,    "--target", "Validate specific node [may use prefixes from the data]");
        super.add(argOutputText,    "--text",   "Output in concise text format");
        //super.add(argOutputRDF,  "--rdf", "Output in RDF (Turtle) format");
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" [--target URI|--map mapsFile] --shapes shapesFile --data dataFile";
    }

    @Override
    protected void processModulesAndArgs() {
         super.processModulesAndArgs();

         datafile = super.getValue(argData);
         shapesfile = super.getValue(argShapes);

         // No -- arguments, use act on single file of shapes and data.
         if ( datafile == null && shapesfile == null ) {
             if ( positionals.size() == 1 ) {
                 datafile = positionals.get(0);
                 shapesfile = positionals.get(0);
             }
         }

         if ( datafile == null )
             throw new CmdException("Usage: "+getSummary());
         if ( shapesfile == null )
             shapesfile = datafile;

         textOutput = super.hasArg(argOutputText);

         if ( contains(argTargetNode) )
             targetNode = getValue(argTargetNode);

         if ( contains(argShapeMap) )
             mapfile = getValue(argShapeMap);

         if ( targetNode != null && mapfile != null )
             throw new CmdException("Only one of target node and map file");

         if ( targetNode == null && mapfile == null )
             throw new CmdException("One of target node and map file required");
    }

    static OutputStream output = System.out;

    @Override
    protected void exec() {

        ShexSchema shapes;
        try {
            shapes = Shex.readSchema(shapesfile);
        } catch (ShexException ex) {
            throw new CmdException("Failed to read shapes: " + ex.getMessage());
        }

        Graph dataGraph;
        try {
            dataGraph = load(datafile, "data file");
        } catch (RiotException ex) {
            throw new CmdException("Failed to load data: " + ex.getMessage());
        }

//        if ( targetNode != null ) {
//            String x = dataGraph.getPrefixMapping().expandPrefix(targetNode);
//            Node node = NodeFactory.createURI(x);
//            ShexValidation.validate(graphData, shapes, shapeRef, focus)
//        }

        if ( mapfile != null ) {
            ShexMap map;
            try {
                map = Shex.readShapeMap(mapfile);
            } catch (ShexException ex) {
                throw new CmdException("Failed to read shapes map: " + ex.getMessage());
            }
            ShexReport report = ShexValidator.get().validate(dataGraph, shapes, map);
            ShexLib.printReport(output, report);
            return;
        }

        // Or --shapeURI
        ShexShape startShape = shapes.getStart();
        if ( startShape == null )
            throw new CmdException("Start node required for URI-validation");

        String targetURIstr = dataGraph.getPrefixMapping().expandPrefix(targetNode);
        Node focus = NodeFactory.createURI(targetURIstr);

//        if ( isVerbose() )
//            ValidationContext.VERBOSE = true;

//        ValidationReport report = ( node != null )
//            ? ShaclValidator.get().validate(shapesGraph, dataGraph, node)
//            : ShaclValidator.get().validate(shapesGraph, dataGraph);

        ShexReport report = ShexValidator.get().validate(dataGraph, shapes, startShape, focus);
        ShexLib.printReport(output, report);
    }

    private Graph load(String filename, String scope) {
        try {
            Graph graph = RDFDataMgr.loadGraph(filename);
            return graph;
        } catch (RiotException ex) {
            System.err.println("Loading "+scope);
            throw ex;
        }
    }

    @Override
    protected String getCommandName() {
        return "shex_validate";
    }
}
