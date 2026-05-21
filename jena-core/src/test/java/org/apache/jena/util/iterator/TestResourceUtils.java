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

package org.apache.jena.util.iterator;

import java.util.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.*;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests on resource utilities (converted from JUnit3 TestResourceUtils)
 */
public class TestResourceUtils {
    public static final String NS = "http://jena.hp.com/test#";

    @Test
    public void testMaximalLowerElements() {
        Model m = ModelFactory.createDefaultModel();

        Resource a = m.createResource(NS + "a");
        Resource b = m.createResource(NS + "b");
        Resource c = m.createResource(NS + "c");
        Resource d = m.createResource(NS + "d");

        b.addProperty(RDFS.subClassOf, a);
        c.addProperty(RDFS.subClassOf, a);
        d.addProperty(RDFS.subClassOf, c);
        d.addProperty(RDFS.subClassOf, a);

        List<Resource> abcd = Arrays.asList(a, b, c, d);
        List<Resource> bcd = Arrays.asList(b, c, d);
        List<Resource> cd = Arrays.asList(c, d);

        assertEquals(1, ResourceUtils.maximalLowerElements(abcd, RDFS.subClassOf, true).size(), "Wrong number of remaining resources");
        assertEquals(a, ResourceUtils.maximalLowerElements(abcd, RDFS.subClassOf, true).iterator().next(), "Result should be a");
        assertEquals(2, ResourceUtils.maximalLowerElements(bcd, RDFS.subClassOf, true).size(), "Wrong number of remaining resources");
        assertEquals(1, ResourceUtils.maximalLowerElements(cd, RDFS.subClassOf, true).size(), "Wrong number of remaining resources");
        assertEquals(c, ResourceUtils.maximalLowerElements(cd, RDFS.subClassOf, true).iterator().next(), "Result should be a");
    }

    @Test
    public void testRenameResource() {
        testRenameResource(ModelFactory.createDefaultModel());
    }

    private void testRenameResource(Model m) {
        Resource a = m.createResource(NS + "a");
        Resource b = m.createResource(NS + "b");
        Resource c = m.createResource(NS + "c");
        Resource d = m.createResource(NS + "d");

        Property p = m.createProperty(NS, "p");
        Property q = m.createProperty(NS, "q");

        a.addProperty(p, b);
        a.addProperty(q, c);
        d.addProperty(p, a);
        d.addProperty(p, b);

        // now rename a to e
        Resource e = ResourceUtils.renameResource(a, NS + "e");

        assertFalse(a.listProperties().hasNext(), "should be no properties of a");
        assertEquals(NS + "a", a.getURI(), "uri of a");
        assertEquals(NS + "e", e.getURI(), "uri of e");

        assertFalse(d.hasProperty(p, a), "d should not have p a");
        assertTrue(d.hasProperty(p, e), "d should have p e");

        assertTrue(e.hasProperty(p, b), "e should have p b");
        assertTrue(e.hasProperty(q, c), "e should have q c");

        assertTrue(d.hasProperty(p, b), "d p b should be unchanged");

        // now rename e to anon
        Resource anon = ResourceUtils.renameResource(e, null);

        assertFalse(e.listProperties().hasNext(), "should be no properties of e");
        assertEquals(NS + "e", e.getURI(), "uri of e");
        assertTrue(anon.isAnon(), "anon");

        assertFalse(d.hasProperty(p, e), "d should not have p e");
        assertTrue(d.hasProperty(p, anon), "d should have p anon");

        assertTrue(anon.hasProperty(p, b), "anon should have p b");
        assertTrue(anon.hasProperty(q, c), "anon should have q c");

        assertTrue(d.hasProperty(p, b), "d p b should be unchanged");

        // reflexive case
        Resource f = m.createResource(NS + "f");
        f.addProperty(p, f);

        Resource f1 = ResourceUtils.renameResource(f, NS + "f1");
        assertFalse(m.listStatements(f, null, (RDFNode) null).hasNext(), "Should be no f statements");
        assertTrue(f1.hasProperty(p, f1), "f1 has p f1");
    }

