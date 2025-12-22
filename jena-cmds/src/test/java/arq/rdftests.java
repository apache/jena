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

package arq;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import arq.cmdline.ModContext;
import arq.cmdline.ModEngine;
import org.apache.jena.Jena;
import org.apache.jena.arq.junit.EarlReport;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.riot.ParsingStepForTest;
import org.apache.jena.arq.junit.riot.RiotTests;
import org.apache.jena.arq.junit.riot.SemanticsTests;
import org.apache.jena.arq.junit.riot.VocabLangRDF;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.arq.junit.sparql.tests.QueryEvalTest;
import org.apache.jena.arq.junit.textrunner.TextTestRunner;
import org.apache.jena.atlas.legacy.BaseTest2;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdMain;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.lang.turtlejcc.TurtleJCC;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sparql.vocabulary.DOAP;
import org.apache.jena.sparql.vocabulary.EARL;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.TestManifest;
import org.apache.jena.vocabulary.XSD;

/** A program to execute test suites by manifest.
 *
 * <pre>
 * Usage:
 *   [--strict]
 *   [--earl]
 *   <i>testManifest</i>
 * </pre>
 */

public class rdftests extends CmdMain {
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // Test runners are in jena-arq, package org.apache.jena.arq.junit.runners

    public static void main(String...argv) {
        try { new rdftests(argv).mainRun(); }
        catch (TerminationException ex) {
            System.exit(ex.getCode());
        }
    }

    protected ModEngine  modEngine         = new ModEngine();
    protected ModContext modContext        = new ModContext();
    protected ArgDecl    strictDecl        = new ArgDecl(ArgDecl.NoValue, "strict");
    protected boolean    cmdStrictMode     = false;

    // Use the alternative Turtle parser which is JavaCC based.
    protected ArgDecl    useTTLjcc         = new ArgDecl(ArgDecl.NoValue, "ttljcc");
    protected ArgDecl    useARQ            = new ArgDecl(ArgDecl.NoValue, "arq");
    // Run with ".rq" as ARQ extended syntax.
    protected boolean    argAsNormal       = false;

    protected ArgDecl    earlDecl          = new ArgDecl(ArgDecl.NoValue, "earl");
    protected boolean    createEarlReport  = false;

    private static final PrintStream earlOut = System.out;

    private static boolean strictMode = false;

    protected rdftests(String[] argv) {
        super(argv);
        super.modVersion.addClass(Jena.class);
        addModule(modEngine);
        addModule(modContext);

        getUsage().startCategory("Tests (execute test manifest)");
        add(useARQ,       "--arq",     "Operate with ARQ syntax");
        add(useTTLjcc,    "--ttljcc",  "Use the alternative Turtle parser in tests");
        add(strictDecl,   "--strict",  "Operate in strict mode (no extensions of any kind)");
        add(earlDecl,     "--earl",    "Create EARL report");
        getUsage().addUsage("<manifest> ...", "run the tests specified in the given manifest");
    }

    @Override
    protected String getCommandName() { return Lib.className(this); }

    @Override
    protected String getSummary() { return getCommandName()+" <manifest>"; }

    @Override
    protected void processModulesAndArgs() {
        if ( !hasPositional() )
            throw new CmdException("No manifest file");
        createEarlReport = contains(earlDecl);
        cmdStrictMode = super.hasArg(strictDecl);
        if ( contains(useTTLjcc) )
            ParsingStepForTest.registerAlternative(Lang.TURTLE, TurtleJCC.factory);
        argAsNormal = contains(useARQ);
    }

