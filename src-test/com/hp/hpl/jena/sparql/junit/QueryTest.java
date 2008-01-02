/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;

import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.sparql.util.GraphUtils;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;

public class QueryTest extends EarlTestCase
{
    private static int testCounter = 1 ;
    private static boolean printModelsOnFailure = false ;
    // -- Items from construction
    int testNumber = testCounter++ ;
    TestItem testItem ;
    FileManager queryFileManager ;
    boolean isRDQLtest = false ;
    boolean resetNeeded = false ;
    
    Model resultsModel = null ;     // Maybe null if no testing of results
    
    // If supplied with a model, the test will load that model with data from the source
    // If no model is supplied one is created or attached (e.g. a database)

    public QueryTest(String testName, EarlReport earl, FileManager fm, TestItem t)
    {
        super(fixName(testName), t.getURI(), earl) ;
        queryFileManager = fm ;
        testItem = t ;
        isRDQLtest = (testItem.getQueryFileSyntax().equals(Syntax.syntaxRDQL)) ;
    }
    
    private static String fixName(String s)
    {
        s = s.replace('(','[') ;
        s = s.replace(')',']') ;
        return s ;
    }
    
    protected void setUp() throws Exception
    {
        super.setUp() ;
        // SPARQL and ARQ tests are done with no value matching (for query execution and results testing)
        if ( ! isRDQLtest )
        {
            resetNeeded = true ;
            ARQ.setTrue(ARQ.strictGraph) ;
        }
        // Sort out data.
        // Not here - done during test execution because it needs to look in the query for source URIs
        
        resultsModel = testItem.getResultModel() ;
    }
    
    protected void tearDown() throws Exception
    {
        if ( resetNeeded )
            ARQ.setFalse(ARQ.strictGraph) ;
        super.tearDown() ;
    }
    
     private Dataset setUpDataset(Query query, TestItem testItem)
    {
        try {
            //testItem.requiresTextIndex()
            
            if ( doesQueryHaveDataset(query) && doesTestItemHaveDataset(testItem) )
            {
                // Only warn if there are results to test
                // Syntax tests may have FROM etc and a manifest data file. 
                if ( testItem.getResultFile() != null )
                    ALog.warn(this, testItem.getName()+" : query data source and also in test file") ; 
            }
            
            // In test file?
            if ( doesTestItemHaveDataset(testItem) )
                // Not specified in the query - get from test item and load
                return createDataset(testItem.getDefaultGraphURIs(), testItem.getNamedGraphURIs()) ;
      
          // Check 3 - were there any at all?
          
          if ( ! doesQueryHaveDataset(query) ) 
              fail("No dataset") ;
      
          // Left to query
          return null ;
      
      } catch (JenaException jEx)
      {
          fail("JenaException creating data source: "+jEx.getMessage()) ;
          return null ;
      }
    }
    
    private static boolean doesTestItemHaveDataset(TestItem testItem)
    {
        boolean r = 
            ( testItem.getDefaultGraphURIs() != null &&  testItem.getDefaultGraphURIs().size() > 0 )
            ||
            ( testItem.getNamedGraphURIs() != null &&  testItem.getNamedGraphURIs().size() > 0 ) ;
        return r ;
    }
    
    private static boolean doesQueryHaveDataset(Query query)
    {
        boolean r = 
            ( query.getGraphURIs() != null && query.getGraphURIs().size() > 0 )
            || 
            ( query.getNamedGraphURIs() != null && query.getNamedGraphURIs().size() > 0 ) ;
        return r ;
    }
    
    private static Dataset createDataset(List defaultGraphURIs, List namedGraphURIs)
    {
        return DatasetUtils.createDataset(defaultGraphURIs, namedGraphURIs, null, null) ;
    }
    
