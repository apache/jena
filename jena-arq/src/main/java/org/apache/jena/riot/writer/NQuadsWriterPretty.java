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

package org.apache.jena.riot.writer;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.riot.out.NodeToLabel;

/**
 * The only prettiness is that blank nodes are written with short labels.
 * <p>
 * This means the writer has long-term internal state and may not write very large
 * N-triple streams which have very large numbers of blank nodes.
 */
public class NQuadsWriterPretty extends NQuadsWriter {
    @Override
    protected NodeFormatter createNodeFormatter() {
        NodeFormatter nodeFmt = new NodeFormatterNT() {
            private final NodeToLabel nodeToLabel = NodeToLabel.createScopeByDocument();
            @Override
            public void formatBNode(AWriter w, Node blankNode) {
                String x = nodeToLabel.get(null, blankNode);
                w.print(x);
            }
        };
        return nodeFmt;
    }
}