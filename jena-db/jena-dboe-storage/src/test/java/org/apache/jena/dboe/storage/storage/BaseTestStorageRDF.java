/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.dboe.storage.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Before;
import org.junit.Test;

public abstract class BaseTestStorageRDF {

    private StorageRDF storage;

    protected abstract StorageRDF create();
    @Before public void before() {
        this.storage = create();
    }

    static Triple t1 = SSE.parseTriple("(:s :p :o1)");
    static Triple t2 = SSE.parseTriple("(:s :p :o2)");
    static Quad q11 = SSE.parseQuad("(:g1 :s :p :o1)");
    static Quad q12 = SSE.parseQuad("(:g1 :s :p :o2)");
    static Quad q21 = SSE.parseQuad("(:g2 :s :p :o1)");
    static Quad q22 = SSE.parseQuad("(:g2 :s :p :o2)");

    @Test public void storage_empty_01() {
        boolean b1 = isEmpty(storage.find(null, null, null));
        assertTrue("triples", b1);
        boolean b2 = isEmpty(storage.find(null, null, null, null));
        assertTrue("quads", b2);
    }

    @Test public void storage_empty_02() {
        boolean b1 = isEmpty(storage.find(null, null, null));
        assertTrue("findDftGraph", b1);
        boolean b2 = isEmpty(storage.findUnionGraph(null, null, null));
        assertTrue("findUnionGraph", b2);
    }

    @Test public void storage_triples_01() {
        storage.add(t1);
        assertTrue(storage.contains(t1));
        long x1 = count(storage.find(null, null, null));
        assertEquals(1, x1);
        long x2 = count(storage.find(null, null, null, null));
        assertEquals(0, x2);
        long x3 = count(storage.findUnionGraph(null, null, null));
        assertEquals(0, x3);
    }

    @Test public void storage_quads_01() {
        storage.add(q11);
        assertTrue(storage.contains(q11));
        long x1 = count(storage.find(null, null, null));
        assertEquals(0, x1);
        long x2 = count(storage.find(null, null, null, null));
        assertEquals(1, x2);
        long x3 = count(storage.findUnionGraph(null, null, null));
        assertEquals(1, x3);
    }

    @Test public void storage_empty_01_stream() {
        boolean b1 = isEmpty(storage.stream(null, null, null));
        assertTrue("triples", b1);
        boolean b2 = isEmpty(storage.stream(null, null, null, null));
        assertTrue("quads", b2);
    }

    @Test public void storage_empty_02_stream() {
        boolean b1 = isEmpty(storage.stream(null, null, null));
        assertTrue("findDftGraph", b1);
        boolean b2 = isEmpty(storage.streamUnionGraph(null, null, null));
        assertTrue("findUnionGraph", b2);
    }

    @Test public void storage_triples_01_stream() {
        storage.add(t1);
        assertTrue(storage.contains(t1));
        long x1 = count(storage.stream(null, null, null));
        assertEquals(1, x1);
        long x2 = count(storage.stream(null, null, null, null));
        assertEquals(0, x2);
        long x3 = count(storage.streamUnionGraph(null, null, null));
        assertEquals(0, x3);
    }

    @Test public void storage_quads_01_stream() {
        storage.add(q11);
        assertTrue(storage.contains(q11));
        long x1 = count(storage.stream(null, null, null));
        assertEquals(0, x1);
        long x2 = count(storage.stream(null, null, null, null));
        assertEquals(1, x2);
        long x3 = count(storage.streamUnionGraph(null, null, null));
        assertEquals(1, x3);
    }

    /** <b>Destructively</b> test whether a stream is empty */
    private boolean isEmpty(Stream<?> stream) { return ! stream.findAny().isPresent(); /*isEmpty at java11 */ }

    /** <b>Non-destructively</b> test whether an iterator is empty */
    private boolean isEmpty(Iterator<?> iterator) { return !iterator.hasNext(); }

    private long count(Stream<?> stream) { return stream.count(); }
    private long count(Iterator<?> iterator) { return Iter.count(iterator); }

}
