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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.riot.RiotException;
import org.apache.jena.shex.Shex;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.parser.ShexParseException;
import org.apache.jena.sys.JenaSystem;

/** ShEx parsing.
 * <p>
 * Usage: <code>shex parse FILE</code>
 */
public class shex_parse extends CmdGeneral {
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    private static ArgDecl argOutput = new ArgDecl(true, "output", "out");

    private boolean printCompact = false;
    private boolean printJSON = false;
    private boolean printRDF = false;
    private boolean printText = false;

    private String shapesfile = null;

    public static void main (String... argv) {
        new shex_parse(argv).mainRun() ;
    }

    public shex_parse(String[] argv) {
        super(argv) ;
        //super.add(argOutput, "-output=", "Output formats: RDF, compact, text (default; terse)");
        super.add(argOutput, "-output=", "Output formats: JSON, compact, RDF, text (default; terse)");
    }

    @Override
    protected String getSummary() {
        return "Usage: "+getCommandName()+" --out=FMT[,FMT] FILE";
    }

    @Override
    protected void processModulesAndArgs() {
         super.processModulesAndArgs();
         if ( super.positionals.size() == 0 ) {
             throw new CmdException(getSummary());
         }

         if ( super.hasArg(argOutput) ) {
             printCompact = false;
             printRDF = false;
             printText = false;
             printJSON = false;
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
             printRDF = values.remove("json") || values.remove("j");
             if ( values.remove("all") || values.remove("a") ) {
                 printCompact = true;
                 printRDF = true;
                 printText = true;
                 printJSON = true;
             }
             if ( ! values.isEmpty() )
                 throw new CmdException("Formats not recognized: "+values+" : Formats are 'text', 'compact', 'json', 'rdf' and 'all'");

         } else {
             printCompact = false;
             printRDF = false;
             printJSON = true;
             printText = true;
         }
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected void exec() {
        boolean multipleFiles = (positionals.size() > 1) ;
        positionals.forEach(fn->{
            exec(fn, multipleFiles);
        });
    }

    private void exec(String fn, boolean multipleFiles) {
        ShexSchema shapes;
        PrintStream out = System.out;
        PrintStream err = System.err;

        if ( ! FileOps.exists(fn) ) {
            throw new CmdException(fn+" : File not found");
        }

        try {
            shapes = Shex.readSchema(fn);
        }
        catch ( RiotException ex ) { /*ErrorHandler logged this */ return; }
        catch (ShexParseException ex) {
            StringBuilder sb = new StringBuilder();
            if ( multipleFiles )
                sb.append(fn+" : ");
            sb.append(ex.getMessage());
            throw new CmdException(sb.toString());
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

    private boolean printText(PrintStream out, PrintStream err, ShexSchema shapes) {
        Shex.printSchema(shapes);
//        IndentedWriter iOut  = new IndentedWriter(out);
//        ShLib.printShapes(iOut, shapes);
//        iOut.ensureStartOfLine();
//        iOut.flush();
//        int numShapes = shapes.numShapes();
//        int numRootShapes = shapes.numRootShapes();
//        if ( isVerbose() ) {
//            System.out.println();
//            System.out.println("Target shapes: ");
//            shapes.getShapeMap().forEach((n,shape)->{
//                if ( shape.hasTarget() )
//                    System.out.println("  "+ShLib.displayStr(shape.getShapeNode()));
//            });
//
//            System.out.println("Other Shapes: ");
//            shapes.getShapeMap().forEach((n,shape)->{
//                if ( ! shape.hasTarget() )
//                    System.out.println("  "+ShLib.displayStr(shape.getShapeNode()));
//            });
//        }
        return true;
    }

    private boolean printRDF(PrintStream out, PrintStream err, ShexSchema shapes) {
//        RDFDataMgr.write(out, shapes.getGraph(), Lang.TTL);
//        return ! shapes.getGraph().isEmpty() && ! shapes.getGraph().getPrefixMapping().hasNoMappings();
        return true;
    }

    private boolean printCompact(PrintStream out, PrintStream err, ShexSchema shapes) {
//        try {
//            ShaclcWriter.print(out, shapes);
//        } catch (ShaclException ex) {
//            err.println(ex.getMessage());
//        }
//        return ! shapes.getGraph().isEmpty() && ! shapes.getGraph().getPrefixMapping().hasNoMappings();
        return false;
    }
}
