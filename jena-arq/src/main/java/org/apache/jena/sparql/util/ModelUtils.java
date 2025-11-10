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

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.ARQInternalErrorException;

public class ModelUtils
{
    /** Convert a {@link Node} (graph SPI) to an RDFNode (model API), anchored to the model if possible.
     *
     * @param node
     * @param model (may be null)
     * @return RDFNode
     */

    public static RDFNode convertGraphNodeToRDFNode(Node node, Model model) {
        if ( node.isVariable() )
            throw new QueryException("Variable: "+node);

        // Best way.
        if ( model != null )
             return model.asRDFNode(node);

        if ( node.isLiteral() )
            return new LiteralImpl(node, null);

        if ( node.isURI() || node.isBlank() )
            return new ResourceImpl(node, null);

        if ( node.isTripleTerm() )
            return new ResourceImpl(node, null) ;

        throw new ARQInternalErrorException("Unknown node type for node: "+node);
    }

    /** Convert a {@link Node} (graph SPI) to an RDFNode (model API)
     *
     * @param node
     * @return RDFNode
     */
    public static RDFNode convertGraphNodeToRDFNode(Node node) {
        return convertGraphNodeToRDFNode(node, null);
    }
}
