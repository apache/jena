/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */


package com.hp.hpl.jena.rdql.test;

import java.io.* ;
//import java.util.* ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.mem.* ;

import junit.framework.* ;

import com.hp.hpl.jena.util.* ;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.rdql.* ;

import com.hp.hpl.jena.vocabulary.* ;

import org.apache.log4j.Logger;

/** Test scripts for RDQL - loads, executes and checks (with JUnit) a collection of
 *  queries.  New tests added as new featues appera and bugs are reported by
 *  adding new script files.  This class need not change.
 *
 * @author   Andy Seaborne
 * @version  $Id: QueryTestScripts.java,v 1.12 2003-07-18 12:50:49 chris-dollin Exp $
 */


public class QueryTestScripts extends TestSuite
{
    static final String testSetName = "RDQL - Query - Scripts" ;

    static final public String defaultControlFilename = "rdql-tests.n3" ;
    static final public String defaultTestDirectory = "testing/RDQL" ;

    public String basename = null ;
    static public boolean printDetails = false ;
    static public boolean displayTime = false ;

    static protected Logger logger = Logger.getLogger( QueryTestScripts.class );

    // Instance variables.
    String controlFilename = null ;
    String testDirectory = null ;


    // Make runnable from JUnit
    public static TestSuite suite() { return suite(testSetName, null) ; }

    public static TestSuite suite(String name, Model m)
    {

        return suite(name, m, defaultTestDirectory, defaultControlFilename) ;
    }

    public static TestSuite suite(String name, Model m, String _testDirectory, String _controlFilename)
    {
        return new QueryTestScripts(name, m, _testDirectory, _controlFilename) ;
    }


    private QueryTestScripts(String name,
                             Model m,
                             String _testDirectory,
                             String _controlFilename)
    {
        super(name) ;
        testDirectory = _testDirectory ;
        controlFilename = _controlFilename ;

        String testsFile = (testDirectory==null)?controlFilename:(testDirectory+"/"+controlFilename) ;
        addTests(m, testsFile) ;

    }


    // Alternative invokation for command line use.
    // Assumes it is in the tests directory
    static public void doTests(String testsFilename, boolean _printOutput, boolean _displayTime)
    {
        displayTime = _displayTime ;
        printDetails = _printOutput || _displayTime ;
        init() ;

        TestSuite suite = suite("RDQL-Scripts", null, null, testsFilename) ;
        junit.textui.TestRunner.run(suite) ;
        /*
        // Fake the TestRunner : don't want all the dots.
        TestResult r = new TestResult() ;
        for ( Enumeration enum = suite.tests() ; enum.hasMoreElements() ; )
        {
            Test t = (Test)enum.nextElement() ;
            t.run(r) ;
        }
        */
    }


    public void addTests(Model model, String testsFile)
    {
        PrintWriter out = new PrintWriter(System.out);
        Model tests = ModelLoader.loadModel(testsFile, "N3");
        TestManifestList manifest = new TestManifestList(tests);

        TestManifestList.TestIterator iter = manifest.iterator();
        for (; iter.hasNext();)
        {
            TestManifestList.TestItem item = iter.nextItem();
            String queryFile = null;
            String dataFile = null;
            String resultsFile = null;

            if (item.getAction() instanceof Literal)
            {
                queryFile = ((Literal) item.getAction()).getString();
            }
            else if (!((Resource) item.getAction()).isAnon())
            {
                queryFile = ((Resource) item.getAction()).getURI();
            }
            else
            {
                // Anon node - more details
                Resource r = (Resource) item.getAction();
                queryFile = r.getRequiredProperty(TestQuery.query).getResource().getURI();
                if (r.hasProperty(TestQuery.data))
                    dataFile = r.getRequiredProperty(TestQuery.data).getResource().getURI();
            }

            if (item.getResult() != null)
            {
                if (item.getResult() instanceof Resource)
                    resultsFile = ((Resource) item.getResult()).getURI();
                else
                    resultsFile = ((Literal) item.getResult()).getString();
            }
            TestCase test =
                new RDQLTest(
                    model,
                    out,
                    item.getName(),
                    testDirectory,
                    queryFile,
                    dataFile,
                    resultsFile);
            addTest(test);
        }
    }

