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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Force all actions in a list to run.
 *
 * Usage:
 * <pre>{@code
 * FinallyAll.run(
 *   () -> action1(),
 *   () -> action2(),
 *   () -> actionN()
 * );
 * }</pre>
 *
 * This is more succinct than nested finally blocks such as:
 * <pre>{@code
 * try { action1(); } finally {
 *   try { action2(); } finally {
 *     try { actionM(); } finally {
 *       actionN();
 *     }
 *   }
 * }
 * }</pre>
 *
 */
public class FinallyRunAll
    implements Runnable
{
    protected List<ThrowingRunnable> actions;

    public static FinallyRunAll create() {
        return new FinallyRunAll();
    }

    public FinallyRunAll() {
        this(new ArrayList<>());
    }

    public FinallyRunAll(List<ThrowingRunnable> actions) {
        super();
        this.actions = actions;
    }

    public void addThrowing(ThrowingRunnable action) {
        actions.add(action);
    }

    public void add(Callable<?> callable) {
        addThrowing(() -> { callable.call(); });
    }

    public void add(Runnable runnable) {
        addThrowing(runnable::run);
    }

    @Override
    public void run() {
        runAction(0);
    }

    protected void runAction(int index) {
        if (index < actions.size()) {
            ThrowingRunnable action = actions.get(index);
            try {
                action.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                runAction(index + 1);
            }
        }
    }

    public static void run(ThrowingRunnable ... actions) {
        new FinallyRunAll(Arrays.asList(actions)).run();
    }
}
