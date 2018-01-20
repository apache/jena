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

package org.apache.jena.tdb2.sys;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.system.Txn;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/** Copy operations of any {@link DatasetGraph} */
public class CopyDSG {

    public static void copy(DatasetGraph dsgSrc, DatasetGraph dsgDst) {
        Txn.executeRead(dsgSrc, ()->{
            Txn.executeWrite(dsgDst, () -> {
                Iterator<Quad> iter = dsgSrc.find();
                iter.forEachRemaining(dsgDst::add);
                copyPrefixes(dsgSrc, dsgDst);
            });
        });
    }

    public static void copyPrefixes(DatasetGraph dsgSrc, DatasetGraph dsgDst) {
        List<Node> graphNames = Iter.toList(dsgSrc.listGraphNodes());
        copyPrefixes(dsgSrc.getDefaultGraph(), dsgDst.getDefaultGraph());
        graphNames.forEach((gn)->copyPrefixes(dsgSrc.getGraph(gn), dsgDst.getGraph(gn)));
    }

    public static void copyPrefixes(Graph srcGraph, Graph dstGraph) {
        dstGraph.getPrefixMapping().setNsPrefixes(srcGraph.getPrefixMapping());
    }
}
