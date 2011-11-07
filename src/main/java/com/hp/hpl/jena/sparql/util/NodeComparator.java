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

package com.hp.hpl.jena.sparql.util;

import java.util.Comparator ;

import com.hp.hpl.jena.graph.Node ;

public class NodeComparator implements Comparator<Node>
{
    @Override
    public int compare(Node o1, Node o2)
    {
        return NodeUtils.compareRDFTerms(o1, o2);
        //return NodeValue.compareAlways(NodeValue.makeNode(o1), NodeValue.makeNode(o2));
    }
}

