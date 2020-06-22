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

package org.apache.jena.dboe.storage.prefixes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public abstract class AbstractTestDatasetPrefixesStorage
{
    /** Create a fresh PrefixMapping */
    protected abstract StoragePrefixes create();
    /** Create a fresh view over the same storage as last create() */
    protected abstract StoragePrefixes view();

    protected Node g1 = NodeFactory.createURI("http://example.org/g1");
    protected Node g2 = NodeFactory.createURI("http://example.org/g2");
    protected String pref1  = "pref1";
    protected String pref1a = "pref1:";
    protected String pref2  = "pref2";

    @Test public void dsg_prefixes_01() {
        StoragePrefixes prefixes = create();
        prefixes.add(g1, pref1, "http://example.net/ns#");
    }

    @Test public void dsg_prefixes_02() {
        StoragePrefixes prefixes = create();
        prefixes.add(g1, pref1, "http://example.net/ns#");
        String x1 = prefixes.get(g1, pref1);
        assertEquals("http://example.net/ns#", x1);
        String x2 = prefixes.get(g1, pref1a);
        assertEquals("http://example.net/ns#", x2);
    }

    @Test public void dsg_prefixes_03() {
        StoragePrefixes prefixes = create();
        prefixes.add(g1, pref1, "http://example.net/ns#");
        String x1 = prefixes.get(g2, pref1);
        assertNull(x1);
    }

    @Test public void dsg_prefixes_04() {
        StoragePrefixes prefixes = create();
        prefixes.add(g1, pref1, "http://example.net/ns#");
        prefixes.delete(g1, pref1);
        String x1 = prefixes.get(g1, pref1);
        assertNull(x1);
    }

    // Accessors
    @Test public void dsg_prefixes_11() {
        StoragePrefixes prefixes = create();
        prefixes.add(g1, pref1, "http://example.net/ns#");

        List<Node> x = Iter.toList(prefixes.listGraphNodes());
        assertEquals(1, x.size());

        List<PrefixEntry> y = Iter.toList(prefixes.get(g1));
        assertEquals(1, y.size());
    }

}

