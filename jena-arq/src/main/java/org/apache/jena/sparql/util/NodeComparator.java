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

package org.apache.jena.sparql.util;

import java.util.Comparator ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * This is not sorting by value. See {@link NodeValue#compare} and {@link NodeValue#compareAlways}.
 * @see NodeCmp#compareRDFTerms
 * @deprecated This can be replaced by {@code (n1,n2)->NodeCmp.compareRDFTerms(n1, n2)}.
 */
@Deprecated
public class NodeComparator implements Comparator<Node>
{
    @Override
    public int compare(Node o1, Node o2) {
        return NodeCmp.compareRDFTerms(o1, o2);
    }
}