    @Override
    protected void exec() {

        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
        EarlReport report = createEarlReport ? new EarlReport(systemURI) : null;

        BaseTest2.setTestLogging();

        if ( cmdStrictMode ) {
            // Which will apply to reading the manifests!
            ARQ.setStrictMode();
            SysRIOT.setStrictMode(true);
            QueryEvalTest.compareResultSetsByValue = false;
        }

        if ( argAsNormal )
            SparqlTests.defaultForSyntaxTests = Syntax.syntaxARQ;
        else
            SparqlTests.defaultForSyntaxTests = Syntax.syntaxSPARQL_12;

        List<String> manifests = getPositional();
        System.out.println("# Run: "+manifests);
        exec(report, manifests);

        if ( createEarlReport ) {
            earlOut.println();
            earlOut.println("# Apache Jena EARL Report");

            Model model = report.getModel();

            // Lang
            model.setNsPrefix("rdft", VocabLangRDF.getURI()) ;
            // SPARQL
            model.setNsPrefix("dawg", TestManifest.getURI()) ;
            // ---
            Model meta = metadata(report);

            // Write meta separately so it is easy to find and can be extracted.
            RDFDataMgr.write(earlOut, model, Lang.TURTLE);
            earlOut.println();
            RDFDataMgr.write(earlOut, meta, Lang.TURTLE);
        }
    }

    protected void exec(EarlReport earlReport, List<String> manifests) {
        if ( manifests.isEmpty() )
            throw new CmdException("No manifest files");
        if ( createEarlReport )
            TextTestRunner.run(earlReport, manifests);
        else
            TextTestRunner.run(manifests);
    }

    // Test subsystems.
    private static List<Function<ManifestEntry, Runnable>> installed = new ArrayList<>();

    public static void installTestMaker(Function<ManifestEntry, Runnable> testMaker) {
        installed.add(testMaker);
    }

    static {
        installTestMaker(RiotTests::makeRIOTTest);
        installTestMaker(SparqlTests::makeSPARQLTest);
        installTestMaker(SemanticsTests::makeSemanticsTest);
    }

    private static String name =  "Apache Jena";
    private static String releaseVersion =  ARQ.VERSION;
    private static String homepageStr = "https://jena.apache.org/";
    private static String systemURI = "http://jena.apache.org/#jena";  // Null for bNode.

    // Generate metadata into a separate model. Does not update the report.
    // Should have a subset of the EARL report prefixes.
    private static Model metadata(EarlReport report) {
        Model model = ModelFactory.createDefaultModel();
        Resource homepage = model.createResource(homepageStr);

        model.setNsPrefix("rdf", RDF.getURI()) ;
        //model.setNsPrefix("rdfs", RDFS.getURI()) ;
        model.setNsPrefix("earl", EARL.getURI()) ;
        model.setNsPrefix("foaf", FOAF.getURI()) ;
        model.setNsPrefix("doap", DOAP.getURI()) ;
        model.setNsPrefix("xsd", XSD.getURI()) ;
        model.setNsPrefix("rdft", "http://www.w3.org/ns/rdftest#");
        model.setNsPrefix("dc", DC.getURI());

        Resource system = report.getSystem();

        if ( name != null )
            model.add(system, DC.title, name);

        Resource who = model.createResource(FOAF.Agent)
                    .addProperty(FOAF.name, "Apache Jena Community")
                    .addProperty(FOAF.homepage, homepage);

        model.add(system, DC.creator, who);
        model.add(system, RDF.type, DOAP.Project);
        model.add(system, DOAP.name, name);
        model.add(system, DOAP.homepage, homepage);
        model.add(system, DOAP.developer, who);
        model.add(system, DOAP.maintainer, who);
        model.add(system, DOAP.shortdesc,  model.createLiteral("RDF and SPARQL triple store", "en"));
        model.add(system, DOAP.description, model.createLiteral("Apache Jena : RDF system and SPARQL triple store", "en"));

        Resource release = model.createResource(DOAP.Version);
        model.add(system, DOAP.release, release);

//        GregorianCalendar gCal = new GregorianCalendar();
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        ZonedDateTime zdt = gCal.toZonedDateTime();
//        String lex = fmt.format(zdt) ;

        Node today_node = NodeFactoryExtra.todayAsDate();
        Literal today = model.createTypedLiteral(today_node.getLiteralLexicalForm(), today_node.getLiteralDatatype());
        model.add(release, DOAP.created, today);
        model.add(release, DOAP.revision, releaseVersion);
        model.add(release, DOAP.homepage, homepage);
        return model;
    }
 }
