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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterSlice;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTestServiceEnhancerResultSetLimits {

    protected String mode;

    public AbstractTestServiceEnhancerResultSetLimits(String mode) {
        this.mode = mode;
    }

    private static final Property hasEmployee = ResourceFactory.createProperty("urn:hasEmployee");
    private static final Property Person = ResourceFactory.createProperty("urn:Person");
    private static final Property Department = ResourceFactory.createProperty("urn:Department");

    public static Model createModel(int departments) {
        Model result = ModelFactory.createDefaultModel();
        // int departments = 4;

        for (int d = 1; d <= departments; ++d) {
            Resource department = result.createResource("urn:dept" + d)
                        .addProperty(RDF.type, Department)
                        .addLiteral(RDFS.label, "Department " + d);


            for (int e = 1; e <= (departments - d + 1); ++e) {
                Resource person = result.createResource("urn:person" + e)
                        .addProperty(RDF.type, Person)
                        .addLiteral(RDFS.label, "Person " + e);

                department.addProperty(hasEmployee, person);
            }
        }

        return result;
    }

    /** Departments in ascending order */
    @Test
    // @Ignore
    public void testLoop01_asc_limit1() {
        Model model = createModel(4);
        int rows = test(model, "SELECT * { { SELECT ?d { ?d a <urn:Department> } ORDER BY ASC(?d) } SERVICE <${mode}> { ?d <urn:hasEmployee> ?p }}", 1);
        Assert.assertEquals(4, rows);
    }

    @Test
    // @Ignore
    public void testLoop01_asc_limit2() {
        // System.err.println("testLoop01_asc_limit2");

        Model model = createModel(4);
        int rows = test(model, "SELECT * { { SELECT ?d { ?d a <urn:Department> } ORDER BY ASC(?d) } SERVICE <${mode}> { ?d <urn:hasEmployee> ?p }}", 2);
        Assert.assertEquals(7, rows);
    }

    /** Departments in descending order */
    @Test
    // @Ignore
    public void testLoop01_desc_limit1() {
        Model model = createModel(4);
        int rows = test(model, "SELECT * { { SELECT ?d { ?d a <urn:Department> } ORDER BY DESC(?d) } SERVICE <${mode}> { ?d <urn:hasEmployee> ?p }}", 1);
        //int rows = test(model, "SELECT * { SELECT ?d { ?d a <urn:Department> } ORDER BY DESC(?d) }", 1);
        Assert.assertEquals(4, rows);
    }

    @Test
    // @Ignore
    public void testLoop01_desc_limit2() {
        // System.out.println("testLoop01_desc_limit2");

        Model model = createModel(4);
        int rows = test(model, "SELECT * { { SELECT ?d { ?d a <urn:Department> } ORDER BY DESC(?d) } SERVICE <${mode}> { ?d <urn:hasEmployee> ?p }}", 2);
        Assert.assertEquals(7, rows);
    }


    /** There are exactly 10 results with a result set limit of 10 -
     *  so a separate request that only yields the end marker may be needed */
    @Test
    // @Ignore
    public void testLoop01_asc_limit10() {
        // System.err.println("testLoop01_asc_limit10");

        Model model = createModel(4);
        int rows = test(model, "SELECT * { { SELECT ?d { ?d a <urn:Department> } ORDER BY ASC(?d) } SERVICE <${mode}> { ?d <urn:hasEmployee> ?p }}", 10);
        Assert.assertEquals(10, rows);
    }


    public int test(Model model, String rawQueryStr, int hiddenLimit) {
        ServiceResultSizeCache.get().invalidateAll();
        ServiceResponseCache.get().invalidateAll();
        String queryStr = rawQueryStr.replaceAll(Pattern.quote("${mode}"), mode);
        int result = testCore(model, queryStr, hiddenLimit);
        return result;
    }

    private static final Map<Model, Dataset> modelToDataset = new IdentityHashMap<>();

    public static Dataset identityWrap(Model model) {
        Dataset result = modelToDataset.computeIfAbsent(model, DatasetFactory::wrap);
        return result;
    }

    public static int testWithCleanCaches(Model model, String queryStr, int hiddenLimit) {
        return testWithCleanCaches(identityWrap(model), queryStr, hiddenLimit);
    }

    public static int testWithCleanCaches(Dataset dataset, String queryStr, int hiddenLimit) {
        ServiceResultSizeCache.get().invalidateAll();
        ServiceResponseCache.get().invalidateAll();

        int result = testCore(dataset, queryStr, hiddenLimit);
        return result;
    }


    public static int testCore(Model model, String queryStr, int hiddenLimit) {
        return testCore(identityWrap(model), queryStr, hiddenLimit);
    }

    public static int testCore(Dataset dataset, String queryStr, int hiddenLimit) {


        Query query = QueryFactory.create(queryStr);

        // Register a service plugin that slices the result
        ServiceExecutorRegistry reg = ServiceExecutorRegistry.get().copy();
/*
        reg.addBulkLink(
                (op, iter, execCxt, chain) -> {
                    // apply the slice to each input binding
                    QueryIterator x = new QueryIterRepeatApply(iter, execCxt) {
                        @Override
                        protected QueryIterator nextStage(Binding binding) {
                            QueryIterator qi = QueryIterPlainWrapper.create(Collections.singleton(binding).iterator(), execCxt);
                            return new QueryIterSlice(chain.createExecution(op, qi, execCxt), 0, hiddenLimit, execCxt);
                        }
                    };

                    QueryIterator r;
                    boolean failOnPurpose = false;
                    if (failOnPurpose) {
                        QueryIterator y = QueryIterPlainWrapper.create(new AbstractIterator<Binding>() {
                            @Override
                            protected Binding computeNext() {
                                throw new RuntimeException("Synthetic error to test for resource leaks on failure");
                            }
                        }, execCxt);

                        QueryIterConcat z = new QueryIterConcat(execCxt);
                        z.add(x);
                        z.add(y);
                        r = z;
                    } else {
                        r = x;
                    }

                    return r;
                });
*/

//        reg.addBulkLink(
//                (op, iter, execCxt, chain) -> {
//                        return new QueryIterSlice(chain.createExecution(op, iter, execCxt), 0, hiddenLimit, execCxt);
//                    }
//                );

        reg.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            return new QueryIterSlice(chain.createExecution(opExec, opOrig, binding, execCxt), 0, hiddenLimit, execCxt);
        });

        int result;
        try (QueryExecution qe = QueryExecution.create(query, dataset)) {
            Context cxt = qe.getContext();
            // cxt.put(ARQ.enablePropertyFunctions, true);
            ServiceEnhancerInit.wrapOptimizer(cxt);
            ServiceExecutorRegistry.set(qe.getContext(), reg);
            ResultSetRewindable rs = ResultSetFactory.makeRewindable(qe.execSelect());
            // ResultSetFormatter.outputAsJSON(rs);
            result = rs.size();
        }

        return result;
    }
}
