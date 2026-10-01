/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.function.library;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Function that pauses for N milliseconds whenever it is called (for testing)
 */
public class wait extends FunctionBase1 {

    /**
     * The default maximum wait time (5 seconds) used for {@link #MAX_WAIT}
     */
    public static final int DEFAULT_MAX_WAIT = 5_000;
    /**
     * Maximum permitted wait time in milliseconds, if a longer wait is requested then no wait happens
     */
    public static int MAX_WAIT = DEFAULT_MAX_WAIT;

    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        Timeouts.Timeout timeout = Timeouts.extractQueryTimeout(env.getContext());

        int waitMillis = getWaitMillis(args, timeout);
        if (waitMillis > MAX_WAIT) {
            throw new ExprEvalException("Requested wait time too large");
        }
        AtomicBoolean cancelSignal = Context.getCancelSignal(env.getContext());
        if (cancelSignal != null && cancelSignal.get()) {
            throw new ExprEvalException("Cannot wait as query is cancelled");
        }

        Long startTime = getStartTimeEpoch(env.getContext());
        if (startTime != null && timeout.hasOverallTimeout()) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed + waitMillis > timeout.overallTimeoutMillis()) {
                throw new ExprEvalException("Cannot wait as requested wait would exceed query timeout");
            }
        }
        Lib.sleep(waitMillis);
        return NodeValue.TRUE;
    }

    private static Long getStartTimeEpoch(Context context) {
        Long startTime = context.get(ARQConstants.sysCurrentTimeEpoch);
        if (startTime != null) {
            return startTime;
        }
        Node executionTime = context.get(ARQConstants.sysCurrentTime);
        if (executionTime == null) {
            return null;
        }
        startTime = Instant.parse(executionTime.getLiteralLexicalForm()).toEpochMilli();
        context.set(ARQConstants.sysCurrentTimeEpoch, startTime);
        return startTime;
    }

    private static int getWaitMillis(List<NodeValue> args, Timeouts.Timeout timeout) {
        if (args.size() != 1) {
            throw new ExprEvalException("afn:wait() takes a single argument but got " + args.size());
        }
        NodeValue nv = args.getFirst();
        if (!nv.isInteger()) {
            throw new ExprEvalException("Not an integer");
        }
        int waitMillis = nv.getInteger().intValue();
        if (waitMillis <= 0) {
            throw new ExprEvalException("Cannot wait for zero/negative milliseconds");
        }
        if ((timeout.hasInitialTimeout() && waitMillis > timeout.initialTimeoutMillis()) || (timeout.hasOverallTimeout() && waitMillis > timeout.overallTimeoutMillis())) {
            throw new ExprEvalException("Cannot wait longer than the query timeout");
        }
        return waitMillis;
    }

    @Override
    public NodeValue exec(NodeValue v) {
        throw new ExprEvalException("Must supply a FunctionEnv in order to enforce query timeout");
    }
}
