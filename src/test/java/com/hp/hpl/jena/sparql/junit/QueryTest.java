/**
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

package com.hp.hpl.jena.sparql.junit;

import java.io.IOException ;
import java.io.PrintStream ;
import java.io.PrintWriter ;
import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.openjena.atlas.logging.Log ;
import org.openjena.riot.checker.CheckerLiterals ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable ;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.util.junit.TestUtils ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class QueryTest extends EarlTestCase
{
    private static int testCounter = 1 ;
    private int testNumber = testCounter++ ;
    private TestItem testItem ;
    private FileManager queryFileManager ;
    private boolean resetNeeded = false ;
    
    private SPARQLResult results = null ;    // Maybe null if no testing of results
    
    // If supplied with a model, the test will load that model with data from the source
    // If no model is supplied one is created or attached (e.g. a database)

    public QueryTest(String testName, EarlReport earl, FileManager fm, TestItem t)
    {
        super(TestUtils.safeName(testName), t.getURI(), earl) ;
        queryFileManager = fm ;
        testItem = t ;
    }
    private boolean oldWarningFlag  ;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp() ;
        // SPARQL and ARQ tests are done with no value matching (for query execution and results testing)
        resetNeeded = true ;
        ARQ.setTrue(ARQ.strictGraph) ;
        // Turn parser warnings off for the test data. 
        oldWarningFlag = CheckerLiterals.WarnOnBadLiterals ;
        CheckerLiterals.WarnOnBadLiterals = false ;

        // Sort out results.
        results =  testItem.getResults() ;
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if ( resetNeeded )
            ARQ.setFalse(ARQ.strictGraph) ;
        CheckerLiterals.WarnOnBadLiterals = oldWarningFlag ;
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
                    Log.warn(this, testItem.getName()+" : query data source and also in test file") ; 
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
        return query.hasDatasetDescription() ;
    }
    
    private static Dataset createDataset(List<String> defaultGraphURIs, List<String> namedGraphURIs)
    {
        return DatasetUtils.createDataset(defaultGraphURIs, namedGraphURIs, null, null) ;
    }
    
    @Override
    protected void runTestForReal() throws Throwable
    {
        Query query = null ;
        try {
            try { query = queryFromTestItem(testItem) ; }
            catch (QueryException qEx)
            {
                query = null ;
                qEx.printStackTrace(System.err) ;
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
        ResultSetRewindable resultsActual = ResultSetFactory.makeRewindable(qe.execSelect()) ;
        
        // Turn into a resettable version
        //ResultSetRewindable results = ResultSetFactory.makeRewindable(resultsActual) ;
        qe.close() ;
        
        if ( results == null )
            return ;
        
        if ( true )
        {
            // Assumes resultSetCompare can cope with full isomorphism possibilities.
            ResultSetRewindable resultsExpected ;
            if ( results.isResultSet() )
                resultsExpected = ResultSetFactory.makeRewindable(results.getResultSet()) ;
            else if ( results.isModel() )
                resultsExpected = ResultSetFactory.makeRewindable(results.getModel()) ;
            else
            {
                fail("Wrong result type for SELECT query") ;
                resultsExpected = null ; // Keep the compiler happy
            }
            
            if ( query.isReduced() )
            {
                // Reduced - best we can do is DISTINCT
                resultsExpected = unique(resultsExpected) ;
                resultsActual = unique(resultsActual) ;
            }
            boolean b = resultSetEquivalent(query, resultsExpected, resultsActual) ;
            if ( ! b )
            {
                resultsExpected.reset() ;
                resultsActual.reset() ; 
                boolean b2 = resultSetEquivalent(query, resultsExpected, resultsActual) ;
                printFailedResultSetTest(query, qe, resultsExpected, resultsActual) ;
            }
            assertTrue("Results do not match: "+testItem.getName(), b) ;
            
            return ;
        }
        
        // TEMPOPRARY
        // Later (when result set compare can cope with full bNode isomorphism)
        // always convert a result set model to a result set and test. 
        if ( results.isResultSet() )
        {
            ResultSetRewindable resultsExpected = ResultSetFactory.makeRewindable(results.getResultSet()) ;
            if ( query.isReduced() )
            {
                // Reduced - best we can do is DISTINCT
                resultsExpected = unique(resultsExpected) ;
                resultsActual = unique(resultsActual) ;
            }

            boolean b = resultSetEquivalent(query, resultsExpected, resultsActual) ;
            if ( ! b )
                printFailedResultSetTest(query, qe, resultsExpected, resultsActual) ;
            assertTrue("Results do not match: "+testItem.getName(), b) ;
        }
        else if ( results.isModel() )
        {
            // TEMPORARY
            // A lot of early tests were written using RDF-encoded result sets. 
            // Choosing to do model->result set is delayed.
            // The results might have been for CONSTRUCT/DESCRIBE
            
            // What's more the this can cope with isomorphism of bNodes. 
            
            Model resultsExpected = results.getModel() ;
                
            if ( query.isReduced() )
            {
                // Reduced - best we can do is DISTINCT
                ResultSetRewindable x = ResultSetFactory.makeRewindable(resultsExpected) ;    
                resultsExpected = ResultSetFormatter.toModel(unique(x)) ;
                resultsActual = unique(resultsActual) ;
            }
                
            //System.err.println(getName()+": Result set model testing") ;
            boolean b = checkResultsByModel(query, resultsExpected, resultsActual) ;
            if ( ! b )
                printFailedResultSetTest(query, qe, ResultSetFactory.makeRewindable(resultsExpected), resultsActual) ;
            assertTrue("Results do not match: "+testItem.getName(), b) ;
        }
        else
            fail("Wrong result type for SELECT query") ;
    }

    private static ResultSetRewindable unique(ResultSetRewindable results)
    {
        // VERY crude.  Utilises the fact that bindings have value equality.
        List<Binding> x = new ArrayList<Binding>() ;
        Set<Binding> seen = new HashSet<Binding>() ;
        
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

    public static boolean resultSetEquivalent(Query query, ResultSetRewindable resultsExpected, ResultSetRewindable resultsActual)
    {
        if ( query.isOrdered() )
            return ResultSetCompare.equalsByValueAndOrder(resultsExpected, resultsActual) ;
        else
            return ResultSetCompare.equalsByValue(resultsExpected, resultsActual) ;
    }
    
    // TEMPORARY
    private boolean checkResultsByModel(Query query, Model expectedModel, ResultSetRewindable results)
    {
        // Fudge - can't cope with ordered results properly.  The output writer for ResultSets does nto add rs:index.
        
        results.reset() ;
        Model actualModel = ResultSetFormatter.toModel(results) ;
        // Tidy the models.
        // Very regretable.
        
        expectedModel.removeAll(null, RDF.type,  ResultSetGraphVocab.ResultSet) ;
        expectedModel.removeAll(null, RDF.type,  ResultSetGraphVocab.ResultSolution) ;
        expectedModel.removeAll(null, RDF.type,  ResultSetGraphVocab.ResultBinding) ;
        expectedModel.removeAll(null, ResultSetGraphVocab.size,  (RDFNode)null) ;
        expectedModel.removeAll(null, ResultSetGraphVocab.index,  (RDFNode)null) ;

        actualModel.removeAll(null, RDF.type,  ResultSetGraphVocab.ResultSet) ;
        actualModel.removeAll(null, RDF.type,  ResultSetGraphVocab.ResultSolution) ;
        actualModel.removeAll(null, RDF.type,  ResultSetGraphVocab.ResultBinding) ;
        actualModel.removeAll(null, ResultSetGraphVocab.size,  (RDFNode)null) ;
        actualModel.removeAll(null, ResultSetGraphVocab.index,  (RDFNode)null) ;
        
        boolean b =  expectedModel.isIsomorphicWith(actualModel) ;
        if ( !b )
        {
            System.out.println("---- Expected") ;
            expectedModel.write(System.out, "TTL") ;
            System.out.println("---- Actual") ;
            actualModel.write(System.out, "TTL") ;
            System.out.println("----");
        }
        return b ;
    }

   void runTestConstruct(Query query, QueryExecution qe) throws Exception
    {
        // Do the query!
        Model resultsActual = qe.execConstruct() ;
        compareGraphResults(resultsActual, query) ;
    }
   
   private void compareGraphResults(Model resultsActual, Query query)
   {
        if ( results != null )
        {
            try {
                if ( ! results.isGraph() )
                    fail("Expected results are not a graph: "+testItem.getName()) ;
                    
                Model resultsExpected = results.getModel() ;
                if ( ! resultsExpected.isIsomorphicWith(resultsActual) )
                {
                    printFailedModelTest(query, resultsExpected, resultsActual) ;
                    fail("Results do not match: "+testItem.getName()) ;
                }
            } catch (Exception ex)
            {
                String typeName = (query.isConstructType()?"construct":"describe") ;
                fail("Exception in result testing ("+typeName+"): "+ex) ;
            }
        }
    }
    
    void runTestDescribe(Query query, QueryExecution qe) throws Exception
    {
        Model resultsActual = qe.execDescribe() ;
        compareGraphResults(resultsActual, query) ;
    }
    
    void runTestAsk(Query query, QueryExecution qe) throws Exception
    {
        boolean result = qe.execAsk() ;
        if ( results != null )
        {
            if ( results.isBoolean() )
            {
                boolean b = results.getBooleanResult() ;
                assertEquals("ASK test results do not match", b, result) ;
            }
            else
            {
                Model resultsAsModel = results.getModel() ;
                StmtIterator sIter = results.getModel().listStatements(null, RDF.type, ResultSetGraphVocab.ResultSet) ;
                if ( !sIter.hasNext() )
                    throw new QueryTestException("Can't find the ASK result") ;
                Statement s = sIter.nextStatement() ;
                if ( sIter.hasNext() )
                    throw new QueryTestException("Too many result sets in ASK result") ;
                Resource r = s.getSubject() ;
                Property p = resultsAsModel.createProperty(ResultSetGraphVocab.getURI()+"boolean") ;

                boolean x = r.getRequiredProperty(p).getBoolean() ;
                if ( x != result )
                    assertEquals("ASK test results do not match", x,result);
            }
        }        
        return ;
    }
    
    void printFailedResultSetTest(Query query, QueryExecution qe, ResultSetRewindable qrExpected, ResultSetRewindable qrActual)
    {
       PrintStream out = System.out ;
       out.println() ;
       out.println("=======================================") ;
       out.println("Failure: "+description()) ;
       out.println("Query: \n"+query) ;
//       if ( qe != null && qe.getDataset() != null )
//       {
//           out.println("Data: \n"+qe.getDataset().asDatasetGraph()) ;
//       }
       out.println("Got: "+qrActual.size()+" --------------------------------") ;
       qrActual.reset() ;
       ResultSetFormatter.out(out, qrActual, query.getPrefixMapping()) ;
       qrActual.reset() ;
       out.flush() ;

       
       out.println("Expected: "+qrExpected.size()+" -----------------------------") ;
       qrExpected.reset() ;
       ResultSetFormatter.out(out, qrExpected, query.getPrefixMapping()) ;
       qrExpected.reset() ;
       
       out.println() ;
       out.flush() ;
   }

    void printFailedModelTest(Query query, Model expected, Model results)
    {
        PrintWriter out = FileUtils.asPrintWriterUTF8(System.out) ;
        out.println("=======================================") ;
        out.println("Failure: "+description()) ;
        results.write(out, "TTL") ;
        out.println("---------------------------------------") ;
        expected.write(out, "TTL") ;
        out.println() ;
    }
    
    @Override
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
            for ( Iterator<String> iter = testItem.getDefaultGraphURIs().iterator() ; iter.hasNext() ; )
                tmp = tmp+iter.next() ;
        }
        if ( testItem.getNamedGraphURIs() != null )
        {
            for ( Iterator<String> iter = testItem.getNamedGraphURIs().iterator() ; iter.hasNext() ; )
                tmp = tmp+iter.next() ;
        }
        
        String d = "Test "+testNumber+" :: "+testItem.getName() ;
        //+" :: QueryFile="+testItem.getQueryFile()+
        //          ", DataFile="+tmp+", ResultsFile="+testItem.getResultFile() ;
        return d ;
    }
}