    static class RDQLTest extends TestCase
    {
        static int testCounter = 1 ;
        Model model ;
        PrintWriter pw ;
        int testNumber = testCounter++ ;
        String queryFile ;
        String dataFile ;
        String resultsFile ;
        String directory ;

        // If supplied with a model, the test will load that model with data from the source
        // If no model is supplied one is created or attached (e.g. a database)

        RDQLTest(PrintWriter _pw, String testName, String _directory,
                 String _queryFile, String _dataFile, String _resultsFile)
        {
            this(null, _pw, testName, _directory, _queryFile, _dataFile, _resultsFile) ;
        }

        RDQLTest(Model _model, PrintWriter _pw, String testName, String _directory,
                 String _queryFile, String _dataFile, String _resultsFile)
        {
            super(testName) ;
            model = _model ;
            pw = _pw ;
            queryFile = _queryFile ;
            dataFile = _dataFile ;
            resultsFile = _resultsFile ;
            directory = _directory ;
        }


        protected void runTest() throws Throwable
        {
            Query query = null ;
            try {
                if ( printDetails )
                {
                    if ( testNumber != 1 )
                    {
                        pw.println() ;
                        pw.println("------------------------------------------------------------------------") ;
                        pw.println() ;
                    }
                    pw.println("Test "+testNumber+" :: QueryFile="+queryFile+", DataFile="+dataFile+", ResultsFile="+resultsFile) ;
                }

                String qf = convertFilename(queryFile, directory) ;
                String queryString = FileUtils.readWholeFile(qf) ;
                if ( printDetails ) {
                    pw.println("Query:") ;
                    pw.println(queryString);
                    if ( ! queryString.endsWith("\n") )
                        pw.println() ;
                    pw.flush() ;
                }

                long startTime = System.currentTimeMillis();
                query = new Query(queryString) ;

                if ( printDetails ) {
                    pw.println("Parsed query:") ;
                    pw.println(query.toString()) ;
                    pw.flush() ;
                }

                if ( model == null )
                {
                    if ( dataFile != null && ! dataFile.equals("") ) {
                        long startLoadTime = System.currentTimeMillis();
                        String df = convertFilename(dataFile, directory) ;
                        query.setSource(ModelLoader.loadModel(df, null)) ;
                        query.loadTime = System.currentTimeMillis() - startLoadTime ;
                    }
                } else
                {
                    // Model supplied
                    emptyModel(model) ;
                    String data = convertFilename(dataFile, directory) ;
                    if ( data == null )
                        data = query.getSourceURL() ;

                    long startLoadTime = System.currentTimeMillis();
                    query.setSource(ModelLoader.loadModel(model, data, null)) ;
                    query.loadTime = System.currentTimeMillis() - startLoadTime ;
                }

                QueryExecution qe = new QueryEngine(query) ;
                ModelLoader.setFileBase(directory) ;
                qe.init() ;
                ModelLoader.setFileBase(null) ;
                // Do the query!
                QueryResults resultsActual = qe.exec() ;

                long finishTime = System.currentTimeMillis();
                long totalTime = finishTime-startTime ;

                // Turn into a resettable version
                QueryResultsRewindable results = new QueryResultsMem(resultsActual) ;
                resultsActual.close() ;
                resultsActual = null ;

                boolean testingResults = ( resultsFile != null && !resultsFile.equals("") ) ;

                if ( printDetails )
                {
                    QueryResultsFormatter fmt = new QueryResultsFormatter(results) ;
                    fmt.printAll(pw, " | ") ;
                    // Must be after the results have been processed
                    pw.println() ;
                    int n = fmt.numRows() ;
                    pw.println("Results: "+((n < 0)?"unknown (one pass format)":n+"")) ;
                    fmt.close() ;
                    results.rewind() ;
                }

                if ( printDetails && displayTime )
                {
                    pw.println() ;
                    pw.println("Query parse:     "+formatlong(query.parseTime)   +" ms") ;
                    pw.println("Query build:     "+formatlong(query.buildTime)   +" ms") ;
                    pw.println("Data load time:  "+formatlong(query.loadTime)    +" ms") ;
                    pw.println("Query execute:   "+formatlong(query.executeTime) +" ms") ;
                    pw.println("Query misc:      "+formatlong(totalTime-query.parseTime-query.buildTime-query.loadTime-query.executeTime)+" ms") ;
                    pw.println("Query total:     "+formatlong(totalTime)         +" ms") ;
                    pw.flush() ;
                }


                if ( testingResults )
                {
                    String rf = convertFilename(resultsFile,directory) ;
                    QueryResultsMem qr1 = new QueryResultsMem(results) ;
                    QueryResultsMem qr2 = new QueryResultsMem(rf) ;
                    if ( ! QueryResultsMem.equivalent(qr1, qr2) )
                    {
                        pw.println() ;
                        pw.println("=======================================") ;
                        pw.println("Failure: "+queryFile) ;
                        pw.println("Got: ----------------------------------") ;
                        qr1.rewind() ;
                        qr1.list(pw) ;
                        qr1.rewind() ;
                        pw.println("---------------------------------------") ;
                        qr1.toModel().write(pw, "N3") ;
                        pw.flush() ;
                        qr1.close() ;

                        pw.println("Expected: -----------------------------") ;
                        qr2.rewind() ;
                        qr2.list(pw) ;
                        qr2.rewind() ;
                        pw.println("---------------------------------------") ;
                        qr2.toModel().write(pw, "N3") ;
                        qr2.close() ;
                        pw.flush() ;
                        Assert.assertTrue("Rsults do not match: "+queryFile,false) ;
                    }
                    //else
                    //  System.err.println("Test: "+queryFile+" => "+resultsFile+" passed") ;
                    qr1.close() ;
                    qr2.close() ;
                }
                results.close() ;
            }
            catch (QueryException qEx) {
                pw.flush() ;
                Assert.fail(queryFile) ;
                // Test failure.
                throw qEx ;
            }
            catch (IOException ioEx){ pw.println("IOException: "+ioEx) ; ioEx.printStackTrace(pw) ; pw.flush() ; }
            //catch (JenaException rdfEx) { pw.println("JenaException: "+rdfEx) ; rdfEx.printStackTrace(pw) ; pw.flush() ; }
            catch (Exception ex)
            {
                pw.println("Exception: "+ex) ;
                ex.printStackTrace(pw) ;
                pw.flush() ;
                Assert.assertTrue("Exception: "+queryFile,false) ;
            }
            finally
            {
                if ( model == null && query.getSource() != null )
                    query.getSource().close() ;
                pw.flush() ;


            }
        }
    }

