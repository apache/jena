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

package org.apache.jena.query.text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDatasetConcurrencyWithConfigurableAnalyzer extends AbstractTestDatasetWithAnalyzer {
    @Override
    @Before
    public void before () {
        init(StrUtils.strjoinNL(
                "text:ConfigurableAnalyzer ;",
                "text:tokenizer text:WhitespaceTokenizer ;",
                "text:filters (text:ASCIIFoldingFilter text:LowerCaseFilter)"
        ));
        initDataset();
    }

    private void initDataset() {
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.WRITE);
        for (int i = 0; i < 1000; i++) {
            String token = "Foo" + i;
            model.createResource(RESOURCE_BASE + token).addProperty(RDFS.label, token);
        }
        dataset.commit();
    }

    private boolean testOneQuery(int probe) {
        final String testName = "testConfigurableAnalyzerIsConcurrencySafe" + probe;
        String query = QUERY_PROLOG + "select ?s WHERE {?s text:query (rdfs:label 'foo" + probe + "' 10).}";
        try {
            doTestQuery(dataset, testName, query, Sets.newHashSet(RESOURCE_BASE + "Foo" + probe), 1);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Test
    public void testConfigurableAnalyzerIsConcurrencySafe () {
        final int parallelism = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        try {
            for (int i = 0; i < 20; i++) {
                List<Future<Boolean>> results = new ArrayList<>(parallelism);
                for (int j = 0; j < parallelism; j++) {
                    final int probe = i;
                    results.add(executorService.submit(() -> testOneQuery(probe)));;
                }
                for (int j = 0; j < parallelism; j++) {
                    Assert.assertTrue("Probe " + i + " failed", results.get(j).get());
                }
            }
        } catch (InterruptedException e) {
            // exit silently on interrupt
        } catch (ExecutionException e) {
            Assert.assertTrue("Concurrency exception: " + e.getMessage(), false);
        }
    }
}
