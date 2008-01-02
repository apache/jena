/*
 * (c) Copyright 2006, 2007 Hewlett--Packard Development Company, LP
 * [See end of file]
 */

package sdb;

import java.util.List;

import junit.framework.TestSuite;
import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.test.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.QueryTestSuiteFactory;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
 
 /** Run a test suite
  * 
  *  <p>
  *  Usage:<pre>
  *  jena.sdbtest [db spec] [ manifest ]
  *  </pre>
  *  </p>
  * 
  * @author Andy Seaborne
  */ 
 
public class sdbtest extends CmdArgsDB
{
    public static final String usage = "sdbtest --sdb <SPEC> [--earl] [--direct] [manifest]" ;
    static ArgDecl earlDecl = new ArgDecl(ArgDecl.NoValue, "earl") ;
    boolean earlReport = false ;
    
    public static void main (String... argv)
    {
        new sdbtest(argv).main() ;
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
        ts.addTest(QueryTestSDBFactory.make(getStore(), manifest, null)) ;
        SimpleTestRunner.runAndReport(ts) ;
    }
    
    static void execOneManifestEarl(String testManifest)
    {
        // Include information later.
        EarlReport report = new EarlReport("SDB", SDB.VERSION, "http://jena.sf.net/SDB") ;
        QueryTestSuiteFactory.results = report ;
        
        Model model = report.getModel() ;

        // Update the EARL report. 
        Resource jena = model.createResource()
                    .addProperty(FOAF.homepage, model.createResource("http://jena.sf.net/")) ;
        
        // SDB is part of Jena.
        Resource arq = report.getSystem()
                        .addProperty(DCTerms.isPartOf, jena) ;
        
        // Andy wrote the test software (updates the thing being tested as well as they are the same). 
        Resource who = report.getModel().createResource(FOAF.Person)
                                .addProperty(FOAF.name, "Andy Seaborne")
                                .addProperty(FOAF.homepage, 
                                             model.createResource("http://www.hpl.hp.com/people/afs")) ; 
        Resource reporter = report.getReporter() ;
        reporter.addProperty(DC.creator, who) ;
        
        TestSuite suite = QueryTestSuiteFactory.make(testManifest) ;
        SimpleTestRunner.runSilent(suite) ;
        
        QueryTestSuiteFactory.results.getModel().write(System.out, "TTL") ;
        
    }
}
 


/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
