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
package org.apache.jena.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass
@MethodSource("generateTestParams")
public class TestQueryCloningEssentials {

    public static Query slowClone(Query query) {
        String qs = query.toString();
        return QueryFactory.create(qs, query.getSyntax()) ;
    }

    /**
     * Assert whether cloning using the old print-parse approach
     * yields the same result as the one using the syntax transform
     * machinery.
     *
     * @param query
     */
    public static Query checkedClone(Query query) {
        Query expected = slowClone(query);
        Query actual = query.cloneQuery();
        assertEquals(query, actual);
        assertEquals(expected, actual);

        // Check that the cloned query is OK.
        Query again = slowClone(actual);
        assertEquals(query, again);

        return actual;
    }

    protected Path queryFile;
    protected Query query;

    public TestQueryCloningEssentials(Path queryFile, Query query) {
        this.queryFile = queryFile;
        this.query = query;
    }

    private static void silenceWarnings(Runnable action) {
        boolean bVerboseWarnings = NodeValue.VerboseWarnings;
        boolean bWarnOnUnknownFunction = E_Function.WarnOnUnknownFunction;
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
        try {
            action.run();
        } finally {
            NodeValue.VerboseWarnings = bVerboseWarnings;
            E_Function.WarnOnUnknownFunction = bWarnOnUnknownFunction;
        }
    }

    @Test
    public void runTest() {
        silenceWarnings(()->checkedClone(query));
    }

    static Stream<Arguments> generateTestParams() throws IOException {
        List<String> exclusions = List.of(/* no exclusions as all test cases work */);

        Path startPath = Path.of("./testing/ARQ").toAbsolutePath().normalize();
        PathMatcher pathMatcher = startPath.getFileSystem().getPathMatcher("glob:**/*.rq");

        List<Arguments> testParams = new ArrayList<>();
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(pathMatcher.matches(file)) {
                    boolean isExcluded = exclusions.stream()
                            .anyMatch(suffix -> file.toString().endsWith(suffix));

                    if(!isExcluded) {
                        String queryStr = Files.lines(file).collect(Collectors.joining("\n"));
                        try {
                            silenceWarnings(()->{
                                Query query = QueryFactory.create(queryStr);
                                Arguments args = Arguments.of(file, query);
                                testParams.add(args);
                            });
                        } catch(Exception e) {
                            // Silently ignore queries that fail to parse
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return testParams.stream();
    }
}
