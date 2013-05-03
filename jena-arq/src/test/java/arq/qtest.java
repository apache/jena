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

import java.io.File ;

import junit.framework.TestSuite ;
import arq.cmd.CmdException ;
import arq.cmd.TerminationException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModEngine ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.ARQTestSuite ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.sparql.vocabulary.DOAP ;
import com.hp.hpl.jena.sparql.vocabulary.FOAF ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest ;
import com.hp.hpl.jena.vocabulary.DC ;
import com.hp.hpl.jena.vocabulary.DCTerms ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.XSD ;


/** A program to execute query test suites
 * 
 * <pre>
 * Usage: 
 *   [--all]
 *   [--dawg]
 *   <i>testManifest</i>
 *   [ --query <i>query</i> --data <i>data</i> --result <i>result</i> ] -- run one test
 * </pre>
 */

public class qtest extends CmdARQ
{
    protected ArgDecl allDecl =    new ArgDecl(ArgDecl.NoValue, "all") ;
    protected ArgDecl wgDecl =     new ArgDecl(ArgDecl.NoValue, "wg", "dawg") ;
    protected ArgDecl queryDecl =  new ArgDecl(ArgDecl.HasValue, "query") ;
    protected ArgDecl dataDecl =   new ArgDecl(ArgDecl.HasValue, "data") ;
    protected ArgDecl resultDecl = new ArgDecl(ArgDecl.HasValue, "result") ;
    protected ArgDecl earlDecl =   new ArgDecl(ArgDecl.NoValue, "earl") ;
    
    protected ModEngine modEngine = null ;
    
    protected TestSuite suite = null;
    protected boolean execAllTests = false;
    protected boolean execDAWGTests = false;
    protected String testfile = null;
    protected boolean createEarlReport = false;
    
    public static void main (String... argv)
    {
        ARQ.init();
        try {
            new qtest(argv).mainRun() ;
        }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
    }
    
    public qtest(String[] argv)
    {
        super(argv) ;
        
        modEngine = setModEngine() ;
        addModule(modEngine) ;
        
        getUsage().startCategory("Tests (single query)") ;
        add(queryDecl, "--query", "run the given query") ;
        add(dataDecl, "--data", "data file to be queried") ;
        add(resultDecl, "--result", "file that specifies the expected result") ;
        
        getUsage().startCategory("Tests (built-in tests)") ;
        add(allDecl, "--all", "run all built-in tests") ;
        add(wgDecl, "--dawg", "run working group tests") ;
        
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
        super.processModulesAndArgs() ;
        
        if ( contains(queryDecl) || contains(dataDecl) || contains(resultDecl) )
        {
            if ( ! ( contains(queryDecl) && contains(dataDecl) && contains(resultDecl) ) )
                throw new CmdException("Must give query, data and result to run a single test") ;
            
            String query = getValue(queryDecl) ;
            String data = getValue(dataDecl) ;
            String result = getValue(resultDecl) ;
            
            suite = ScriptTestSuiteFactory.make(query, data, result) ;
        }
        else if ( contains(allDecl) )
        {
            execAllTests = true ;
        }
        else if ( contains(wgDecl) )
        {
            execDAWGTests = true ;
        }
        else
        {
            // OK - running a manifest
            
            if ( ! hasPositional() )
                throw new CmdException("No manifest file") ;

            testfile = getPositionalArg(0) ;
            createEarlReport = contains(earlDecl) ;
        }
    }
    
    @Override
    protected void exec()
    {
        if ( cmdStrictMode )
            ARQ.setStrictMode() ;
        
        if ( suite != null )
            SimpleTestRunner.runAndReport(suite) ;
        else if ( execAllTests )
            allTests() ;
        else if ( execDAWGTests )
            dawgTests() ;
        else
        {
            // running a manifest
            
            NodeValue.VerboseWarnings = false ;
            E_Function.WarnOnUnknownFunction = false ;
            
            if ( createEarlReport )
                oneManifestEarl(testfile) ;
            else
                oneManifest(testfile) ;
        }
    }
    
    static void oneManifest(String testManifest)
    {
        TestSuite suite = ScriptTestSuiteFactory.make(testManifest) ;

        //junit.textui.TestRunner.run(suite) ;
        SimpleTestRunner.runAndReport(suite) ;
    }
    
    static void oneManifestEarl(String testManifest)
    {
        String name =  "ARQ" ;
        String releaseName =  "ARQ" ;
        String version = "2.9.1" ;
        String homepage = "http://jena.apache.org/" ;
        String systemURI = "http://jena.apache.org/#arq" ;  // Null for bNode.
        
        // Include information later.
        EarlReport report = new EarlReport(systemURI, name, version, homepage) ;
        ScriptTestSuiteFactory.results = report ;
        
        Model model = report.getModel() ;
        model.setNsPrefix("dawg", TestManifest.getURI()) ;
        
        // Update the EARL report. 
        Resource jena = model.createResource()
                    .addProperty(FOAF.homepage, model.createResource("http://jena.apache.org/")) ;
        
        // ARQ is part fo Jena.
        Resource arq = report.getSystem()
                        .addProperty(DCTerms.isPartOf, jena) ;
        
        // Andy wrote the test software (updates the thing being tested as well as they are the same). 
        Resource who = model.createResource(FOAF.Person)
                                .addProperty(FOAF.name, "Andy Seaborne")
                                .addProperty(FOAF.homepage, 
                                             model.createResource("http://people.apache.org/~andy")) ;
        
        Resource reporter = report.getReporter() ;
        reporter.addProperty(DC.creator, who) ;

        model.setNsPrefix("doap", DOAP.getURI()) ; 
        model.setNsPrefix("xsd", XSD.getURI()) ;
        
        // DAWG specific stuff.
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
        
        TestSuite suite = ScriptTestSuiteFactory.make(testManifest) ;
        SimpleTestRunner.runSilent(suite) ;
        
        ScriptTestSuiteFactory.results.getModel().write(System.out, "TTL") ;
        
    }
    
    static void allTests()
    {
        // This should load all the built in tests.
        // Check to see if expected directories are present or not.
        
        ensureDirExists("testing") ;
        ensureDirExists("testing/ARQ") ;
        ensureDirExists("testing/DAWG") ;
        
        TestSuite ts = ARQTestSuite.suite() ;
        junit.textui.TestRunner.run(ts) ;
        //SimpleTestRunner.runAndReport(ts) ;
    }

    static void dawgTests()
    {
        System.err.println("DAWG tests not packaged up yet") ;
    }
    
    static void ensureDirExists(String dirname)
    {
        File f = new File(dirname) ;
        if ( ! f.exists() || !f.isDirectory() )
        {
            System.err.println("Can't find required directory: '"+dirname+"'") ;
            throw new TerminationException(8) ;
        }
    }
 }
