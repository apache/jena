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

import arq.cmdline.ModContext ;
import arq.cmdline.ModEngine ;
import jena.cmd.ArgDecl ;
import jena.cmd.CmdException ;
import jena.cmd.CmdGeneral ;
import jena.cmd.TerminationException ;
import org.apache.jena.arq.junit.TextTestRunner;
import org.apache.jena.arq.junit.riot.RiotTests;
import org.apache.jena.atlas.legacy.BaseTest2 ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.langsuite.VocabLangRDF ;
import org.apache.jena.sparql.expr.E_Function ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.junit.EarlReport ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.apache.jena.sparql.vocabulary.DOAP ;
import org.apache.jena.sparql.vocabulary.FOAF ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.vocabulary.DC ;
import org.apache.jena.vocabulary.DCTerms ;
import org.apache.jena.vocabulary.RDF ;

/** A program to execute RDF language test suites
 * 
 * <pre>
 * Usage: 
 *   [--all]
 *   <i>testManifest</i>
 *   [ --query <i>query</i> --data <i>data</i> --result <i>result</i> ] -- run one test
 * </pre>
 */

public class rdflangtest extends CmdGeneral
{
    static { JenaSystem.init() ; }
    protected ModContext modContext     = new ModContext() ;
    protected ArgDecl  strictDecl       = new ArgDecl(ArgDecl.NoValue, "strict") ;
    protected boolean  cmdStrictMode    = false ; 

    //protected ArgDecl allDecl =    new ArgDecl(ArgDecl.NoValue, "all") ;
    protected ArgDecl earlDecl          = new ArgDecl(ArgDecl.NoValue, "earl") ;
    
    protected boolean createEarlReport = false;
    
    public static void main (String... argv)
    {
        try { new rdflangtest(argv).mainRun() ; }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
    }
    
    public rdflangtest(String[] argv)
    {
        super(argv) ;
        super.add(strictDecl, "--strict", "Operate in strict mode (no extensions of any kind)") ;
        super.modVersion.addClass(ARQ.class) ;
        //add(allDecl, "--all", "run all tests") ;
        getUsage().startCategory("Tests (execute test manifest)") ;
        getUsage().addUsage("<manifest>", "run the tests specified in the given manifest") ;
        add(earlDecl, "--earl", "create EARL report") ;
        addModule(modContext) ;
    }
    
    protected ModEngine setModEngine()
    {
        return new ModEngine() ;
    }
    
    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" <manifest>" ; }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( ! hasPositional() )
            throw new CmdException("No manifest file") ;
        createEarlReport = contains(earlDecl) ;
    }
    
    @Override
    protected void exec()
    {
        BaseTest2.setTestLogging() ;
        
        if ( contains(strictDecl) ) {
            // Always done in test setups.
            cmdStrictMode = true ;
            // Which will apply to reading the manifest!
            ARQ.setStrictMode() ;
            SysRIOT.setStrictMode(true) ;
        }
        
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
        
        for ( String fn : getPositional() )
            exec1(fn) ;
    }
    
    protected void exec1(String manifest)
    {
        if ( createEarlReport )
            oneManifestEarl(manifest) ;
        else
            oneManifest(manifest) ;
    }

    static void oneManifest(String testManifest)
    {
        TextTestRunner.runOne(testManifest, RiotTests::makeRIOTTest);
    }
    
    static String name =  "Apache Jena RIOT" ;
    static String releaseName =  "RIOT" ;
    //static String version = RIOT.getVersion() ;  // Can be "development"
    static String version = null ;
    static String homepage = "http://jena.apache.org/" ;
    static String systemURI = "http://jena.apache.org/#riot" ;  // Null for bNode.

    static void oneManifestEarl(String testManifest)
    {
        EarlReport report = new EarlReport(systemURI, name, version, homepage) ;
        
        TextTestRunner.runOne(report, testManifest, RiotTests::makeRIOTTest);

        Model model = report.getModel() ;
        model.setNsPrefix("rdft", VocabLangRDF.getURI()) ;
        model.setNsPrefix("turtletest", "http://www.w3.org/2013/TurtleTests/manifest.ttl#") ;
        insertMetaOld(report) ;
        RDFDataMgr.write(System.out, model, Lang.TURTLE) ;
    }
    
    static void insertMeta(EarlReport report) {
        Model model = report.getModel() ;
        // We add the meta by hand separatly for better layout later 
    }
    
    //OLD meta.
    static void insertMetaOld(EarlReport report) {
        Model model = report.getModel() ;
        /*
        <> foaf:primaryTopic <http://jena.apache.org/#riot> ;
            dc:issued "..."^^xsd:dateTime;
            foaf:maker who.
        */
        
        // Update the EARL report. 
        Resource jena = model.createResource()
                    .addProperty(FOAF.homepage, model.createResource("http://jena.apache.org/")) ;
        
        // ARQ is part of Jena.
        Resource arq = report.getSystem()
                        .addProperty(DCTerms.isPartOf, jena) ;
        
        // Andy wrote the test software (updates the thing being tested as well as they are the same). 
        Resource who = model.createResource(FOAF.Person)
                                .addProperty(FOAF.name, "Andy Seaborne")
                                .addProperty(FOAF.homepage, 
                                             model.createResource("http://people.apache.org/~andy")) ;
        
        Resource reporter = report.getReporter() ;
        reporter.addProperty(DC.creator, who) ;

        Resource system = report.getSystem() ;
        system.addProperty(RDF.type, DOAP.Project) ;
        system.addProperty(DOAP.name, name) ;
        system.addProperty(DOAP.homepage, homepage) ;
        system.addProperty(DOAP.maintainer, who) ;
        
        Resource release = model.createResource(DOAP.Version) ;
        system.addProperty(DOAP.release, release) ;
        
        Node today_node = NodeFactoryExtra.todayAsDate() ;
        Literal today = model.createTypedLiteral(today_node.getLiteralLexicalForm(), today_node.getLiteralDatatype()) ;
        release.addProperty(DOAP.created, today) ;
        release.addProperty(DOAP.name, releaseName) ;      // Again
    }
 }
