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

package org.apache.jena.arq.junit.sparql.tests;

import static org.apache.jena.arq.junit.sparql.tests.SparqlTestLib.setupFailure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.irix.IRIs;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.junit.QueryTestException;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sparql.vocabulary.ResultSetGraphVocab;
import org.apache.jena.sparql.vocabulary.TestManifest;
import org.apache.jena.system.Txn;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;

public class QueryExecTest implements Runnable {

    private final ManifestEntry testEntry;
    private final SPARQLResult results;
    private final QueryTestItem testItem;
    private final Creator<Dataset> creator;

    public QueryExecTest(ManifestEntry entry, Creator<Dataset> maker) {
        testEntry = entry;
        testItem = QueryTestItem.create(testEntry.getEntry(), TestManifest.QueryEvaluationTest);
        results = testItem.getResults();
        creator = maker;
    }

    public QueryExecTest(ManifestEntry entry) {
        this(entry, ()->DatasetFactory.createGeneral());
    }

    @Override
    public void run() {
        Query query;
        try {
            try {
                query = SparqlTestLib.queryFromEntry(testEntry);
            } catch (QueryException qEx) {
                qEx.printStackTrace(System.err);
                setupFailure("Parse failure: " + qEx.getMessage());
                throw qEx;
            }

            Dataset dataset = setUpDataset(query, testItem);
            if ( dataset == null && !doesQueryHaveDataset(query) )
                setupFailure("No dataset for query");

            if ( dataset != null )
                Txn.executeRead(dataset, ()->execute(dataset,query));
            else
                execute(null,query);
        } catch (NullPointerException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            setupFailure("Exception: " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private void execute(Dataset dataset, Query query) {
        try (QueryExecution qe = (dataset == null)
                        ? QueryExecutionFactory.create(query)
                        : QueryExecutionFactory.create(query, dataset)) {
            if ( query.isSelectType() )
                runTestSelect(query, qe);
            else if ( query.isConstructType() )
                runTestConstruct(query, qe);
            else if ( query.isDescribeType() )
                runTestDescribe(query, qe);
            else if ( query.isAskType() )
                runTestAsk(query, qe);
            else if ( query.isJsonType() )
                throw new UnsupportedOperationException("JSON {} queries not supported");
        }
    }

    protected Dataset setUpDataset(Query query, QueryTestItem testItem) {
        try {
            // testItem.requiresTextIndex()

            if ( doesQueryHaveDataset(query) && doesTestItemHaveDataset(testItem) ) {
                // Only warn if there are results to test
                // Syntax tests may have FROM etc and a manifest data file.
                if ( testItem.getResultFile() != null )
                    Log.warn(this, testItem.getName() + " : query data source and also in test file");
            }

            // In test file?
            if ( doesTestItemHaveDataset(testItem) )
                // Not specified in the query - get from test item and load
                return createDataset(testItem.getDefaultGraphURIs(), testItem.getNamedGraphURIs());

            if ( !doesQueryHaveDataset(query) )
                setupFailure("No dataset");

            // Left to query
            return null;

        } catch (JenaException jEx) {
            setupFailure("JenaException creating data source: " + jEx.getMessage());
            return null;
        }
    }

    protected Dataset createEmptyDataset() {
        return creator.create();
    }

    private static boolean doesTestItemHaveDataset(QueryTestItem testItem) {
        boolean r = (testItem.getDefaultGraphURIs() != null && testItem.getDefaultGraphURIs().size() > 0)
                    || (testItem.getNamedGraphURIs() != null && testItem.getNamedGraphURIs().size() > 0);
        return r;
    }

    private static boolean doesQueryHaveDataset(Query query) {
        return query.hasDatasetDescription();
    }

    private Dataset createDataset(List<String> defaultGraphURIs, List<String> namedGraphURIs) {
        // Allow "qt:data" to be quads in defaultGraphURIs.
        SystemARQ.UsePlainGraph = true;
        try {
            Dataset ds = createEmptyDataset();
            Txn.executeWrite(ds, ()->{
                if ( defaultGraphURIs != null ) {
                    for ( String sourceURI : defaultGraphURIs ) {
                        SparqlTestLib.parser(sourceURI).parse(ds);
                    }
                }
                if ( namedGraphURIs != null ) {
                    for ( String sourceURI : namedGraphURIs ) {
                        String absSourceURI = IRIs.resolve(sourceURI);
                        SystemARQ.UsePlainGraph = true;
                        Model m = ds.getNamedModel(absSourceURI);
                        SparqlTestLib.parser(sourceURI).parse(m);
                    }
                }
            });
            return ds;
        }
        finally {
            SystemARQ.UsePlainGraph = false;
        }

    }

    private void runTestSelect(Query query, QueryExecution qe) {
        QueryTestItem testItem = QueryTestItem.create(testEntry.getEntry(), TestManifest.QueryEvaluationTest);
        // Do the query!
        ResultSetRewindable resultsActual = ResultSetFactory.makeRewindable(qe.execSelect());

        qe.close();

        if ( results == null )
            return;

        // Assumes resultSetCompare can cope with full isomorphism possibilities.
        ResultSetRewindable resultsExpected;
        if ( results.isResultSet() )
            resultsExpected = ResultSetFactory.makeRewindable(results.getResultSet());
        else if ( results.isModel() )
            resultsExpected = ResultSetFactory.makeRewindable(results.getModel());
        else {
            fail("Wrong result type for SELECT query");
            resultsExpected = null; // Keep the compiler happy
        }

        if ( query.isReduced() ) {
            // Reduced - best we can do is DISTINCT
            resultsExpected = unique(resultsExpected);
            resultsActual = unique(resultsActual);
        }

        // Hack for CSV : tests involving bNodes need manually checking.
        if ( testItem.getResultFile().endsWith(".csv") ) {
            resultsActual = convertToStrings(resultsActual);
            resultsActual.reset();

            int nActual = ResultSetFormatter.consume(resultsActual);
            int nExpected = ResultSetFormatter.consume(resultsExpected);
            resultsActual.reset();
            resultsExpected.reset();
            assertEquals("CSV: Different number of rows", nExpected, nActual);
            boolean b = resultSetEquivalent(query, resultsExpected, resultsActual);
            if ( !b )
                System.out.println("Manual check of CSV results required: " + testItem.getName());
            return;
        }

        boolean b = resultSetEquivalent(query, resultsExpected, resultsActual);

        if ( !b ) {
            resultsExpected.reset();
            resultsActual.reset();
            boolean b2 = resultSetEquivalent(query, resultsExpected, resultsActual);
            printFailedResultSetTest(query, qe, resultsExpected, resultsActual);
        }
        assertTrue("Results do not match", b);

        return;
    }

    private ResultSetRewindable convertToStrings(ResultSetRewindable resultsActual) {
        List<Binding> bindings = new ArrayList<>();
        while (resultsActual.hasNext()) {
            Binding b = resultsActual.nextBinding();
            BindingBuilder builder = Binding.builder();

            for ( String vn : resultsActual.getResultVars() ) {
                Var v = Var.alloc(vn);
                Node n = b.get(v);
                String s;
                if ( n == null )
                    s = "";
                else if ( n.isBlank() )
                    s = "_:" + n.getBlankNodeLabel();
                else
                    s = NodeFunctions.str(n);
                builder.add(v, NodeFactory.createLiteral(s));
            }
            bindings.add(builder.build());
        }
        ResultSet rs = new ResultSetStream(resultsActual.getResultVars(), null, QueryIterPlainWrapper.create(bindings.iterator()));
        return rs.rewindable();
    }

    private static ResultSetRewindable unique(ResultSetRewindable results) {
        // VERY crude. Utilises the fact that bindings have value equality.
        List<Binding> x = new ArrayList<>();
        Set<Binding> seen = new HashSet<>();

        for ( ; results.hasNext() ; ) {
            Binding b = results.nextBinding();
            if ( seen.contains(b) )
                continue;
            seen.add(b);
            x.add(b);
        }
        QueryIterator qIter = QueryIterPlainWrapper.create(x.iterator());
        ResultSet rs = new ResultSetStream(results.getResultVars(), ModelFactory.createDefaultModel(), qIter);
        return rs.rewindable();
    }

    private static boolean resultSetEquivalent(Query query, ResultSetRewindable resultsExpected, ResultSetRewindable resultsActual) {
        final boolean testByValue = true;
        if ( testByValue ) {
            if ( query.isOrdered() )
                return ResultSetCompare.equalsByValueAndOrder(resultsExpected, resultsActual);
            else
                return ResultSetCompare.equalsByValue(resultsExpected, resultsActual);
        } else {
            if ( query.isOrdered() )
                return ResultSetCompare.equalsByTermAndOrder(resultsExpected, resultsActual);
            else
                return ResultSetCompare.equalsByTerm(resultsExpected, resultsActual);
        }
    }

    private void runTestConstruct(Query query, QueryExecution qe) {
        // Do the query!
        if ( query.isConstructQuad() ) {
            Dataset resultActual = qe.execConstructDataset();
            compareDatasetResults(resultActual, query);
        } else {
            Model resultsActual = qe.execConstruct();
            compareGraphResults(resultsActual, query);
        }
    }

    private void compareGraphResults(Model resultsActual, Query query) {
        if ( results != null ) {
            try {
                if ( !results.isGraph() )
                    SparqlTestLib.testFailure("Expected results are not a graph: " + testItem.getName());

                Model resultsExpected = results.getModel();


                boolean pass = IsoMatcher.isomorphic(resultsExpected.getGraph(), results.getModel().getGraph()) ;
                // Does not cope with <<>>
                //boolean pass = resultsExpected.isIsomorphicWith(resultsActual);
                if (! pass ) {
                    printFailedModelTest(query, resultsExpected, resultsActual);
                    fail("Results do not match: " + testItem.getName());
                }
            } catch (Exception ex) {
                String typeName = (query.isConstructType() ? "construct" : "describe");
                SparqlTestLib.testFailure("Exception in result testing (" + typeName + "): " + ex);
            }
        }
    }

    private void compareDatasetResults(Dataset resultsActual, Query query) {
        if ( results != null ) {
            try {
                if ( !results.isDataset() )
                    SparqlTestLib.testFailure("Expected results are not a graph: " + testItem.getName());

                Dataset resultsExpected = results.getDataset();
                if ( !IsoMatcher.isomorphic(resultsExpected.asDatasetGraph(), resultsActual.asDatasetGraph()) ) {
                    printFailedDatasetTest(query, resultsExpected, resultsActual);
                    fail("Results do not match: " + testItem.getName());
                }
            } catch (Exception ex) {
                String typeName = (query.isConstructType() ? "construct" : "describe");
                fail("Exception in result testing (" + typeName + "): " + ex);
            }
        }
    }

    private void runTestDescribe(Query query, QueryExecution qe) {
        Model resultsActual = qe.execDescribe();
        compareGraphResults(resultsActual, query);
    }

    private void runTestAsk(Query query, QueryExecution qe) {
        boolean result = qe.execAsk();
        if ( results != null ) {
            if ( results.isBoolean() ) {
                boolean b = results.getBooleanResult();
                assertEquals("ASK test results do not match", b, result);
            } else {
                Model resultsAsModel = results.getModel();
                StmtIterator sIter = results.getModel().listStatements(null, RDF.type, ResultSetGraphVocab.ResultSet);
                if ( !sIter.hasNext() )
                    throw new QueryTestException("Can't find the ASK result");
                Statement s = sIter.nextStatement();
                if ( sIter.hasNext() )
                    throw new QueryTestException("Too many result sets in ASK result");
                Resource r = s.getSubject();
                Property p = resultsAsModel.createProperty(ResultSetGraphVocab.getURI() + "boolean");

                boolean x = r.getRequiredProperty(p).getBoolean();
                if ( x != result )
                    assertEquals("ASK test results do not match", x, result);
            }
        }
        return;
    }

    private void printFailedResultSetTest(Query query, QueryExecution qe, ResultSetRewindable qrExpected, ResultSetRewindable qrActual) {
        PrintStream out = System.out;
        out.println();
        out.println("=======================================");
        out.println("Failure: " + description());
        out.println("Query: \n" + query);
        if ( qe != null && qe.getDataset() != null ) {
            out.println("Data:");
            RDFDataMgr.write(out, qe.getDataset(), Lang.TRIG);
        }
        out.println("Got: " + qrActual.size() + " --------------------------------");
        qrActual.reset();
        ResultSetFormatter.out(out, qrActual, query.getPrefixMapping());
        qrActual.reset();
        out.flush();

        out.println("Expected: " + qrExpected.size() + " -----------------------------");
        qrExpected.reset();
        ResultSetFormatter.out(out, qrExpected, query.getPrefixMapping());
        qrExpected.reset();

        out.println();
        out.flush();
    }

    private void printFailedModelTest(Query query, Model expected, Model results) {
        PrintWriter out = FileUtils.asPrintWriterUTF8(System.out);
        out.println("=======================================");
        out.println("Failure: " + description());
        results.write(out, "TTL");
        out.println("---------------------------------------");
        expected.write(out, "TTL");
        out.println();
    }

    private void printFailedDatasetTest(Query query, Dataset expected, Dataset results) {
        System.out.println("=======================================");
        System.out.println("Failure: " + description());
        RDFDataMgr.write(System.out, results, Lang.TRIG);
        System.out.println("---------------------------------------");
        RDFDataMgr.write(System.out, expected, Lang.TRIG);
        System.out.println();
    }

    @Override
    public String toString() {
        if ( testItem.getName() != null )
            return testItem.getName();
        return testEntry.getName();
    }

    // Cache
    private String description() {
        String tmp = "";
        if ( testItem.getDefaultGraphURIs() != null ) {
            for ( String s : testItem.getDefaultGraphURIs() ) {
                tmp = tmp + s;
            }
        }
        if ( testItem.getNamedGraphURIs() != null ) {
            for ( String s : testItem.getNamedGraphURIs() ) {
                tmp = tmp + s;
            }
        }

        String d = "Test " + testItem.getName();
        // String d = "Test "+testNumber+" :: "+testItem.getName();
        // +" :: QueryFile="+testItem.getQueryFile()+
        // ", DataFile="+tmp+", ResultsFile="+testItem.getResultFile();
        return d;
    }

}
