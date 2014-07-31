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

package com.hp.hpl.jena.sparql.engine;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

/**
 * Tests for multi-threaded query execution
 */
public class TestQueryEngineMultiThreaded {
    private class RunResult {
        public int numFailures;
        public List<Exception> exceptions = new ArrayList<>();
    }
    
    @Test
    public void parallel_sparql_construct_default_model_read_lock() throws Exception {
        Model model = this.createDefaultModel();
        this.testParallelConstruct(model, Lock.READ, EXPECTED_NUM_TRIPLES);
    }

    @Test
    public void parallel_sparql_construct_inference_model_read_lock() throws Exception {
        Model model = createForwardChainingModel();
        this.testParallelConstruct(model, Lock.READ, EXPECTED_NUM_REASONER_TRIPLES);
    }
    
    @Test
    public void parallel_sparql_construct_default_model_write_lock() throws Exception {
        Model model = this.createDefaultModel();
        this.testParallelConstruct(model, Lock.WRITE, EXPECTED_NUM_TRIPLES);
    }

    @Test
    public void parallel_sparql_construct_inference_model_write_lock() throws Exception {
        Model model = createForwardChainingModel();
        this.testParallelConstruct(model, Lock.WRITE, EXPECTED_NUM_REASONER_TRIPLES);
    }
    
    @Test
    public void parallel_sparql_select_default_model_read_lock() throws Exception {
        Model model = this.createDefaultModel();
        this.testParallelSelect(model, Lock.READ, EXPECTED_NUM_RESULTS);
    }

    @Test
    public void parallel_sparql_select_inference_model_read_lock() throws Exception {
        Model model = createForwardChainingModel();
        this.testParallelSelect(model, Lock.READ, EXPECTED_NUM_REASONER_RESULTS);
    }
    
    @Test
    public void parallel_sparql_select_default_model_write_lock() throws Exception {
        Model model = this.createDefaultModel();
        this.testParallelSelect(model, Lock.WRITE, EXPECTED_NUM_RESULTS);
    }

    @Test
    public void parallel_sparql_select_inference_model_write_lock() throws Exception {
        Model model = createForwardChainingModel();
        this.testParallelSelect(model, Lock.WRITE, EXPECTED_NUM_REASONER_RESULTS);
    }

    private void testParallelConstruct(Model model, boolean lock, int expected) throws Exception {
        RunResult runResult = new RunResult();
        List<Thread> threads = createSparqlConstructExecutionThreads(model, lock, expected, runResult);
        executeThreads(threads);
        assertEquals(0, runResult.exceptions.size());
        assertEquals(0, runResult.numFailures);
    }
    
    private void testParallelSelect(Model model, boolean lock, int expected) throws Exception {
        RunResult runResult = new RunResult();
        List<Thread> threads = createSparqlSelectExecutionThreads(model, lock, expected, runResult);
        executeThreads(threads);
        assertEquals(0, runResult.exceptions.size());
        assertEquals(0, runResult.numFailures);
    }

    private List<Thread> createSparqlConstructExecutionThreads(Model model, boolean lock, int expected, RunResult runResult) {
        List<Thread> threads = new ArrayList<>();

        for (int thread = 0; thread < NUMBER_OF_THREADS; thread++) {
            threads.add(createExecuteSparqlConstructThread(model, lock, expected, runResult));
        }
        return threads;
    }
    
    private List<Thread> createSparqlSelectExecutionThreads(Model model, boolean lock, int expected, RunResult runResult) {
        List<Thread> threads = new ArrayList<>();
        
        for (int thread = 0; thread < NUMBER_OF_THREADS; thread++) {
            threads.add(createExecuteSparqlSelectThread(model, lock, expected, runResult));
        }
        return threads;
    }

