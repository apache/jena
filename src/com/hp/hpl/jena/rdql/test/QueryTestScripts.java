/*
 * (c) Copyright 2001, 2002, 2003, 2004 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: QueryTestScripts.java,v 1.21 2005-02-21 12:16:07 andy_seaborne Exp $
 */


package com.hp.hpl.jena.rdql.test;

import java.io.* ;
//import java.util.* ;

import com.hp.hpl.jena.rdql.* ;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Test scripts for RDQL - loads, executes and checks (with JUnit) a collection of
 *  queries.  New tests added as new featues appera and bugs are reported by
 *  adding new script files.  This class need not change.
 *
 * @author   Andy Seaborne
 * @version  $Id: QueryTestScripts.java,v 1.21 2005-02-21 12:16:07 andy_seaborne Exp $
 */


public class QueryTestScripts extends TestSuite
{
    static final String testSetName = "RDQL - Query - Scripts" ;
    static final String directory = "testing/RDQL/" ;
    FileManager fm = null ;
    
    public String basename = null ;
    static public boolean printDetails = false ;
    static public boolean displayTime = false ;

    static protected Log log = LogFactory.getLog( QueryTestScripts.class );

    static PrintWriter out = FileUtils.asPrintWriterUTF8(System.out) ;

    public static TestSuite suite()
    {
        return new QueryTestScripts(testSetName) ;
    }

    
    // Use to explicitly create tests.
    public QueryTestScripts(String name)
    {
        super(name) ;
        FileManager fm = new FileManager() ;
        fm.addLocatorFile(directory) ;
    }


    static int testCounter = 0 ;
    
    public void addTest(TestItem item)
    {
        testCounter++ ;
        addTest("BRQL-test-"+testCounter, item) ; 
    }

    public void addTest(String testName, TestItem item)
    {
        addTest(testName, null, item) ; 
    }

    
    public void addTest(String testName, String directory,
                        TestItem item)
    {
        addTest(null, testName, directory, item) ;
    }

    public void addTest(Model model, String testName, String directory,
                        TestItem item)
    {
        TestCase test = new RDQLTest(model, testName, fm, item) ;
        addTest(test) ;
    }
    
    // One test.  State and execution.
    
    private static class RDQLTest extends TestCase
    {
        static int testCounter = 1 ;
        static boolean printModelsOnFailure = false ;
        
        Model model ;
        int testNumber = testCounter++ ;
        TestItem testItem ;
        FileManager fileManager ;

        // If supplied with a model, the test will load that model with data from the source
        // If no model is supplied one is created or attached (e.g. a database)

        RDQLTest(Model m, String testName, FileManager fm, TestItem t)
        {
            super(testName) ;
            model = m ;
            fileManager = fm ;
            testItem = t ;
        }
        
        protected void runTest() throws Throwable
        {
            Query query = null ;
            try {
                if ( printDetails )
                {
                    if ( testNumber != 1 )
                    {
                        out.println() ;
                        out.println("------------------------------------------------------------------------") ;
                        out.println() ;
                    }
                    out.println("Test "+testNumber+" :: QueryFile="+testItem.getQueryFile()+", DataFile="+testItem.getDataFile()+", ResultsFile="+testItem.getResultFile()) ;
                }

                String queryString = fileManager.readWholeFileAsUTF8(testItem.getQueryFile()) ; 

                if ( printDetails ) {
                    out.println("Query:") ;
                    out.println(queryString);
                    if ( ! queryString.endsWith("\n") )
                        out.println() ;
                    out.flush() ;
                }

                long startTime = System.currentTimeMillis();
                
                try {
                    query = new Query(queryString) ;
                }
                catch (QueryException qEx)
                {
                    query = null ;
                    out.flush() ;
                    assertFalse("Parse failure: "+qEx.getMessage(), true) ; 
                    // Test failure.
                    throw qEx ;
                }
                
                if ( printDetails ) {
                    out.println("Parsed query:") ;
                    out.println(query.toString()) ;
                    out.flush() ;
                }

                if ( model == null )
                {
                    if ( testItem.getDataFile() != null && ! testItem.getDataFile().equals("") ) {
                        long startLoadTime = System.currentTimeMillis();
                        Model m = fileManager.loadModel(testItem.getDataFile()) ;
                        query.setSource(m) ;
                        query.loadTime = System.currentTimeMillis() - startLoadTime ;
                    }
                } else
                {
                    // Model supplied : ensure empty then load it.
                    emptyModel(model) ;
                    
                    String data = null ;
                    if ( testItem.getDataFile() != null && ! testItem.getDataFile().equals("") )
                        data = testItem.getDataFile() ;
                    if ( data == null )
                        data = query.getSourceURL() ;

                    try {
                        long startLoadTime = System.currentTimeMillis();
                        query.setSource(fileManager.readModel(model, data)) ;
                        query.loadTime = System.currentTimeMillis() - startLoadTime ;
                    } catch (JenaException ex)
                    {
                        log.warn("Problems loading data for: "+data) ;
                    }
                }

                QueryExecution qe = new QueryEngine(query) ;
                qe.init() ;
                runTestSelect(query, qe, startTime) ;
            }
            catch (IOException ioEx){ out.println("IOException: "+ioEx) ; ioEx.printStackTrace(out) ; out.flush() ; }
            //catch (JenaException rdfEx) { out.println("JenaException: "+rdfEx) ; rdfEx.printStackTrace(out) ; out.flush() ; }
            catch (Exception ex)
            {
                out.println("Exception: "+ex) ;
                ex.printStackTrace(out) ;
                out.flush() ;
                Assert.assertTrue("Exception: "+testItem.getQueryFile(),false) ;
            }
            finally
            {
                if ( model == null && query != null && query.getSource() != null )
                    query.getSource().close() ;
                out.flush() ;
            }
        }
        
