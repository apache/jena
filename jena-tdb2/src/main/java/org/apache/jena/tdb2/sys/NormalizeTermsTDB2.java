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

import org.apache.jena.graph.Node;
import org.apache.jena.riot.process.StreamRDFApply;
import org.apache.jena.riot.process.normalize.NormalizeRDFTerms;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdInline;

/**
 * A variation on {@link NormalizeRDFTerms} that attempts to generate TDB2 {@link NodeIdInline inline} NodeIds,
 * then convert back to {@link Node Nodes}.
 * This is used to ensure that TDB2 Node Inlining is compatible with {@link NormalizeRDFTerms}
 */
public class NormalizeTermsTDB2 {
    // Via TDB2 Node inlining.
    public static Node normalizeTDB2(Node node) {
        NodeId x = NodeIdInline.inline(node);
        if ( x == null )
            // No inline.
            return NormalizeRDFTerms.get().normalize(node);
        Node inlined = NodeIdInline.extract(x);
        return inlined;
    }

    static class StreamNormalizedTDB2 extends StreamRDFApply {

        public StreamNormalizedTDB2(StreamRDF other) {
            super(other,
                  NormalizeTermsTDB2::normalizeTDB2,
                  NormalizeTermsTDB2::normalizeTDB2,
                  NormalizeTermsTDB2::normalizeTDB2,
                  NormalizeTermsTDB2::normalizeTDB2);
        }
    }

    /**
     * Return a {@link StreamRDF} that applies
     */
    public static StreamRDF stream(StreamRDF base) {
        return new StreamNormalizedTDB2(base);
    }
}