    private Thread createExecuteSparqlConstructThread(final Model model, final boolean lock, final int expected, final RunResult runResult) {
        return new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < NUMBER_OF_LOOPS; i++) {
                        Model resultModel = executeSparqlConstruct(model, CONSTRUCT_QUERY, lock);
                        if (resultModel.size() != expected) {
                            runResult.numFailures++;
                        }
                    }
                } catch (Exception e) {
                    runResult.exceptions.add(e);
                }
            }
        };
    }
    
    private Thread createExecuteSparqlSelectThread(final Model model, final boolean lock, final int expected, final RunResult runResult) {
        return new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < NUMBER_OF_LOOPS; i++) {
                        ResultSetRewindable rset = executeSparqlSelect(model, SELECT_QUERY, lock);
                        if (rset.size() != expected) {
                            runResult.numFailures++;
                        }
                    }
                } catch (Exception e) {
                    runResult.exceptions.add(e);
                }
            }
        };
    }

    private void executeThreads(List<Thread> threads) throws Exception {
        for ( Thread thread2 : threads )
        {
            thread2.start();
        }
        for ( Thread thread1 : threads )
        {
            thread1.join();
        }
    }

    private Model executeSparqlConstruct(Model model, String sparql, boolean lock) {
        Query query = QueryFactory.create(sparql);
        try(QueryExecution queryExec = QueryExecutionFactory.create(query, model)) {
            model.enterCriticalSection(lock);
            try { return queryExec.execConstruct() ; }
            finally { model.leaveCriticalSection() ; }
        }
    }
    
    private ResultSetRewindable executeSparqlSelect(Model model, String sparql, boolean lock) {
        Query query = QueryFactory.create(sparql);
        try(QueryExecution queryExec = QueryExecutionFactory.create(query, model)) {
            model.enterCriticalSection(lock);
            try {
                return ResultSetFactory.makeRewindable(queryExec.execSelect());
            } finally {
                model.leaveCriticalSection();
            }
        }
    }
    
    private Model createDefaultModel() {
        Model def = ModelFactory.createDefaultModel();
        def.read(new ByteArrayInputStream(TURTLE_RDF.getBytes()), "", "TURTLE");
        return def;
    }

    private Model createForwardChainingModel() {
        Model baseModel = ModelFactory.createDefaultModel();
        Model schemaModel = ModelFactory.createDefaultModel();
        Model configurationRuleReasoner = ModelFactory.createDefaultModel();
        com.hp.hpl.jena.rdf.model.Resource configuration = configurationRuleReasoner.createResource();
        configuration.addProperty(ReasonerVocabulary.PROPruleMode, "forward");
        configuration.addProperty(ReasonerVocabulary.PROPsetRDFSLevel, ReasonerVocabulary.RDFS_SIMPLE);
        Reasoner ruleReasoner = RDFSRuleReasonerFactory.theInstance().create(configuration);
        InfModel inf = ModelFactory.createInfModel(ruleReasoner, schemaModel, baseModel);
        inf.read(new ByteArrayInputStream(TURTLE_RDF.getBytes()), "", "TURTLE");
        return inf;
    }

    private static final int NUMBER_OF_THREADS = 50;
    private static final int NUMBER_OF_LOOPS = 50;
    private static final int EXPECTED_NUM_REASONER_TRIPLES = 42;
    private static final int EXPECTED_NUM_TRIPLES = 42;
    private static final int EXPECTED_NUM_REASONER_RESULTS = 6;
    private static final int EXPECTED_NUM_RESULTS = 6;
    private static final String CONSTRUCT_QUERY = StrUtils.strjoinNL("BASE <http://example.com/2010/04/>",
            "construct {",
            "?subj <gender> ?gender .",
            "?subj <name> ?name .",
            "?subj <age> ?age .",
            "?subj <med> ?med .",
            "}",
            "where ",
            "{",
            "?subj a <person> .",
            "OPTIONAL { ?subj <gender> ?gender .",
            "?subj <name> ?name .",
            "?subj <age> ?age .",
            "?subj <med> ?med . }",
            "}");
    private static final String SELECT_QUERY = StrUtils.strjoinNL("BASE <http://example.com/2010/04/>",
            "SELECT * WHERE { ?s a <person> }"
            );
    private static final String TURTLE_RDF = "@base <http://example.com/2010/04/> ." + ""
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
            + ""
            + "<patient> rdfs:subClassOf <person> . "
            + "<name> rdfs:subClassOf <fullName> . "
            + "<age> rdfs:subClassOf <yearsOld> . "
            + "<med> rdfs:subClassOf <drug> . "
            + ""
            + "<patient/1234> a <person> . "
            + "<patient/1234> <name> \"Fred Smith\" ."
            + "<patient/1234> <gender> \"Male\" ."
            + "<patient/1234> <age> \"24\" ."
            + "<patient/1234> <med> \"Aspirin\" ."
            + "<patient/1234> <med> \"Atenolol\" ."
            + "<patient/1234> <med> \"Acetominophen\" ."
            + "<patient/1234> <med> \"Ibuprofen\" ."
            + ""
            + "<patient/1235> a <person> . "
            + "<patient/1235> <name> \"F Smith\" ."
            + "<patient/1235> <gender> \"Male\" ."
            + "<patient/1235> <age> \"34\" ."
            + "<patient/1235> <med> \"Aspirin\" ."
            + "<patient/1235> <med> \"Atenolol\" ."
            + "<patient/1235> <med> \"Acetominophen\" ."
            + "<patient/1235> <med> \"Ibuprofen\" ."
            + ""
            + "<patient/1236> a <person> . "
            + "<patient/1236> <name> \"Frederick Smith\" ."
            + "<patient/1236> <gender> \"Male\" ."
            + "<patient/1236> <age> \"44\" ."
            + "<patient/1236> <med> \"Aspirin\" ."
            + "<patient/1236> <med> \"Atenolol\" ."
            + "<patient/1236> <med> \"Acetominophen\" ."
            + "<patient/1236> <med> \"Ibuprofen\" ."
            + ""
            + "<patient/1237> a <person> . "
            + "<patient/1237> <name> \"Freddie Smith\" ."
            + "<patient/1237> <gender> \"Male\" ."
            + "<patient/1237> <age> \"14\" ."
            + "<patient/1237> <med> \"Aspirin\" ."
            + "<patient/1237> <med> \"Atenolol\" ."
            + "<patient/1237> <med> \"Acetominophen\" ."
            + "<patient/1237> <med> \"Ibuprofen\" ."
            + ""
            + "<patient/1238> a <person> . "
            + "<patient/1238> <name> \"Fredd Smith\" ."
            + "<patient/1238> <gender> \"Male\" ."
            + "<patient/1238> <age> \"54\" ."
            + "<patient/1238> <med> \"Aspirin\" ."
            + "<patient/1238> <med> \"Atenolol\" ."
            + "<patient/1238> <med> \"Acetominophen\" ."
            + "<patient/1238> <med> \"Ibuprofen\" ."
            + ""
            + "<patient/1239> a <person> . "
            + "<patient/1239> <name> \"Frederic Smith\" ."
            + "<patient/1239> <gender> \"Male\" ."
            + "<patient/1239> <age> \"64\" ."
            + "<patient/1239> <med> \"Aspirin\" ."
            + "<patient/1239> <med> \"Atenolol\" ."
            + "<patient/1239> <med> \"Acetominophen\" ."
            + "<patient/1239> <med> \"Ibuprofen\" .";
}