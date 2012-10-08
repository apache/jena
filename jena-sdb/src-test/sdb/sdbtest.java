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

package sdb;

import java.util.List;

import junit.framework.TestSuite;
import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.test.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
 
 /** Run a test suite
  * 
  *  <p>
  *  Usage:<pre>
  *  sdb.sdbtest [db spec] [ manifest ]
  *  </pre>
  *  </p>
  */ 
 
public class sdbtest extends CmdArgsDB
{
    public static final String usage = "sdbtest --sdb <SPEC> [--earl] [--direct] [manifest]" ;
    static ArgDecl earlDecl = new ArgDecl(ArgDecl.NoValue, "earl") ;
    boolean earlReport = false ;
    
    public static void main (String... argv)
    {
        new sdbtest(argv).mainRun() ;
    }
    
    String filename = null ;

    protected sdbtest(String... args)
    {
        super(args);
        add(earlDecl, "--earl", "Generate an EARL report (RDF)") ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return Utils.className(this)+" <SPEC> [--earl] [--direct] [manifest]" ; }
    
    @Override
    protected void processModulesAndArgs()
    { 
        if ( getPositional().size() == 0 )
            cmdError("No manifest to run") ;
        earlReport = contains(earlDecl) ; 
    }
    
    @Override 
    protected void execCmd(List<String> positionalArgs)
    {
        for ( String x : positionalArgs )
        {
            if ( earlReport )
                execOneManifestEarl(x) ;
            else
                execOneManifest(x) ;
        }
    }
    
    private void execOneManifest(String manifest)
    { 
        if ( isVerbose() )
        {
            //SchemaBase.printBlock = true ;
            //SchemaBase.printAbstractSQL = true ;
            System.out.println("Manifest: "+manifest) ;
        }
        
        TestSuite ts = new TestSuite() ;
        ts.addTest(QueryTestSDBFactory.make(super.getStoreDesc(), manifest, null)) ;
        
        if ( true )
            // PostgreSQL gets upset with comments in comments??
            ARQ.getContext().setFalse(SDB.annotateGeneratedSQL) ;
        
        SimpleTestRunner.runAndReport(ts) ;
    }
    
    static void execOneManifestEarl(String testManifest)
    {
        // Include information later.
        EarlReport report = new EarlReport("http://jena.apache.org/#sdb", "SDB", SDB.VERSION, "http://jena.apahe.org/") ;
        ScriptTestSuiteFactory.results = report ;
        
        Model model = report.getModel() ;

        // Update the EARL report. 
        Resource jena = model.createResource()
                    .addProperty(FOAF.homepage, model.createResource("http://jena.apahe.org/")) ;
        
        // SDB is part of Jena.
        Resource arq = report.getSystem()
                        .addProperty(DCTerms.isPartOf, jena) ;
        
        // Andy wrote the test software (updates the thing being tested as well as they are the same). 
        Resource who = model.createResource(FOAF.Person)
            .addProperty(FOAF.name, "Andy Seaborne")
            .addProperty(FOAF.homepage, 
                         model.createResource("http://people.apache.org/~andy")) ; 
        Resource reporter = report.getReporter() ;
        reporter.addProperty(DC.creator, who) ;
        
        TestSuite suite = ScriptTestSuiteFactory.make(testManifest) ;
        SimpleTestRunner.runSilent(suite) ;
        
        ScriptTestSuiteFactory.results.getModel().write(System.out, "TTL") ;
        
    }
}