    @Test
    public void testReachableGraphClosure() {
        Model m0 = ModelFactory.createDefaultModel();
        Resource a = m0.createResource("a");
        Resource b = m0.createResource("b");
        Resource c = m0.createResource("c");
        Resource d = m0.createResource("d");
        Property p = m0.createProperty("p");

        m0.add(a, p, b);
        m0.add(a, p, c);
        m0.add(b, p, b);  // unit loop
        m0.add(b, p, a);  // loop
        m0.add(d, p, a);  // not reachable from a

        Model m1 = ModelFactory.createDefaultModel();
        m1.add(a, p, b);
        m1.add(a, p, c);
        m1.add(b, p, b);
        m1.add(b, p, a);

        assertTrue(m1.isIsomorphicWith(ResourceUtils.reachableClosure(a)), "m1 should be isomorphic with the reachable sub-graph from a");
    }

    @Test
    public void testRemoveEquiv() {
        Model m = ModelFactory.createDefaultModel();

        Resource a = m.createResource(NS + "a");
        Resource b = m.createResource(NS + "b");
        Resource c = m.createResource(NS + "c");
        Resource d = m.createResource(NS + "d");
        Resource e = m.createResource(NS + "e");

        b.addProperty(RDFS.subClassOf, a);
        a.addProperty(RDFS.subClassOf, b);  // a,b are equivalent
        d.addProperty(RDFS.subClassOf, e);
        e.addProperty(RDFS.subClassOf, d);  // d,e are equivalent

        // reflexive relations - would be inferred by inf engine
        a.addProperty(RDFS.subClassOf, a);
        b.addProperty(RDFS.subClassOf, b);
        c.addProperty(RDFS.subClassOf, c);
        d.addProperty(RDFS.subClassOf, d);
        e.addProperty(RDFS.subClassOf, e);

        List<Resource> abcde = Arrays.asList(a, b, c, d, e);
        List<Resource> ab = Arrays.asList(a, b);
        List<Resource> cde = Arrays.asList(c, d, e);
        List<Resource> abde = Arrays.asList(a, b, d, e);
        List<Resource> de = Arrays.asList(d, e);

        List<Resource> in = new ArrayList<>();
        in.addAll(abcde);
        List<Resource> out = null;
        assertTrue(in.equals(abcde));
        assertFalse(in.equals(abde));
        assertFalse(in.equals(cde));
        assertNull(out);

        out = ResourceUtils.removeEquiv(in, RDFS.subClassOf, a);

        assertFalse(in.equals(abcde));
        assertFalse(in.equals(abde));
        assertTrue(in.equals(cde));
        assertNotNull(out);
        assertEquals(out, ab);

        out = ResourceUtils.removeEquiv(in, RDFS.subClassOf, e);

        assertFalse(in.equals(abcde));
        assertFalse(in.equals(abde));
        assertTrue(in.equals(Collections.singletonList(c)));
        assertNotNull(out);
        assertEquals(out, de);
    }

    @Test
    public void testPartition() {
        Model m = ModelFactory.createDefaultModel();

        Resource a = m.createResource(NS + "a");
        Resource b = m.createResource(NS + "b");
        Resource c = m.createResource(NS + "c");
        Resource d = m.createResource(NS + "d");
        Resource e = m.createResource(NS + "e");

        b.addProperty(RDFS.subClassOf, a);
        a.addProperty(RDFS.subClassOf, b);  // a,b are equivalent
        d.addProperty(RDFS.subClassOf, e);
        e.addProperty(RDFS.subClassOf, d);  // d,e are equivalent

        // reflexive relations - would be inferred by inf engine
        a.addProperty(RDFS.subClassOf, a);
        b.addProperty(RDFS.subClassOf, b);
        c.addProperty(RDFS.subClassOf, c);
        d.addProperty(RDFS.subClassOf, d);
        e.addProperty(RDFS.subClassOf, e);

        List<Resource> abcde = Arrays.asList(new Resource[]{a, b, c, d, e});
        List<Resource> ab = Arrays.asList(new Resource[]{b, a});
        List<Resource> cc = Arrays.asList(new Resource[]{c});
        List<Resource> de = Arrays.asList(new Resource[]{e, d});

        List<List<Resource>> partition = ResourceUtils.partition(abcde, RDFS.subClassOf);
        assertEquals(3, partition.size(), "Should be 3 partitions");
        assertEquals(ab, partition.get(0), "First parition should be (a,b)");
        assertEquals(cc, partition.get(1), "First parition should be (c)");
        assertEquals(de, partition.get(2), "First parition should be (d,e)");
    }

}
