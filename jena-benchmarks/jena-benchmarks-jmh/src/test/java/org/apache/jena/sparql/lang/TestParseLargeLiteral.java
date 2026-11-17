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

package org.apache.jena.sparql.lang;

import java.util.concurrent.TimeUnit;

import org.apache.jena.jmh.JmhDefaultOptions;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sys.JenaSystem;

import org.junit.Assert;
import org.junit.Test;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Parse a query with one large literal, as when a SERVICE request with a geometry
 * literal of about 100MB stalled in the parser. The ARQ parser reads with
 * ARQCharStream, which doubles its buffer.
 * <p>
 * The javacc-generated SimpleCharStream of Jena 5.6.0 grows its buffer by 2048 chars
 * at a time, so the time to read a token grows with the square of its length. It is
 * run up to 100MB, in a separate run with fewer iterations and a longer timeout: one
 * parse of 100MB takes about a quarter of an hour.
 */
public class TestParseLargeLiteral {

    static {
        JenaSystem.init();
        org.apache.shadedJena560.sys.JenaSystem.init();
    }

    private static final int MB = 1024 * 1024;

    @State(Scope.Benchmark)
    public static class QueryCurrent {
        @Param({"1", "2", "10", "100", "250"})
        public int param0_literalSizeMB;

        String queryString;

        @Setup(Level.Trial)
        public void setup() {
            queryString = queryWithLiteral(param0_literalSizeMB * MB);
        }
    }

    @State(Scope.Benchmark)
    public static class QueryJena560 {
        @Param({"1", "2", "10", "100"})
        public int param0_literalSizeMBJena560;

        String queryString;

        @Setup(Level.Trial)
        public void setup() {
            queryString = queryWithLiteral(param0_literalSizeMBJena560 * MB);
        }
    }

    /**
     * A query with a literal of {@code literalLength} chars, made of WKT polygon
     * coordinates.
     */
    static String queryWithLiteral(int literalLength) {
        StringBuilder sb = new StringBuilder(literalLength + 32);
        sb.append("SELECT * { ?s ?p \"");
        int start = sb.length();
        sb.append("POLYGON((");
        while ( sb.length() - start < literalLength )
            sb.append("10.123456 59.123456, ");
        sb.setLength(start + literalLength - 2);
        sb.append("))");
        sb.append("\" }");
        return sb.toString();
    }

    @Benchmark
    public Query parseQuery(QueryCurrent state) {
        return QueryFactory.create(state.queryString, Syntax.syntaxARQ);
    }

    @Benchmark
    public org.apache.shadedJena560.query.Query parseQueryJena560(QueryJena560 state) {
        return org.apache.shadedJena560.query.QueryFactory.create(state.queryString,
                                                                 org.apache.shadedJena560.query.Syntax.syntaxARQ);
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JmhDefaultOptions.getDefaults(this.getClass())
                .exclude("\\.parseQueryJena560$")
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(2)
                .measurementIterations(4)
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);

        var optJena560 = JmhDefaultOptions.getDefaults(this.getClass())
                .exclude("\\.parseQuery$")
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .measurementIterations(2)
                .timeout(TimeValue.minutes(30))
                .build();
        var resultsJena560 = new Runner(optJena560).run();
        Assert.assertNotNull(resultsJena560);
    }
}
