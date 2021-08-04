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

package org.apache.jena.sparql.exec;

import java.util.concurrent.TimeUnit;

import org.apache.jena.sparql.util.Context;

/**
 * Aspects of a building a {@link QueryExec} that can be changed before use. This is
 * limited than {@link QueryExecBuilder} and assumes that the query and target for
 * the query have been set.up.
 */
public interface QueryExecMod {

    public default QueryExecMod timeout(long timeout, TimeUnit timeoutUnits) {
        overallTimeout(timeout, timeoutUnits);
        return this;
    }

    public default QueryExecMod timeout(long timeout) {
        return timeout(timeout, TimeUnit.MILLISECONDS);
    }

    public default QueryExecMod timeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        initialTimeout(timeout1, timeUnit1);
        overallTimeout(timeout2, timeUnit2);
        return this;
    }

    public default QueryExecMod timeout(long timeout1, long timeout2) {
        return timeout(timeout1, TimeUnit.MILLISECONDS, timeout2, TimeUnit.MILLISECONDS);
    }

    public QueryExecMod initialTimeout(long timeout, TimeUnit timeUnit);

    public QueryExecMod overallTimeout(long timeout, TimeUnit timeUnit);

    public Context getContext();

    public QueryExec build();
}
