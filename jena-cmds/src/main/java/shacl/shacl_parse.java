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

import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import jena.cmd.CmdGeneral;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.compact.ShaclcWriter;
import org.apache.jena.shacl.compact.reader.ShaclcParseException;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.ShaclParseException;
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

    private static ArgDecl argOutput = new ArgDecl(true, "output", "out");

    private boolean printCompact = false;
    private boolean printRDF = false;
    private boolean printText = false;

    private String shapesfile = null;

    public static void main (String... argv) {
        new shacl_parse(argv).mainRun() ;
    }

    public shacl_parse(String[] argv) {
        super(argv) ;
        super.add(argOutput);
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" --out=FMT[,FMT] FILE";
    }

    @Override
    protected void processModulesAndArgs() {
         super.processModulesAndArgs();
         if ( super.positionals.size() == 0 ) {
             System.err.println(getSummary());
             System.exit(0);
         }

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
             printRDF = values.remove("rdf") || values.remove("r");
             if ( values.remove("all")) {
                 printCompact = true;
                 printRDF = true;
                 printText = true;
             }
             if ( ! values.isEmpty() )
                 throw new CmdException("Formats not recognized: "+values+" : Formats are 'text', 'compact', 'rdf' and 'all'");

         } else {
             printCompact = false;
             printRDF = false;
             printText = true;
         }
    }

    @Override
    protected String getCommandName() {
        return "shacl_parse";
    }

    @Override
    protected void exec() {
        boolean multipleFiles = (positionals.size() > 1) ;
        positionals.forEach(fn->{
            exec(fn, multipleFiles);
        });
    }

    private void exec(String fn, boolean multipleFiles) {
        Shapes shapes;
        PrintStream out = System.out;
        PrintStream err = System.err;

        if ( ! FileOps.exists(fn) ) {
            err.println(fn+" : File not found");
            return;
        }

        try {
            shapes = Shapes.parse(fn);
        }
        catch ( RiotException ex ) { /*ErrorHandler logged this */ return; }
        catch (ShaclParseException | ShaclcParseException /*| RiotException*/ ex) {
            // Errors parsing the RDF.
            // Errors parsing SHACL Compact Syntax.
            if ( multipleFiles )
                err.println(fn+" : ");
            err.println(ex.getMessage());
            return;
        }

        boolean outputByPrev = false;

        if ( printText ) {
            outputByPrev = printText(out, err, shapes);
        }
        if ( printCompact) {
            if ( outputByPrev ) {
                out.println("- - - - - - - - ");
                outputByPrev = false;
            }
            outputByPrev = printCompact(out, err, shapes);
        }
        if ( printRDF) {
            if ( outputByPrev ) {
                out.println("- - - - - - - - ");
                outputByPrev = false;
            }
            outputByPrev = printRDF(out, err, shapes);
        }
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
        return ! shapes.getGraph().isEmpty() && ! shapes.getGraph().getPrefixMapping().hasNoMappings();
    }

    private boolean printCompact(PrintStream out, PrintStream err, Shapes shapes) {
        try {
            ShaclcWriter.print(out, shapes);
        } catch (ShaclException ex) {
            err.println(ex.getMessage());
        }
        return ! shapes.getGraph().isEmpty() && ! shapes.getGraph().getPrefixMapping().hasNoMappings();
    }
}
