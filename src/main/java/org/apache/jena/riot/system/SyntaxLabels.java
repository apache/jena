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

package org.apache.jena.riot.system;

import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.out.NodeToLabel ;

/** Factory for default policies for syntax labels to and from nodes */  
public class SyntaxLabels
{
    /** Default setup - scope by document, relabel BNodes ids to short forms */
    static public NodeToLabel createNodeToLabel() { return NodeToLabel.createScopeByDocument() ; }
    static public LabelToNode createLabelToNode() { return LabelToNode.createScopeByDocument() ; }
}
