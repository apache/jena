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

package org.apache.jena.rdfs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.junit.jupiter.api.function.Executable;

/**
 * JUnit test case executable to compare two datasets w.r.t.
 * their result of find(pattern).
 */
public class GraphFindExecutable
    implements Executable
{
    private static PrintStream out = System.err;

    private String       testLabel;
    private Quad         findPattern;
    private DatasetGraph referenceDsg;
    private DatasetGraph testDsg;

    /** Compare expected and actual data as sets instead of lists. */
    private boolean compareAsSet;

    public GraphFindExecutable(String testLabel, Quad findPattern, DatasetGraph referenceDsg, DatasetGraph testDsg, boolean compareAsSet) {
        super();
        this.testLabel    = testLabel;
        this.referenceDsg = referenceDsg;
        this.testDsg = testDsg;
        this.findPattern = findPattern;
        this.compareAsSet = compareAsSet;
    }

    public String getTestLabel() {
        return testLabel;
    }

    public Quad getFindPattern() {
        return findPattern;
    }

    /**
     * Assert that graph.find() returned the same set of quads for the given pattern.
     * Duplicates are ignored.
     */
    @Override
    public void execute() throws Throwable {
        List<Quad> expectedList = Iter.toList(referenceDsg.find(findPattern));
        List<Quad> actualList = Iter.toList(testDsg.find(findPattern));

        if (compareAsSet) {
            Set<Quad> expectedSet = new LinkedHashSet<>(expectedList);
            Set<Quad> actualSet = new LinkedHashSet<>(actualList);
            expectedList = new ArrayList<>(expectedSet);
            actualList = new ArrayList<>(actualSet);
        }

        boolean b = ListUtils.equalsUnordered(expectedList, actualList);
        if ( ! b ) {
            out.println("Fail: find(" + NodeFmtLib.str(findPattern) + ")");
            LibTestRDFS.printDiff(out, expectedList, actualList);
        }

        assertTrue(b,()->getTestLabel());
    }

    @Override
    public String toString() {
        return getTestLabel() + " " + getFindPattern();
    }
}
