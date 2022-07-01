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

package riotcmd;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream ;

import arq.cmdline.ModContext ;
import arq.cmdline.ModLangOutput ;
import arq.cmdline.ModLangParse ;
import arq.cmdline.ModTime ;
import org.apache.jena.Jena ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIs;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.rdfs.SetupRDFS;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.ErrorHandlerFactory.ErrorHandlerTracking;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sys.JenaSystem ;

/** Common framework for running RIOT parsers */
public abstract class CmdLangParse extends CmdGeneral
{
    static { JenaSystem.init(); }
    protected ModTime modTime                   = new ModTime() ;
    protected ModLangParse modLangParse         = new ModLangParse() ;
    protected ModLangOutput modLangOutput       = new ModLangOutput() ;
    protected SetupRDFS setup                   = null ;
    protected ModContext modContext             = new ModContext() ;
    protected ArgDecl strictDecl                = new ArgDecl(ArgDecl.NoValue, "strict") ;

    protected boolean cmdStrictMode = false ;

    protected CmdLangParse(String[] argv)
    {
        super(argv) ;
        addModule(modContext) ;
        addModule(modTime) ;
        addModule(modLangOutput) ;
        addModule(modLangParse) ;

        super.modVersion.addClass(Jena.class) ;
        //super.modVersion.addClass(RIOT.class) ;
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" [--help] [--time] [--base=IRI] [--syntax=FORMAT] [--out=FORMAT] [--count] file ..." ;
    }

    protected List<ParseRecord> outcomes = new ArrayList<>();

    protected OutputStream outputWrite = System.out ;
    protected StreamRDF outputStream   = null ;
    protected String parserBaseIRI     = null;
    protected String writerBaseIRI     = null;

    @Override
    protected void processModulesAndArgs() {
        cmdStrictMode = super.contains(strictDecl) ;
    }

    protected interface PostParseHandler { void postParse(); }

    static class ParseRecord {
        final String filename;
        final boolean success;
        final long timeMillis;
        final long triples;
        final long quads;
        final long tuples = 0;
        final ErrorHandlerTracking errHandler;

        public ParseRecord(String filename, boolean successful, long timeMillis,
                           long countTriples, long countQuads, ErrorHandlerTracking errHandler) {
            this.filename = filename;
            this.success = successful;
            this.timeMillis = timeMillis;
            this.triples = countTriples;
            this.quads = countQuads;
            this.errHandler = errHandler;
        }
    }

    @Override
    protected void exec() {
        boolean oldStrictValue = SysRIOT.isStrictMode() ;
        if ( modLangParse.strictMode() )
            SysRIOT.setStrictMode(true) ;

        parserBaseIRI = modLangParse.getBaseIRI();
        writerBaseIRI = parserBaseIRI;

        try { exec$() ; }
        finally { SysRIOT.setStrictMode(oldStrictValue) ; }
    }

    protected void exec$() {

        if ( modLangParse.getRDFSVocab() != null )
            setup = RDFSFactory.setupRDFS(modLangParse.getRDFSVocab().getGraph()) ;

        if ( modLangOutput.compressedOutput() ) {
            try { outputWrite = new GZIPOutputStream(outputWrite, true) ; }
            catch (IOException e) { IO.exception(e);}
        }

        outputStream = null ;
        PostParseHandler postParse = null ;

        outputStream = createStreamSink() ;
        if ( outputStream == null ) {
            Pair<StreamRDF, PostParseHandler> p = createAccumulateSink() ;
            outputStream = p.getLeft() ;
            postParse = p.getRight();
        }

        try {
            // The actual parsing ...
            if ( super.getPositional().isEmpty() ) {
                ParseRecord parseRec = parseFile("-");
                outcome(parseRec);
            }
            else {
                boolean b = super.getPositional().size() > 1;
                for ( String fn : super.getPositional() ) {
                    if ( b && !super.isQuiet() )
                        SysRIOT.getLogger().info("File: " + fn);
                    ParseRecord parseRec = parseFile(fn);
                    outcome(parseRec);
                }
            }
            // ... parsing done.

            if ( postParse != null )
                postParse.postParse();
            // Post parse information.
            // Total if more than one file.
            if ( super.getPositional().size() > 1 && ( modTime.timingEnabled() || modLangParse.outputCount() ) ) {
                long totalMillis = 0;
                long totalTriples = 0;
                long totalQuads = 0;
                long totalTuples = 0;
                long totalErrors = 0;
                long totalWarnings = 0;
                boolean allSuccessful = true;

                for ( ParseRecord pRec : outcomes ) {
                    if ( pRec.timeMillis >= 0 )
                        totalMillis += pRec.timeMillis;
                    totalTriples += pRec.triples;
                    totalQuads += pRec.quads;
                    totalTuples += pRec.tuples;
                    totalErrors += pRec.errHandler.getErrorCount();
                    totalWarnings += pRec.errHandler.getWarningCount();
                    allSuccessful = allSuccessful & pRec.success;
                }
                output("Total", true, totalTriples, totalQuads, totalTuples, totalMillis, totalErrors, totalWarnings);
            }
        } finally {
            if ( outputWrite != System.out )
                IO.close(outputWrite) ;
            else
                IO.flush(outputWrite);
            System.err.flush() ;
        }

        // exit(1) if there were any errors.
        for ( ParseRecord pr : outcomes ) {
            if ( ! pr.success || pr.errHandler.hadIssues() )
                throw new CmdException();
        }
    }

