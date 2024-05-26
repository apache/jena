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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.iterator.QueryIterSlice;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.apache.jena.sparql.util.Context;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * Base class for test suites that compare SERVICE requests with features of the service enhancer plugin.
 * Primarily tests results for equivalence with and without use of 'cache:'.
 * Furthermore, requests with / without use of 'bulk:' and 'loop:' (with an empty input binding) can also be compared.
 * Query results should be the same regardless of whether these options are present or absent.
 */
@RunWith(Parameterized.class)
public class TestServiceEnhancerCachedVsUncached {

    protected String name;
    protected String queryStrA;
    protected String queryStrB;
    protected Model model;
    protected Consumer<Context> cxtMutator;

    public TestServiceEnhancerCachedVsUncached(String name, String queryStrA, String queryStrB, Model model, Consumer<Context> cxtMutator) {
        super();
        this.name = name;
        this.queryStrA = queryStrA;
        this.queryStrB = queryStrB;
        this.model = model;
        this.cxtMutator = cxtMutator;
    }

    @Test
    public void test() {
        Log.debug(TestServiceEnhancerCachedVsUncached.class, "Query A: " + queryStrA);
        Log.debug(TestServiceEnhancerCachedVsUncached.class, "Query B: " + queryStrB);

        // Debug flag: If onlyA is true then no comparison with queryB is made
        boolean onlyA = false;

        Query queryA = QueryFactory.create(queryStrA);
        ResultSetRewindable rsA;
        try (QueryExecution qeA = QueryExecution.create(queryA, model)) {
            cxtMutator.accept(qeA.getContext());
            rsA = ResultSetFactory.makeRewindable(qeA.execSelect());

            if (!onlyA) {
                Query queryB = QueryFactory.create(queryStrB);
                ResultSetRewindable rsB;
                try (QueryExecution qeB = QueryExecution.create(queryB, model)) {
                    cxtMutator.accept(qeB.getContext());
                    rsB = ResultSetFactory.makeRewindable(qeB.execSelect());

                    boolean isEqual = ResultSetCompare.equalsByValue(rsA, rsB);
                    if (!isEqual) {
                        rsA.reset();
                        ResultSetFormatter.out(System.out, rsA);

                        rsB.reset();
                        ResultSetFormatter.out(System.out, rsB);
                    }
                    Assert.assertTrue(isEqual);
                }
            } else {
                rsA.reset();
                System.out.println("Got " + ResultSetFormatter.consume(rsA) + " results");
            }

        }
    }




    @Parameters(name = "SPARQL Cache Test {index}: {0}")
    public static Collection<Object[]> data()
            throws Exception

    {
        int randomSeed = 42;
        Random random = new Random(randomSeed);
        int resourceCount = random.nextInt(100) + 1;
        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(resourceCount);

        int resultSetLimit = random.nextInt(20) + 1;

        // System.out.println("ResourceCount: " + resourceCount);
        // System.out.println("ResultSetLimit: " + resultSetLimit);

        ServiceResponseCache contentCache = new ServiceResponseCache();
        ServiceResultSizeCache limitCache = new ServiceResultSizeCache();

        ServiceExecutorRegistry reg = ServiceExecutorRegistry.get().copy();
        reg.addBulkLink(
                (op, iter, execCxt, chain) -> {
                        return new QueryIterSlice(chain.createExecution(op, iter, execCxt), 0, resultSetLimit, execCxt);
                    }
                );


        List<Object[]> pool = new ArrayList<>();

        for (int i = 0; i < 1000; ++i) {
            int bulkSizeA = random.nextInt(9) + 1;
            int bulkSizeB = random.nextInt(9) + 1;
            int inputOffset = random.nextInt(resourceCount);
            int inputLimit = random.nextInt(resourceCount - inputOffset);

            String info = String.format("Run %d: bulkSizeA=%d bulkSizeB=%d offset=%d limit%d", i, bulkSizeA, bulkSizeB, inputOffset, inputLimit);
            Log.debug(TestServiceEnhancerCachedVsUncached.class, info);

            String strBase = "SELECT * { { SELECT ?d { ?d a <urn:Department> } ORDER BY ASC(?d) LIMIT ${limit} OFFSET ${offset} } SERVICE <${mode}> { SELECT * { ?d <urn:hasEmployee> ?p } ORDER BY ?p  } }"
                    .replaceAll(Pattern.quote("${offset}"), "" + inputOffset)
                    .replaceAll(Pattern.quote("${limit}"), "" + inputLimit);

            String strA = strBase
                    .replaceAll(Pattern.quote("${mode}"), "cache:loop:bulk+" + bulkSizeA + ":");


            String strB = strBase
                    .replaceAll(Pattern.quote("${mode}"), "loop:bulk+" + bulkSizeB + ":");

            Consumer<Context> cxtMutator = cxt-> {
                ServiceEnhancerInit.wrapOptimizer(cxt);
                ServiceResponseCache.set(cxt, contentCache);
                ServiceResultSizeCache.set(cxt, limitCache);
                ServiceExecutorRegistry.set(cxt, reg);
            };

            pool.add(new Object[] { "test" + i, strA, strB, model, cxtMutator });
        }

        return pool;
    }

}