        void runTestSelect(Query query, QueryExecution qe, long startTime) throws Exception
        {
            // Do the query!
            QueryResults resultsActual = qe.exec() ;
            
            long finishTime = System.currentTimeMillis();
            long totalTime = finishTime-startTime ;
            
            // Turn into a resettable version
            QueryResultsRewindable results = new QueryResultsMem(resultsActual) ;
            resultsActual.close() ;
            resultsActual = null ;
            
            boolean testingResults = ( testItem.getResultFile() != null && !testItem.getResultFile().equals("") ) ;
            
            if ( printDetails )
            {
                QueryResultsFormatter fmt = new QueryResultsFormatter(results) ;
                fmt.printAll(out) ;
                // Must be after the results have been processed
                out.println() ;
                fmt.close() ;
            }
            
            if ( printDetails && displayTime )
            {
                out.println() ;
                out.println("Query parse:     "+formatlong(query.parseTime)   +" ms") ;
                out.println("Query build:     "+formatlong(query.buildTime)   +" ms") ;
                out.println("Data load time:  "+formatlong(query.loadTime)    +" ms") ;
                out.println("Query execute:   "+formatlong(query.executeTime) +" ms") ;
                out.println("Query misc:      "+formatlong(totalTime-query.parseTime-query.buildTime-query.loadTime-query.executeTime)+" ms") ;
                out.println("Query total:     "+formatlong(totalTime)         +" ms") ;
                out.flush() ;
            }
            
            
            if ( testingResults )
            {
                try {
                    Model tmp = fileManager.loadModel(testItem.getResultFile()) ;
                    
                    QueryResultsMem qr1 = new QueryResultsMem(results) ;
                    QueryResultsMem qr2 = new QueryResultsMem(tmp) ;

                    if ( !resultSetEquivalent(qr1, qr2) ) 
                    {
                        out.println() ;
                        out.println("=======================================") ;
                        out.println("Failure: "+testItem.getQueryFile()) ;
                        out.println("Got: "+qr1.size()+" ----------------------------------") ;
                        qr1.reset() ;
                        
                        QueryResultsFormatter qrFmt1 = new QueryResultsFormatter(qr1) ;
                        qrFmt1.dump(out, false) ;
                        qr1.reset() ;
                        
                        if ( printModelsOnFailure )
                        {
                            out.println("---------------------------------------") ;
                            qrFmt1.toModel().write(out, "N3") ;
                            qr1.reset() ;
                        }
                        out.flush() ;
                        
                        QueryResultsFormatter qrFmt2 = new QueryResultsFormatter(qr2) ;
                        
                        out.println("Expected: "+qr2.size()+" -----------------------------") ;
                        qr2.reset() ;
                        qrFmt2.dump(out, false) ;
                        qr2.reset() ;
                        
                        if ( printModelsOnFailure )
                        {
                            out.println("---------------------------------------") ;
                            qrFmt2.toModel().write(out, "N3") ;
                            qr2.reset() ;
                        }
                        out.println() ;
                        out.flush() ;
    
                        qrFmt1.close() ;
                        qrFmt2.close() ;
                        qr1.close() ;
                        qr2.close() ;
                                
                        Assert.assertTrue("Results do not match: "+testItem.getQueryFile(),false) ;
                    }
                    //else
                    //  passed
                } catch (Exception ex)
                {
                    log.warn("Exception in result testing", ex) ;
                    Assert.fail("Exception in result testing: "+ex) ;
                }
            }
            results.close() ;
        }
        
        /** Are two result sets the same (isomorphic)?
        *
        * @param irs1
        * @param irs2
        * @return boolean
        */

       static public boolean resultSetEquivalent(
           QueryResults rs1,
           QueryResults rs2)
       {
           QueryResultsFormatter fmt1 = new QueryResultsFormatter(rs1) ;
           Model model1 = fmt1.toModel() ;

           QueryResultsFormatter fmt2 = new QueryResultsFormatter(rs2) ;
           Model model2 = fmt2.toModel() ;

           return model1.isIsomorphicWith(model2) ;
       }


        
        void runTestConstruct(Query query, QueryExecution qe, long startTime) throws Exception
        {
        }
        
        void runTestDescribe(Query query, QueryExecution qe, long startTime) throws Exception
        {
        }
        
        void runTestAsk(Query query, QueryExecution qe, long startTime) throws Exception
        {
        }
        

    }

    public static void emptyModel(Model model)
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
        { log.error( "Failed to empty model (com.hp.hpl.jena.rdf.query.Test.QueryTest.emptyModel)", rdfEx) ; }
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

    // Copied from rdfquery.  Share it!
    static String formatlong(long x) {
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append(Long.toString(x)) ;
        for ( int i = sbuff.length() ; i < 4 ; i++ ) sbuff.append(" ") ;
        return sbuff.toString() ;
    }

}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004 2004, 2005 Hewlett-Packard Development Company, LP
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
