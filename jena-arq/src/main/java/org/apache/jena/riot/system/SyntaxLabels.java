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

/**
 * Factory for default policies for syntax labels to and from nodes.
 * For label to node (parsing) we use a scalable hashing scheme (MD5 of a seed and the label)
 * <p>
 * For node to label (pretty labels output), we use a unique tracking scheme.
 * Fully scalable writers use different polices and don't have short, pretty bNode labels.
 * <p>
 * These should be used pairs:
 * <pre>
 *     createNodeToLabel , createLabelToNode
 *     createNodeToLabelRT , createLabelToNodeRT
 *     createNodeToLabelAsGiven , createLabelToNodeAsGiven
 * </pre>
 * "AsGiven" assumes that the label is valid syntax for the usage, no checking.
 * <br/>
 * "RT" encodes the label into characters 0-9,A-Z.
 *
 * @see NodeToLabel
 * @see LabelToNode
 */
public class SyntaxLabels
{
    /** Default setup - scope by document, relabel BNodes ids to short forms */
    static public NodeToLabel createNodeToLabel() { return NodeToLabel.createScopeByDocument() ; }
    /** Default setup - scope by document, relabel BNodes ids to short forms */
    static public LabelToNode createLabelToNode() { return LabelToNode.createScopeByDocumentHash() ; }

    /** Round-trip setup */
    static public NodeToLabel createNodeToLabelRT() { return NodeToLabel.createBNodeByLabelEncoded() ; }
    /** Round-trip setup */
    static public LabelToNode createLabelToNodeRT() { return LabelToNode.createUseLabelEncoded() ; }

    /** Raw string label setup */
    static public NodeToLabel createNodeToLabelAsGiven() { return NodeToLabel.createBNodeByLabelAsGiven(); }
    /** Raw string label setup */
    static public LabelToNode createLabelToNodeAsGiven() { return LabelToNode.createUseLabelAsGiven(); }
}