    public void outcome(ParseRecord rtn) {
        if ( modLangParse.outputCount() ) {
            System.err.printf("%-15s", rtn.filename);
            if ( rtn.triples > 0 )
                System.err.printf(" : Triples = %,d", rtn.triples);
            if ( rtn.quads > 0 )
                System.err.printf(" : Quads = %,d", rtn.quads);
            System.err.println();
        }
        outcomes.add(rtn);
        if ( modTime.timingEnabled() )
            output(rtn);
    }

    public ParseRecord parseFile(String filename) {
        String baseParserIRI = this.parserBaseIRI;
        RDFParserBuilder builder = RDFParser.create();
        if ( baseParserIRI != null )
            builder.base(baseParserIRI);
        if ( modLangParse.getLang() != null )
            // Always use the command line specified syntax.
            builder.forceLang(modLangParse.getLang());
        else {
            // Otherwise, use the command selected language, with N-Quads as the ultimate fallback.
            Lang lang = dftLang() ;
            if ( lang == null )
                lang = Lang.NQUADS;
            // Fall back lang if RIOT can't guess it.
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
            String scheme = IRIs.scheme(filename);
            if ( scheme == null || scheme.equalsIgnoreCase("file") )
                // Convert spaces and other characters in file names.
                // File handling will reverse the transformation to open
                // the file correctly but for base name generation we want the %20 form.
                filename = IRILib.filenameToIRI(filename);
            builder.source(filename);
        }
        return parseRIOT(builder, filename);
    }

    // Return the default (fall-back) language used if no other choice is made.
    // Contrast with --syntax=.. which forces the language.
    protected abstract Lang dftLang() ;

    protected ParseRecord parseRIOT(RDFParserBuilder builder, String filename) {
        boolean checking = true ;
        if ( modLangParse.explicitChecking() )
            checking = true;
        if ( modLangParse.explicitNoChecking() )
            checking = false;
        builder.checking(checking);
        if ( checking )
            builder.strict(true);

        // Should use ErrorHandlerRecorder
        ErrorHandlerTracking errHandler = ErrorHandlerFactory.errorHandlerTracking(ErrorHandlerFactory.stdLogger,
                                                                                   true,
                                                                                   modLangParse.stopOnWarnings());
        builder.errorHandler(errHandler);

        // Make into a cmd flag. (input and output subflags?)
        // If input is "label, then output using NodeToLabel.createBNodeByLabelRaw() ;
        // else use NodeToLabel.createBNodeByLabel() ;
        // Also, as URI.
        final boolean labelsAsGiven = false ;
//        NodeToLabel labels = SyntaxLabels.createNodeToLabel() ;
//        if ( labelsAsGiven )
//            labels = NodeToLabel.createBNodeByLabelEncoded() ;
        if ( labelsAsGiven )
            builder.labelToNode(LabelToNode.createUseLabelAsGiven());

        StreamRDF s = outputStream ;
        if ( setup != null )
            s = RDFSFactory.streamRDFS(s, setup) ;
        StreamRDFCounting sink = StreamRDFLib.count(s) ;
        s = null ;

        boolean successful = true;

        modTime.startTimer() ;
        RDFParser parser = builder.build();
        try {
            sink.start() ;
            parser.parse(sink);
            successful = true;
        }
        catch (RiotNotFoundException ex) {
            errHandler.error(ex.getMessage(), -1, -1);
            successful = false;
        }
        catch (RiotException ex) {
            successful = false;
        }
        catch (IRIException ex) {
            successful = false;
        }
        sink.finish() ;
        long x = modTime.endTimer() ;
        ParseRecord outcome = new ParseRecord(filename, successful, x, sink.countTriples(), sink.countQuads(), errHandler);
        return outcome;
    }

