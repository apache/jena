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
package org.apache.jena.query;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestQueryCloningEssentials {

    public static Query slowClone(Query query) {
        String qs = query.toString();
        return QueryFactory.create(qs, query.getSyntax()) ;
    }

    /**
     * Assert whether cloning using the old print-parse approach
     * yields the same result as the one using the syntax transform
     * machinery
     *
     * @param query
     */
    public static Query checkedClone(Query query) {
        Query expected = slowClone(query);
        Query actual = query.cloneQuery();

        Assert.assertEquals(expected, actual);;
        return actual;
    }

    protected Path queryFile;
    protected Query query;

    public TestQueryCloningEssentials(Path queryFile, Query query) {
        this.queryFile = queryFile;
        this.query = query;
    }

    @Test
    public void runTest() {
        checkedClone(query);
    }

    @Parameters(name = "Query.clone {0}")
    public static Collection<Object[]> generateTestParams() throws Exception
    {
        Path startPath = Paths.get("./testing").toAbsolutePath().normalize();
        PathMatcher pathMatcher = startPath.getFileSystem().getPathMatcher("glob:**/*.rq");

        List<Object[]> testParams = new ArrayList<>();
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(pathMatcher.matches(file)) {
                    String queryStr = Files.lines(file).collect(Collectors.joining("\n"));
                    try {
                        Query query = QueryFactory.create(queryStr);

                        testParams.add(new Object[] {file, query});
                    } catch(Exception e) {
                        // Silently ignore queries that fail to parse
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return testParams;
    }


}
