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

package org.apache.jena.sparql.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.DatasetGraphWithLock ;
import org.junit.Assert;
import org.junit.Test;

public class TestDatasetGraphWithLock extends AbstractTestDataset {

    @Override
    protected Dataset createDataset() {
        return DatasetFactory.wrap(new DatasetGraphWithLock(DatasetGraphFactory.create()));
    }

    @Test
    public synchronized void dsg_with_lock_concurrency_01() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            final DatasetGraphWithLock dsg = new DatasetGraphWithLock(DatasetGraphFactory.create());

            Callable<Boolean> callable = new Callable<Boolean>() {

                @Override
                public Boolean call() {
                    dsg.begin(ReadWrite.READ);

                    // Hold the lock for a few seconds
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        // Ignore error
                    }

                    dsg.commit();
                    return true;
                }

            };

            // Fire off two threads
            Future<Boolean> f1 = executor.submit(callable);
            Future<Boolean> f2 = executor.submit(callable);
            Assert.assertTrue(f1.get(4, TimeUnit.SECONDS));
            Assert.assertTrue(f2.get(4, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public synchronized void dsg_with_lock_concurrency_02() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            final DatasetGraphWithLock dsg = new DatasetGraphWithLock(DatasetGraphFactory.create());

            Callable<Boolean> callable = new Callable<Boolean>() {

                @Override
                public Boolean call() {
                    dsg.begin(ReadWrite.READ);

                    // Hold the lock for a few seconds
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // Ignore error
                    }

                    dsg.commit();
                    return true;
                }

            };

            // Run the callable a bunch of times
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                futures.add(executor.submit(callable));
            }

            // Check all the futures come back OK
            for (Future<Boolean> f : futures) {
                Assert.assertTrue(f.get(3, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }

}