    /** Create a streaming output sink if possible */
    protected StreamRDF createStreamSink() {
        if ( modLangParse.toBitBucket() )
            return StreamRDFLib.sinkNull() ;

        RDFFormat fmt = modLangOutput.getOutputStreamFormat() ;
        if ( fmt == null )
            return null ;
        /** Create an accumulating output stream for later pretty printing */
        return StreamRDFWriter.getWriterStream(outputWrite, fmt, RIOT.getContext()) ;
    }

    /** Create an accumulating output stream for later pretty printing */
    protected Pair<StreamRDF, PostParseHandler> createAccumulateSink() {
        final DatasetGraph dsg = DatasetGraphFactory.create() ;
        StreamRDF sink = StreamRDFLib.dataset(dsg) ;
        final RDFFormat fmt = modLangOutput.getOutputFormatted() ;
        PostParseHandler handler = ()->{
            RDFWriterBuilder builder = RDFWriter.create();
            builder.format(fmt);
            if ( RDFLanguages.isQuads(fmt.getLang()) )
                builder.source(dsg);
            else
                builder.source(dsg.getDefaultGraph());
            String baseURI = writerBaseIRI;
            if ( baseURI != null )
                builder.base(baseURI);
            builder.output(outputWrite);
        };
        return Pair.create(sink, handler) ;
    }

    protected Tokenizer makeTokenizer(InputStream in) {
        Tokenizer tokenizer = TokenizerText.create().source(in).build() ;
        return tokenizer ;
    }

    protected void output(ParseRecord rtn) {
        output(rtn.filename, rtn.success,
               rtn.triples, rtn.quads, rtn.tuples,
               rtn.timeMillis, rtn.errHandler.getErrorCount(),
               rtn.errHandler.getWarningCount()) ;
    }

    protected void output(String label, boolean success, long numberTriples, long numberQuads, long numberTuples, long timeMillis, long errorCount, long warningCount) {
        double timeSec = timeMillis/1000.0 ;
        long total = numberTriples + numberQuads + numberTuples;
        StringBuilder sb = new StringBuilder();
        if ( total > 0 ) {
            sb.append(String.format("%-15s", label));
            if ( success )
                appendFmt(sb, " : %,4.2f sec", timeSec);
            appendCount(sb, numberTriples, "Triple", "Triples", "TPS");
            appendCount(sb, numberQuads,   "Quad",   "Quads",   "QPS");
            appendCount(sb, numberTuples,  "Tuple",  "Tuples",  "TPS");
            if ( success && timeMillis > 0 )
                appendFmt(sb," : %,.2f %s", total/timeSec, "per second");
        } else {
            appendFmt(sb, "%s :  (No Output)", label) ;
        }
        if ( errorCount > 0 || warningCount > 0 ) {
            appendFmt(sb," : %,d %s", errorCount, "errors");
            appendFmt(sb," : %,d %s", warningCount, "warnings");
        }
        System.err.println(sb.toString());
    }

    private void appendFmt(StringBuilder sb, String fmt, Object ... args) {
        sb.append(String.format(fmt, args)) ;
    }

    private void appendCount(StringBuilder sb, long number, String itemName, String itemsName, String rateName) {
        if ( number > 0 ) {
            String str = itemName;
            if ( number > 1 )
                str=itemsName;
            sb.append(String.format(" : %,d %s", number, str));
        }
    }

    protected void output(String label) {
        System.err.printf("%s : \n", label) ;
    }
}
