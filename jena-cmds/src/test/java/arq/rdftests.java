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
import org.apache.jena.arq.junit.SurpressedTest;
import org.apache.jena.arq.junit.TextTestRunner;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.riot.RiotTests;
import org.apache.jena.arq.junit.riot.VocabLangRDF;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.atlas.legacy.BaseTest2;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
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
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.junit.EarlReport;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sparql.vocabulary.DOAP;
import org.apache.jena.sparql.vocabulary.EARL;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sparql.vocabulary.TestManifest;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
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

public class rdftests extends CmdGeneral
{
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...argv) {
        try { new rdftests(argv).mainRun(); }
        catch (TerminationException ex) {
            System.exit(ex.getCode());
        }
    }

    protected ModContext modContext        = new ModContext();
    protected ArgDecl    strictDecl        = new ArgDecl(ArgDecl.NoValue, "strict");
    protected boolean    cmdStrictMode     = false;

    protected ArgDecl    arqDecl           = new ArgDecl(ArgDecl.NoValue, "arq");
    // Run with ".rq" as ARQ extended syntax.
    protected boolean    arqAsNormal       = false;

    protected ArgDecl    earlDecl          = new ArgDecl(ArgDecl.NoValue, "earl");
    protected boolean    createEarlReport  = false;

    protected ArgDecl    baseDecl          = new ArgDecl(ArgDecl.HasValue, "base");
    protected String     baseURI           = null;

    private static final PrintStream earlOut = System.out;

    private static boolean strictMode = false;

    protected rdftests(String[] argv) {
        super(argv);
//        super.add(baseDecl, "--base=URI", "Set the base URI");
        super.modVersion.addClass(ARQ.class);
        getUsage().startCategory("Tests (execute test manifest)");
        getUsage().addUsage("<manifest>", "run the tests specified in the given manifest");
        add(arqDecl, "--arq",       "Operate with ARQ syntax");
        add(strictDecl, "--strict", "Operate in strict mode (no extensions of any kind)");
        add(earlDecl, "--earl", "create EARL report");
        addModule(modContext);
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
        if ( contains(baseDecl) )
            baseURI = super.getValue(baseDecl);
        arqAsNormal = contains(arqDecl);
    }

    @Override
    protected void exec() {
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
        EarlReport report = new EarlReport(systemURI);

        BaseTest2.setTestLogging();

        if ( cmdStrictMode ) {
            // Which will apply to reading the manifests!
            ARQ.setStrictMode();
            SysRIOT.setStrictMode(true);
        }

        if ( arqAsNormal )
            SparqlTests.defaultSyntaxForSyntaxTests = Syntax.syntaxARQ;
        else
            SparqlTests.defaultSyntaxForSyntaxTests = Syntax.syntaxSPARQL_11;

        for ( String fn : getPositional() ) {
            System.out.println("Run: "+fn);
            exec1(report, fn);
        }

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

            // Write meta separately so it is easy to find.
            RDFDataMgr.write(earlOut, model, Lang.TURTLE);
            earlOut.println();
            RDFDataMgr.write(earlOut, meta, Lang.TURTLE);
        }
    }

    protected void exec1(EarlReport report, String manifest) {
        if ( createEarlReport )
            oneManifestEarl(report, manifest);
        else
            oneManifest(manifest);
    }

    static void oneManifest(String testManifest) {
        TextTestRunner.runOne(testManifest, testMaker());
    }
    static void oneManifestEarl(EarlReport report, String testManifest) {
        TextTestRunner.runOne(report, testManifest, testMaker());
    }

    // Test subsystems.
    private static List<Function<ManifestEntry, Runnable>> installed = new ArrayList<>();
    static {
        installed.add(RiotTests::makeRIOTTest);
        installed.add(SparqlTests::makeSPARQLTest);
        // SHACL-WG manifests are structured different.
    }

    private static Function<ManifestEntry, Runnable> testMaker() {
        return (ManifestEntry entry) -> {
            for ( Function<ManifestEntry, Runnable> engine : installed) {
                Runnable r = engine.apply(entry);
                if ( r != null )
                    return r;
            }
            String testName = entry.getName();
            Resource testType = entry.getTestType() ;
            System.err.println("Unrecognized test : ("+testType+")" + testName) ;
            return new SurpressedTest(entry) ;
        };
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
