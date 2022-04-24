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

package shacl;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.graph.Graph;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.*;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.compact.ShaclcWriter;
import org.apache.jena.shacl.compact.reader.ShaclcParseException;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;

/** SHACL parsing.
 * <p>
 * Usage: <code>shacl parse FILE</code>
 */
public class shacl_parse extends CmdGeneral {
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    private static ArgDecl argOutput   = new ArgDecl(ArgDecl.HasValue, "output", "out");
    private static ArgDecl argBase     = new ArgDecl(ArgDecl.HasValue, "base");
    private static ArgDecl argSyntax   = new ArgDecl(ArgDecl.HasValue, "syntax");

    private String baseIRI             = null;
    private Lang lang                  = null;

    private boolean printCompact       = false;
    private boolean printRDF           = false;
    private boolean printText          = false;

    private String shapesfile          = null;

    public static void main (String... argv) {
        new shacl_parse(argv).mainRun() ;
    }

    public shacl_parse(String[] argv) {
        super(argv) ;
        super.add(argOutput,  "--output=",      "Output formats: RDF, compact, text (default, text)");
        super.add(argSyntax,  "--syntax=NAME",  "Set syntax (otherwise syntax guessed from file extension)");
        super.add(argBase,    "--base=URI",     "Set the base URI (does not apply to N-triples and N-Quads)");
    }

    @Override
    protected String getSummary() {
        return "Usage: "+getCommandName()+" --out=FMT[,FMT] FILE";
    }

    @Override
    protected void processModulesAndArgs() {
         super.processModulesAndArgs();

         if ( super.hasArg(argOutput) ) {
             printCompact = false;
             printRDF = false;
             printText = false;
             // Split values.
             Function<String, Stream<String>> f = (x) -> {
                 String[] a = x.split(",");
                 return Arrays.stream(a);
             };
             List<String> values =
                 StreamOps.toList(getValues(argOutput).stream()
                     .flatMap(f)
                     .map(s->s.toLowerCase())
                     );
             printText = values.remove("text") || values.remove("t");
             printCompact = values.remove("compact") || values.remove("c");
             printRDF = values.remove("rdf") || values.remove("r") || values.remove("ttl");
             if ( values.remove("all") || values.remove("a") ) {
                 printCompact = true;
                 printRDF = true;
                 printText = true;
             }
             if ( ! values.isEmpty() )
                 throw new CmdException("Formats not recognized: "+values+" : Formats are 'text', 'compact', 'rdf','ttl' and 'all'");

         } else {
             printCompact = false;
             printRDF = false;
             printText = true;
         }

         if ( super.contains(argSyntax) ) {
             String syntax = super.getValue(argSyntax);
             Lang lang$ = RDFLanguages.nameToLang(syntax);
             if ( lang$ == null )
                 throw new CmdException("Can not detemine the syntax from '" + syntax + "'");
             this.lang = lang$;
         }

         if ( super.contains(argBase) ) {
             baseIRI = super.getValue(argBase);
             try {
                 IRIx iri = IRIs.reference(baseIRI);
                 if ( !iri.isAbsolute() )
                     throw new CmdException("Base IRI not suitable for use as a base for RDF: " + baseIRI);
             } catch (IRIException ex) {
                 throw new CmdException("Bad base IRI: " + baseIRI);
             }
         }

         if  (positionals.isEmpty() )
             // stdin
             positionals.add("-");
    }

    @Override
    protected String getCommandName() {
        return "shacl_parse";
    }

    @Override
    protected void exec() {
        boolean filesOK = true;
        for ( String fn : positionals ) {
            if ( ! "-".equals(fn) ) {
                if ( ! IO.exists(fn) ) {
                    System.err.println("File not found: "+fn);
                    filesOK = false;
                }
            }
        }
        if ( ! filesOK )
            throw new CmdException("File(s) not found");
        boolean multipleFiles = (positionals.size() > 1) ;
        positionals.forEach(fn->{
            exec(fn, multipleFiles);
        });
    }

    private void exec(String fn, boolean multipleFiles) {
        Shapes shapes;
        PrintStream out = System.out;
        PrintStream err = System.err;

        try {
            Graph g = parseFile(fn);
            shapes = Shapes.parse(g);
        } catch (ShaclParseException | ShaclcParseException | RiotNotFoundException ex) {
            // Errors parsing the RDF.
            // Errors parsing SHACL Compact Syntax.
            if ( multipleFiles )
                err.println(fn+" : ");
            err.println(ex.getMessage());
            return;
        } catch ( RiotException ex ) { /*ErrorHandler logged this */ return; }

        boolean outputByPrev = false;

        if ( printText ) {
            outputByPrev = printText(out, err, shapes);
        }
        if ( printCompact) {
            if ( outputByPrev ) {
                out.println("- - - - - - - -");
                outputByPrev = false;
            }
            outputByPrev = printCompact(out, err, shapes);
        }
        if ( printRDF) {
            if ( outputByPrev ) {
                out.println("- - - - - - - -");
                outputByPrev = false;
            }
            outputByPrev = printRDF(out, err, shapes);
        }
    }

    public Graph parseFile(String filename) {
        String baseParserIRI = this.baseIRI;
        RDFParserBuilder builder = RDFParser.create();
        if ( baseParserIRI != null )
            builder.base(baseParserIRI);
        if ( this.lang != null )
            // Always use the command line specified syntax.
            builder.forceLang(this.lang);
        else {
            // Otherwise default to SHACLC (if the parser can't guess).
            Lang lang = Lang.SHACLC;
            builder.lang(lang);
        }

        // Set the source.
        if ( filename.equals("-") ) {
            if ( baseParserIRI == null ) {
                baseParserIRI = "http://base/";
                builder.base(baseParserIRI);
            }
            filename = "stdin";
            builder.source(System.in);
        } else {
            builder.source(filename);
        }

        Graph graph = GraphFactory.createDefaultGraph();
        builder.parse(graph);
        return graph;
    }


    private boolean printText(PrintStream out, PrintStream err, Shapes shapes) {
        IndentedWriter iOut  = new IndentedWriter(out);
        ShLib.printShapes(iOut, shapes);
        iOut.ensureStartOfLine();
        iOut.flush();
        int numShapes = shapes.numShapes();
        int numRootShapes = shapes.numRootShapes();
        if ( isVerbose() ) {
            System.out.println();
            System.out.println("Target shapes: ");
            shapes.getShapeMap().forEach((n,shape)->{
                if ( shape.hasTarget() )
                    System.out.println("  "+ShLib.displayStr(shape.getShapeNode()));
            });

            System.out.println("Other Shapes: ");
            shapes.getShapeMap().forEach((n,shape)->{
                if ( ! shape.hasTarget() )
                    System.out.println("  "+ShLib.displayStr(shape.getShapeNode()));
            });
        }
        return true;
    }

    private boolean printRDF(PrintStream out, PrintStream err, Shapes shapes) {
        RDFDataMgr.write(out, shapes.getGraph(), Lang.TTL);
        return somethingWritten(shapes.getGraph());
    }

    private boolean printCompact(PrintStream out, PrintStream err, Shapes shapes) {
        try {
            ShaclcWriter.print(out, shapes);
        } catch (ShaclException ex) {
            err.println(ex.getMessage());
        }
        return somethingWritten(shapes.getGraph());
    }

    private static boolean somethingWritten(Graph graph) {
        return ! ( graph.isEmpty() && graph.getPrefixMapping().hasNoMappings() );

    }
}
