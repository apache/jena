/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */


package arq;

import java.io.File;
import java.util.Iterator;

import junit.framework.TestSuite;
import arq.cmd.CmdException;
import arq.cmd.CmdUtils;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdLineArgs;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineQuad;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.QueryTestSuiteFactory;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner;
import com.hp.hpl.jena.sparql.test.ARQTestSuite;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;


/** A program to execute query test suites
 * 
 * <pre>
 * Usage: 
 *   [--all]
 *   [--dawg]
 *   <i>testManifest</i>
 *   [ --query <i>query</i> --data <i>data</i> --result <i>result</i> ] -- run one test
 * </pre>
 * 
 * @author  Andy Seaborne
 */

public class qtest
{
    // TODO Convert to extends CmdArgModule 

    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    
    static String className = Utils.classShortName(qtest.class) ;
    
    static String usage = 
        "One of:\n"+
        "  "+className+" [--all]\n"+
        "  "+className+" [--dawg]\n"+
        "  "+className+" manifest\n"+
        "  "+className+" --query queryFile --data dataFile --result resultsFile\n"+
                "     where --all      run all built-in tests\n"+
                "           --dawg     run working group tests\n"+
                "           manifest   run a set of tests";
    
    public static void main (String [] argv)
    {
        try {
            main2(argv) ;
        }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
        catch (CmdException ex)
        {
            System.err.println(ex.getMessage()) ;
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
        }
        
    }
        
    public static void main2(String [] argv)
    {
        CmdLineArgs cl = new CmdLineArgs(argv) ;
        
        ArgDecl helpDecl = new ArgDecl(ArgDecl.NoValue, "h", "help") ;
        cl.add(helpDecl) ;
        
//        ArgDecl verboseDecl = new ArgDecl(false, "v", "verbose") ;
//        cl.add(verboseDecl) ;
//        
//        ArgDecl quietDecl = new ArgDecl(false, "q", "quiet") ;
//        cl.add(quietDecl) ;
        
        ArgDecl allDecl = new ArgDecl(ArgDecl.NoValue, "all") ;
        cl.add(allDecl) ;

        ArgDecl wgDecl = new ArgDecl(ArgDecl.NoValue, "wg", "dawg") ;
        cl.add(wgDecl) ;
        
        ArgDecl dirDecl = new ArgDecl(ArgDecl.HasValue, "dir") ;
        cl.add(dirDecl) ;

        ArgDecl queryDecl = new ArgDecl(ArgDecl.HasValue, "query") ;
        cl.add(queryDecl) ;

        ArgDecl dataDecl = new ArgDecl(ArgDecl.HasValue, "data") ;
        cl.add(dataDecl) ;

        ArgDecl resultDecl = new ArgDecl(ArgDecl.HasValue, "result") ;
        cl.add(resultDecl) ;
        
        ArgDecl engineDecl = new ArgDecl(ArgDecl.HasValue, "engine") ;
        cl.add(engineDecl) ;
        
        ArgDecl setDecl = new ArgDecl(ArgDecl.HasValue, "set", "define", "defn", "def") ;
        cl.add(setDecl) ;

        ArgDecl earlDecl = new ArgDecl(ArgDecl.NoValue, "earl") ;
        cl.add(earlDecl) ;
        
        ArgDecl strictDecl = new ArgDecl(ArgDecl.NoValue, "strict") ;
        cl.add(strictDecl) ;

        try {
            cl.process() ;
        } catch (IllegalArgumentException ex)
        {
            System.err.println(ex.getMessage()) ;
            usage(System.err) ;
            throw new TerminationException(2) ;
        }

        if ( cl.contains(helpDecl) ||
             ( !cl.hasArgs() && ! cl.hasPositional() ) )
        {
            usage() ;
            throw new TerminationException(0) ;
        }

        // ==== General things
        //boolean verbose = cl.contains(verboseDecl) ;
        //boolean quiet = cl.contains(quietDecl) ;
        
        // Strict mode
        if ( cl.contains(strictDecl) )
            ARQ.setStrictMode() ;
        
        // Set symbols
        if ( cl.getValues(setDecl) != null )
            for ( Iterator iter = cl.getValues(setDecl).iterator() ; iter.hasNext(); )
            {
                String arg = (String)iter.next();
                String[] frags = arg.split("=", 2) ;
                if ( frags.length != 2)
                    throw new RuntimeException("Can't split '"+arg+"'") ;

                String symbolName = frags[0] ;
                String value = frags[1] ;

                if ( ! symbolName.matches("^[a-zA-Z]*:") )
                    symbolName = "http://jena.hpl.hp.com/ARQ#" + symbolName ;

                Symbol symbol = Symbol.create(symbolName) ;
                ARQ.getContext().set(symbol, value) ;
            }
                
        if ( cl.contains(engineDecl) )
        {
            if ( cl.getValue(engineDecl).equalsIgnoreCase("engine2") ||
                 cl.getValue(engineDecl).equalsIgnoreCase("ref") )
            {
                QueryEngineRef.register() ;
            }
            else if ( cl.getValue(engineDecl).equalsIgnoreCase("quad") )
                QueryEngineQuad.register() ;
            else
            {
                argError("No such engine ("+cl.getValue(engineDecl)+")") ;
            }
        }
        
        // First - what are we doing?
        
        if ( cl.contains(queryDecl) || cl.contains(dataDecl) || cl.contains(resultDecl) )
        {
            if ( ! ( cl.contains(queryDecl) && cl.contains(dataDecl) && cl.contains(resultDecl) ) )
                argError("Must give query, data and result to run a single test") ;
            
            String query = cl.getValue(queryDecl) ;
            String data = cl.getValue(dataDecl) ;
            String result = cl.getValue(resultDecl) ;
            
            TestSuite suite = QueryTestSuiteFactory.make(query, data, result) ;

            //junit.textui.TestRunner.run(suite) ;
            SimpleTestRunner.runAndReport(suite) ;
            return ;
        }
        
        if ( cl.contains(allDecl) )
        {
            allTests() ; 
            throw new TerminationException(0) ;
        }
        
        if ( cl.contains(wgDecl) )
        {
            dawgTests() ;
            throw new TerminationException(0) ;
        }
        
        // OK - running a manifest
        
        if ( ! cl.hasPositional() )
            argError("No manifest file") ;
        String testfile = cl.getPositionalArg(0) ;
        
        String testfileAbs = IRIResolver.resolveGlobal(testfile) ;
        
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
        
        if ( cl.contains(earlDecl) )
            oneManifestEarl(testfileAbs) ;
        else
            oneManifest(testfileAbs) ;
        throw new TerminationException(0) ;
    }
    