    private static void emptyModel(Model model)
    {
        if ( model == null )
            return ;
        try {
            StmtIterator sIter = model.listStatements();
            while (sIter.hasNext()) {
                sIter.nextStatement();
                sIter.remove();
            }
            sIter.close() ;
        } catch ( JenaException rdfEx)
        { logger.error( "Failed to empty model (com.hp.hpl.jena.rdf.query.Test.QueryTest.emptyModel)", rdfEx) ; }
    }

    static String convertFilename(String filename, String directory)
    {
        if ( filename == null )
            return null;
        if ( filename.startsWith("file:"))
            filename = filename.substring("file:".length()) ;
        if ( directory != null && ! filename.startsWith("/"))
            filename = directory+"/"+filename ;
        return filename ;
    }

    // This method executes a trivial query in order to force most classes to be loaded.
    // Not perfect (e.g. does not flush model data into kernel buffers) but better than nothing.
    private static void init()
    {
        String queryString = "SELECT * WHERE (?x, ?y, ?z)" ;
        Query query = new Query(queryString) ;
        query.setSource(new ModelMem());
        QueryExecution qe = new QueryEngine(query) ;
        QueryResults qr = qe.exec() ;
        QueryResultsFormatter fmt = new QueryResultsFormatter(qr) ;
        fmt.consume();
    }

    // Copied from rdfquery.  Share it!
    static String formatlong(long x) {
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append(Long.toString(x)) ;
        for ( int i = sbuff.length() ; i < 4 ; i++ ) sbuff.append(" ") ;
        return sbuff.toString() ;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003, 2001-2003
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
