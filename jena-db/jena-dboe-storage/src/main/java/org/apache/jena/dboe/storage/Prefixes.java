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

package org.apache.jena.dboe.storage;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class Prefixes {
    //Distinguished nodes:
    //  Default graph   : Quad.defaultGraphNodeGenerated would have been preferred.
    //     For compatibility reasons, in TDB2, this is the URI <> (empty string).
    //  "whole dataset" : <urn:x-arq:Dataset> (maybe reserved NodeFactory.createLiteral("") or URI <$>

    // Name assigned to the default graph.
    // For backwards compatibility of TDB2 , this an (unresolved) URI <>.
    /** Name assigned to the default graph. */
    public static Node nodeDefaultGraph = NodeFactory.createURI("");

    /** Name for dataset prefixes. */
    public static Node nodeDataset = nodeDefaultGraph; //NodeFactory.createURI("urn:x-arq:Dataset");


}