    static void oneManifest(String testManifest)
    {
        TestSuite suite = QueryTestSuiteFactory.make(testManifest) ;

        //junit.textui.TestRunner.run(suite) ;
        SimpleTestRunner.runAndReport(suite) ;
    }
    
    
    static void oneManifestEarl(String testManifest)
    {
        String name =  "ARQ" ;
        String releaseName =  "ARQ2" ;
        String version = "ARQ-2.2-dev" ; //ARQ.VERSION ;
        String homepage = "http://jena.sf.net/ARQ" ;
        
        // Include information later.
        EarlReport report = new EarlReport(name, version, homepage) ;
        QueryTestSuiteFactory.results = report ;
        
        Model model = report.getModel() ;
        model.setNsPrefix("dawg", TestManifest.getURI()) ;
        
        // Update the EARL report. 
        Resource jena = model.createResource()
                    .addProperty(FOAF.homepage, model.createResource("http://jena.sf.net/")) ;
        
        // ARQ is part fo Jena.
        Resource arq = report.getSystem()
                        .addProperty(DCTerms.isPartOf, jena) ;
        
        // Andy wrote the test software (updates the thing being tested as well as they are the same). 
        Resource who = model.createResource(FOAF.Person)
                                .addProperty(FOAF.name, "Andy Seaborne")
                                .addProperty(FOAF.homepage, 
                                             model.createResource("http://www.hpl.hp.com/people/afs")) ;
        
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
        
        Node today_node = NodeFactory.todayAsDate() ;
        Literal today = model.createTypedLiteral(today_node.getLiteralLexicalForm(), today_node.getLiteralDatatype()) ;
        release.addProperty(DOAP.created, today) ;
        release.addProperty(DOAP.name, releaseName) ;      // Again
        
        TestSuite suite = QueryTestSuiteFactory.make(testManifest) ;
        SimpleTestRunner.runSilent(suite) ;
        
        QueryTestSuiteFactory.results.getModel().write(System.out, "TTL") ;
        
    }
    
    static void allTests()
    {
        // This should load all the built in tests.
        // Check to see if expected directories are present or not.
        
        ensureDirExists("testing") ;
        ensureDirExists("testing/ARQ") ;
        ensureDirExists("testing/DAWG") ;
        ensureDirExists("testing/DAWG-Approved") ;
        
        TestSuite ts = ARQTestSuite.suite() ;
        junit.textui.TestRunner.run(ts) ;
        throw new TerminationException(0) ;
        //SimpleTestRunner.runAndReport(ts) ;
    }

    static void dawgTests()
    {
        System.err.println("DAWG tests not packaged up yet") ;
        throw new TerminationException(4) ;
    }

    
    static void usage() { usage(System.err) ; }
    
    static void usage(java.io.PrintStream out)
    {
        out.println(usage) ;
    }

    static void argError(String s)
    {
        System.err.println("Argument Error: "+s) ;
        //usage(System.err) ;
        throw new TerminationException(3) ;
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

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