    protected void runTestForReal() throws Throwable
    {
        Query query = null ;
        try {
            try { query = queryFromTestItem(testItem) ; }
            catch (QueryException qEx)
            {
                query = null ;
                fail("Parse failure: "+qEx.getMessage()) ;
                throw qEx ;
            }

            Dataset dataset = setUpDataset(query, testItem) ;
            if ( dataset == null && ! doesQueryHaveDataset(query) ) 
                fail("No dataset for query") ;

            QueryExecution qe = null ;
            
            if ( dataset == null )
                qe = QueryExecutionFactory.create(query, queryFileManager) ;
            else
                qe = QueryExecutionFactory.create(query, dataset) ;
            
            try {
                if ( query.isSelectType() )
                    runTestSelect(query, qe) ;
                else if ( query.isConstructType() )
                    runTestConstruct(query, qe) ;
                else if ( query.isDescribeType() )
                    runTestDescribe(query, qe) ;
                else if ( query.isAskType() )
                    runTestAsk(query, qe) ;
            } finally { qe.close() ; }
        }
        catch (IOException ioEx)
        {
            //log.debug("IOException: ",ioEx) ;
            fail("IOException: "+ioEx.getMessage()) ;
            throw ioEx ;
        }
        catch (NullPointerException ex) { throw ex ; }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            fail( "Exception: "+ex.getClass().getName()+": "+ex.getMessage()) ;
        }
    }
    
    void runTestSelect(Query query, QueryExecution qe) throws Exception
    {
        // Do the query!
        ResultSet resultsActual = qe.execSelect() ;
        
        // Turn into a resettable version
        ResultSetRewindable results = ResultSetFactory.makeRewindable(resultsActual) ;
        qe.close() ;
        if ( ! query.isReduced() )
            checkResults(query, results, resultsModel) ;
        else
        {
            // Unfortunately, we turned the result set into a model. 
            // Turn into a ResultSet-uniqueify-turn back into a model.
            // Excessive copying.  Only for small results in teh DAWG test suite.
            ResultSetRewindable x = ResultSetFactory.makeRewindable(resultsModel) ;
            x = unique(x) ;
            results = unique(results) ;
            checkResults(query, results, ResultSetFormatter.toModel(x)) ;
        }
    }
    
    private static ResultSetRewindable unique(ResultSetRewindable results)
    {
        // VERY crude.  Utilises the fact that bindings have value equality.
        List x = new ArrayList() ;
        Set seen = new HashSet() ;
        for ( ; results.hasNext() ; )
        {
            Binding b = results.nextBinding() ;
            if ( seen.contains(b) )
                continue ;
            seen.add(b) ;
            x.add(b) ;
        }
        QueryIterator qIter = new QueryIterPlainWrapper(x.iterator()) ;
        ResultSet rs = new ResultSetStream(results.getResultVars(), ModelFactory.createDefaultModel(), qIter) ;
        return ResultSetFactory.makeRewindable(rs) ;
    }

    private void checkResults(Query query, ResultSetRewindable results, Model resultsModel)
    {
        if ( resultsModel == null )
            return ;
        try {
            ResultSetRewindable qr1 = ResultSetFactory.makeRewindable(results) ;
            ResultSetRewindable qr2 = ResultSetFactory.makeRewindable(resultsModel) ;
            boolean b = resultSetEquivalent(query, qr1, qr2)  ; 
            if ( ! b)
                printFailedResultSetTest(query, qr1, qr2) ;
            assertTrue("Results do not match: "+testItem.getName(), b) ;
        } catch (Exception ex)
        {
            fail("Exception in result testing: "+ex) ;
        }
    }
    
    private static Model resultSetToModel(ResultSet rs)
    {
        Model m = GraphUtils.makeDefaultModel() ;
        ResultSetFormatter.asRDF(m, rs) ;
        if ( m.getNsPrefixURI("rs") == null )
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI() ) ;
        if ( m.getNsPrefixURI("rdf") == null )
            m.setNsPrefix("rdf", RDF.getURI() ) ;
        if ( m.getNsPrefixURI("xsd") == null )
            m.setNsPrefix("xsd", XSDDatatype.XSD+"#") ;
        return m ;
        
    }
    
    /** Are two result sets the same (isomorphic)?
    *
    * @param rs1
    * @param rs2
    * @return boolean
    */

   static public boolean resultSetEquivalent(Query query,
       ResultSet rs1, ResultSet rs2)
   {
       Model model2 = resultSetToModel(rs2) ;
       return resultSetEquivalent(query, rs1, model2) ;
   }

   static public boolean resultSetEquivalent(Query query,
                                             ResultSet rs1,
                                             Model model2)
   {
       Model model1 = resultSetToModel(rs1) ;
       return model1.isIsomorphicWith(model2) ;
   }
   

   void runTestConstruct(Query query, QueryExecution qe) throws Exception
    {
        // Do the query!
        Model resultsActual = qe.execConstruct() ;
        
        if ( resultsModel != null )
        {
            try {
                if ( ! resultsModel.isIsomorphicWith(resultsActual) )
                {
                    printFailedModelTest(query, resultsActual, resultsModel) ;
                    fail("Results do not match: "+testItem.getName()) ;
                }
            } catch (Exception ex)
            {
                fail("Exception in result testing (construct): "+ex) ;
            }
        }
    }
    
    void runTestDescribe(Query query, QueryExecution qe) throws Exception
    {
        Model resultsActual = qe.execDescribe() ;
        
        if ( resultsModel != null )
        {
            try {
                if ( ! resultsModel.isIsomorphicWith(resultsActual) )
                {
                    printFailedModelTest(query, resultsActual, resultsModel) ;
                    fail("Results do not match: "+testItem.getName()) ;
                }
            } catch (Exception ex)
            {
                fail("Exception in result testing (describe): "+ex) ;
            }
        }
    }
    
    void runTestAsk(Query query, QueryExecution qe) throws Exception
    {
        boolean result = qe.execAsk() ;
        
        if ( resultsModel != null )
        {
            StmtIterator sIter = resultsModel.listStatements(null, RDF.type, ResultSetGraphVocab.ResultSet) ;
            if ( !sIter.hasNext() )
                throw new QueryTestException("Can't find the ASK result") ;
            Statement s = sIter.nextStatement() ;
            if ( sIter.hasNext() )
                throw new QueryTestException("Too many result sets in ASK result") ;
            Resource r = s.getSubject() ;
            Property p = resultsModel.createProperty(ResultSetGraphVocab.getURI()+"boolean") ;
            
            boolean x = r.getRequiredProperty(p).getBoolean() ;
            if ( x != result )
                assertEquals("ASK test results do not match", x,result);
        }
        
        return ;
    }
    
    void printFailedResultSetTest(Query query, ResultSetRewindable qr1,
                                   ResultSetRewindable qr2)
   {
       PrintStream out = System.out ;
       out.println() ;
       out.println("=======================================") ;
       out.println("Failure: "+description()) ;
       
       out.println("Got: "+qr1.size()+" --------------------------------") ;
       qr1.reset() ;
       ResultSetFormatter.out(out, qr1, query.getPrefixMapping()) ;
       qr1.reset() ;
       
       if ( printModelsOnFailure )
       {
           out.println("-----------------------------------------") ;
           resultSetToModel(qr1).write(out, "N3") ;
           qr1.reset() ;
       }
       out.flush() ;

       
       out.println("Expected: "+qr2.size()+" -----------------------------") ;
       qr2.reset() ;
       ResultSetFormatter.out(out, qr2, query.getPrefixMapping()) ;
       qr2.reset() ;
       
       if ( printModelsOnFailure )
       {
           out.println("---------------------------------------") ;
           resultSetToModel(qr2).write(out, "N3") ;
           qr2.reset() ;
       }
       out.println() ;
       out.flush() ;
   }

    void printFailedModelTest(Query query, Model results, Model expected)
    {
        PrintWriter out = FileUtils.asPrintWriterUTF8(System.out) ;
        out.println("=======================================") ;
        out.println("Failure: "+description()) ;
        if ( printModelsOnFailure )
        {
            results.write(out, "N3") ;
            out.println("---------------------------------------") ;
            expected.write(out, "N3") ;
        }
        out.println() ;
    }
    
    public String toString()
    { 
        if ( testItem.getName() != null )
            return testItem.getName() ;
        return super.getName() ;
    }

    // Cache
    String _description = null ;
    private String description()
    {
        if ( _description == null )
            _description = makeDescription() ;
        return _description ;
    }
    
    private String makeDescription()
    {
        String tmp = "" ;
        if ( testItem.getDefaultGraphURIs() != null )
        {
            for ( Iterator iter = testItem.getDefaultGraphURIs().iterator() ; iter.hasNext() ; )
                tmp = tmp+(String)iter.next() ;
        }
        if ( testItem.getNamedGraphURIs() != null )
        {
            for ( Iterator iter = testItem.getNamedGraphURIs().iterator() ; iter.hasNext() ; )
                tmp = tmp+(String)iter.next() ;
        }
        
        String d = "Test "+testNumber+" :: "+testItem.getName() ;
        //+" :: QueryFile="+testItem.getQueryFile()+
        //          ", DataFile="+tmp+", ResultsFile="+testItem.getResultFile() ;
        return d ;
    }
}
/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
