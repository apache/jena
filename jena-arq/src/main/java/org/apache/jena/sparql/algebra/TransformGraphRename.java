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

package org.apache.jena.sparql.algebra;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;

/** Rename quad form alegra */
public class TransformGraphRename extends TransformCopy
{
    private Node oldGraphName;
    private Node newGraphName;

    public TransformGraphRename(Node oldGraphName, Node newGraphName)
    {
        this.oldGraphName = oldGraphName;
        this.newGraphName = newGraphName;
    }

    @Override
    public Op transform(OpGraph opGraph, Op x)
    {
        if ( opGraph.getNode().equals(oldGraphName) )
            opGraph = new OpGraph(newGraphName, x);
        return super.transform(opGraph, x);
    }

    @Override
    public Op transform(OpQuadPattern opQuadPattern)
    {
        if ( opQuadPattern.getGraphNode().equals(oldGraphName) )
            opQuadPattern = new OpQuadPattern(newGraphName, opQuadPattern.getBasicPattern());
        return super.transform(opQuadPattern);
    }
}
