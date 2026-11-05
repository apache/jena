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

package org.apache.jena.tdb2.solver.skipscan_3995;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.solver.skipscan.TestOpExecutorTDB2SkipScan;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * Finding 14 - {@code TestOpExecutorTDB2SkipScan:138}.
 *
 * <pre>{@code
 * public static void addResourceObjectsAsSubjects(DatasetGraph referenceDsg) {
 *     DatasetGraph dataDsg = DatasetGraphFactory.create();
 *     dataDsg.addAll(referenceDsg);
 *     ...
 *     extra.forEach(dataDsg::add);      // the local copy, not the argument
 * }
 * }</pre>
 * <p>Every write goes to a fresh local dataset that is dropped on return, so the method - named
 * and documented as an "in-place addition" and called for its effect on the argument - does
 * nothing. The extra lookup shapes it exists to generate are never tested.</p>
 */
public class Test14_AddResourceObjectsAsSubjects {

    @Test
    public void argumentIsModifiedInPlace() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        RDFParser.fromString("""
            PREFIX : <http://example/>
            :s1 :p :o1 .
            :s1 :p :o2 .
            """, Lang.TRIG).parse(dsg);

        long before = count(dsg);
        TestOpExecutorTDB2SkipScan.addResourceObjectsAsSubjects(dsg);
        long after = count(dsg);

        assertTrue(after > before,
                   () -> "addResourceObjectsAsSubjects did not modify its argument"
                       + " (" + before + " quads before, " + after + " after)");

        Node o1 = NodeFactory.createURI("http://example/o1");
        assertTrue(hasQuad(dsg, o1), "The object :o1 was not added as a subject");
    }

    private static long count(DatasetGraph dsg) {
        try ( Stream<Quad> stream = dsg.stream() ) {
            return stream.count();
        }
    }

    private static boolean hasQuad(DatasetGraph dsg, Node subject) {
        try ( Stream<Quad> stream = dsg.stream() ) {
            return stream.anyMatch(q -> subject.equals(q.getSubject())
                                     && RDF.Nodes.type.equals(q.getPredicate())
                                     && RDFS.Nodes.Resource.equals(q.getObject()));
        }
    }
}
