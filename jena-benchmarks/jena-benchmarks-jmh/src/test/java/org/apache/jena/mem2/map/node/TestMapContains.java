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

package org.apache.jena.mem2.map.node;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Releases;
import org.apache.jena.mem2.helper.JMHDefaultOptions;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.HashMap;
import java.util.List;


@State(Scope.Benchmark)
public class TestMapContains {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "HashSet",
            "HashCommonNodeSet",
            "FastHashNodeSet"
    })
    public String param1_SetImplementation;
    java.util.function.Supplier<Boolean> setContainsSubjects;
    java.util.function.Supplier<Boolean> setContainsPredicates;
    java.util.function.Supplier<Boolean> setContainsObjects;
    private List<Triple> triplesToFind;
    private HashMap<Node, Object> subjectHashMap;
    private HashMap<Node, Object> predicateHashMap;
    private HashMap<Node, Object> objectHashMap;
    private HashCommonNodeMap subjectHashCommonNodeMap;
    private HashCommonNodeMap predicateHashCommonNodeMap;
    private HashCommonNodeMap objectHashCommonNodeMap;
    private FastHashNodeMap subjectFastHashNodeSet;
    private FastHashNodeMap predicateFastHashNodeSet;
    private FastHashNodeMap objectFastHashNodeSet;


    @Benchmark
    public boolean setContainsSubjects() {
        return setContainsSubjects.get();
    }

    @Benchmark
    public boolean setContainsPredicates() {
        return setContainsPredicates.get();
    }

    @Benchmark
    public boolean setContainsObjects() {
        return setContainsObjects.get();
    }

    private boolean hashMapContainsSubjects() {
        var found = false;
        for (var t : triplesToFind) {
            found = subjectHashMap.containsKey(t.getSubject());
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean hashMapContainsPredicates() {
        var found = false;
        for (var t : triplesToFind) {
            found = predicateHashMap.containsKey(t.getPredicate());
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean hashMapContainsObjects() {
        var found = false;
        for (var t : triplesToFind) {
            found = objectHashMap.containsKey(t.getObject());
            Assert.assertTrue(found);
        }
        return found;
    }


    private boolean HashCommonNodeMapContainsSubjects() {
        var found = false;
        for (var t : triplesToFind) {
            found = subjectHashCommonNodeMap.containsKey(t.getSubject());
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean HashCommonNodeMapContainsPredicates() {
        var found = false;
        for (var t : triplesToFind) {
            found = predicateHashCommonNodeMap.containsKey(t.getPredicate());
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean HashCommonNodeMapContainsObjects() {
        var found = false;
        for (var t : triplesToFind) {
            found = objectHashCommonNodeMap.containsKey(t.getObject());
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean FastHashNodeMapContainsSubjects() {
        var found = false;
        for (var t : triplesToFind) {
            found = subjectFastHashNodeSet.containsKey(t.getSubject());
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean FastHashNodeMapContainsPredicates() {
        var found = false;
        for (var t : triplesToFind) {
            found = predicateFastHashNodeSet.containsKey(t.getPredicate());
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean FastHashNodeMapContainsObjects() {
        var found = false;
        for (var t : triplesToFind) {
            found = objectFastHashNodeSet.containsKey(t.getObject());
            Assert.assertTrue(found);
        }
        return found;
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        var triples = Releases.current.readTriples(param0_GraphUri);
        this.triplesToFind = Releases.current.cloneTriples(triples);
        switch (param1_SetImplementation) {
            case "HashSet":
                this.subjectHashMap = new HashMap<>();
                this.predicateHashMap = new HashMap<>();
                this.objectHashMap = new HashMap<>();
                triples.forEach(t -> {
                    subjectHashMap.put(t.getSubject(), null);
                    predicateHashMap.put(t.getPredicate(), null);
                    objectHashMap.put(t.getObject(), null);
                });
                this.setContainsSubjects = this::hashMapContainsSubjects;
                this.setContainsPredicates = this::hashMapContainsPredicates;
                this.setContainsObjects = this::hashMapContainsObjects;
                break;
            case "HashCommonNodeSet":
                this.subjectHashCommonNodeMap = new HashCommonNodeMap();
                this.predicateHashCommonNodeMap = new HashCommonNodeMap();
                this.objectHashCommonNodeMap = new HashCommonNodeMap();
                triples.forEach(t -> {
                    subjectHashCommonNodeMap.tryPut(t.getSubject(), null);
                    predicateHashCommonNodeMap.tryPut(t.getPredicate(), null);
                    objectHashCommonNodeMap.tryPut(t.getObject(), null);
                });
                this.setContainsSubjects = this::HashCommonNodeMapContainsSubjects;
                this.setContainsPredicates = this::HashCommonNodeMapContainsPredicates;
                this.setContainsObjects = this::HashCommonNodeMapContainsObjects;
                break;
            case "FastHashNodeSet":
                this.subjectFastHashNodeSet = new FastHashNodeMap();
                this.predicateFastHashNodeSet = new FastHashNodeMap();
                this.objectFastHashNodeSet = new FastHashNodeMap();
                triples.forEach(t -> {
                    subjectFastHashNodeSet.tryPut(t.getSubject(), null);
                    predicateFastHashNodeSet.tryPut(t.getPredicate(), null);
                    objectFastHashNodeSet.tryPut(t.getObject(), null);
                });
                this.setContainsSubjects = this::FastHashNodeMapContainsSubjects;
                this.setContainsPredicates = this::FastHashNodeMapContainsPredicates;
                this.setContainsObjects = this::FastHashNodeMapContainsObjects;
                break;
            default:
                throw new IllegalArgumentException("Unknown set implementation: " + param1_SetImplementation);
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JMHDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}
