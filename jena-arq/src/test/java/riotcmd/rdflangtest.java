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

import junit.framework.TestSuite ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RIOT ;
import org.apache.jena.riot.langsuite.FactoryTestRiot ;
import org.apache.jena.riot.langsuite.VocabLangRDF ;
import arq.cmd.CmdException ;
import arq.cmd.TerminationException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdGeneral ;
import arq.cmdline.ModEngine ;
import arq.cmdline.ModSymbol ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.sparql.vocabulary.DOAP ;
import com.hp.hpl.jena.sparql.vocabulary.FOAF ;
import com.hp.hpl.jena.vocabulary.DC ;
import com.hp.hpl.jena.vocabulary.DCTerms ;
import com.hp.hpl.jena.vocabulary.RDF ;

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
    protected ModSymbol modSymbol       = new ModSymbol() ;
    protected ArgDecl  strictDecl       = new ArgDecl(ArgDecl.NoValue, "strict") ;
    protected boolean  cmdStrictMode    = false ; 

    //protected ArgDecl allDecl =    new ArgDecl(ArgDecl.NoValue, "all") ;
    protected ArgDecl earlDecl          = new ArgDecl(ArgDecl.NoValue, "earl") ;
    
    protected boolean createEarlReport = false;
    
    public static void main (String... argv)
    {
        RIOT.init() ;
        try { new rdflangtest(argv).mainRun() ; }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
    }
    
    public rdflangtest(String[] argv)
    {
        super(argv) ;
        addModule(modSymbol) ;
        super.add(strictDecl, "--strict", "Operate in strict mode (no extensions of any kind)") ;
        super.modVersion.addClass(ARQ.class) ;
        //add(allDecl, "--all", "run all tests") ;
        getUsage().startCategory("Tests (execute test manifest)") ;
        getUsage().addUsage("<manifest>", "run the tests specified in the given manifest") ;
        add(earlDecl, "--earl", "create EARL report") ;
    }
    
    protected ModEngine setModEngine()
    {
        return new ModEngine() ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" [ --data=<file> --query=<query> --result=<results> ] | --all | --dawg | <manifest>" ; }
    
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
        // Paradoxical naming - the boolean is a visibility flag.
        BaseTest.setTestLogging() ;
        
        if ( contains(strictDecl) )
        {
            cmdStrictMode = true ;
            // Which will apply to reading the manifest!
            ARQ.setStrictMode() ;
            RIOT.setStrictMode(true) ;
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
        TestSuite suite = FactoryTestRiot.make(testManifest, null, null) ;

        //junit.textui.TestRunner.run(suite) ;
        SimpleTestRunner.runAndReport(suite) ;
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
        FactoryTestRiot.report = report ;
        TestSuite suite = FactoryTestRiot.make(testManifest, null, null) ;
        SimpleTestRunner.runSilent(suite) ;

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
