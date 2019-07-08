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

package org.apache.jena.tdb2.store;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.tdb2.lib.TupleLib;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

/**
 * TripleTable - a collection of TupleIndexes for 3-tuples together with a node
 * table. Normally, based on 3 indexes (SPO, POS, OSP) but other indexing
 * structures can be configured.
 */

public class TripleTable extends TableBase {
    public TripleTable(TupleIndex[] indexes, NodeTable nodeTable) {
        super(3, indexes, nodeTable);
    }

    /** Add triple */
    public void add(Triple triple) {
        add(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /** Add triple */
    public void add(Node s, Node p, Node o) {
        table.addRow(s, p, o);
    }

    /** Delete a triple */
    public void delete(Triple triple) {
        delete(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /** Delete a triple */
    public void delete(Node s, Node p, Node o) {
        table.deleteRow(s, p, o);
    }

    /** Find matching triples */
    public Iterator<Triple> find(Node s, Node p, Node o) {
        Iterator<Tuple<NodeId>> iter = table.findAsNodeIds(s, p, o);
        if ( iter == null )
            return Iter.nullIterator();
        Iterator<Triple> iter2 = TupleLib.convertToTriples(table.getNodeTable(), iter);
        return iter2;
    }

    /** Clear - does not clear the associated node tuple table */
    public void clearTriples() {
        table.clear();
    }
}
