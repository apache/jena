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

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.QueryException ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Statement ;
import org.apache.jena.rdf.model.StmtIterator ;
import org.apache.jena.rdf.model.impl.LiteralImpl ;
import org.apache.jena.rdf.model.impl.ResourceImpl ;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.util.ModelCollector;
import org.apache.jena.util.iterator.ClosableIterator ;

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
            throw new QueryException("Variable: "+node) ;

        // Best way.
        if ( model != null )
             return model.asRDFNode(node) ;

        if ( node.isLiteral() )
            return new LiteralImpl(node, null) ;

        if ( node.isURI() || node.isBlank() )
            return new ResourceImpl(node, null) ;

        if ( node.isNodeTriple() )
            return new ResourceImpl(node, null) ;

        throw new ARQInternalErrorException("Unknown node type for node: "+node) ;
    }

    /** Convert a {@link Node} (graph SPI) to an RDFNode (model API)
     *
     * @param node
     * @return RDFNode
     */
    public static RDFNode convertGraphNodeToRDFNode(Node node) {
        return convertGraphNodeToRDFNode(node, null);
    }

    public static Statement tripleToStatement(Model model, Triple t)
    {
        if ( model == null )
            throw new ARQInternalErrorException("Attempt to create statement with null model") ;

        Node sNode = t.getSubject() ;
        Node pNode = t.getPredicate() ;
        Node oNode = t.getObject() ;

        if (!isValidAsStatement(sNode, pNode, oNode)) return null;

        return model.asStatement(t) ;
    }

    /**
     * Determines whether a valid Statement can be formed from the given Subject, Predicate and Object
     * <p>
     * This function reflects the fact that the {@link Triple} API is flexible in allowing any Node type in any position (including non-RDF node types like Variable)
     * and as such not all Triples can be safely converted into Statements
     * </p>
     * @param s Subject
     * @param p Predicate
     * @param o Object
     * @return True if a valid Statement can be formed
     */
    public static boolean isValidAsStatement(Node s, Node p, Node o)
    {
        if ( s.isLiteral() || s.isVariable() )
            return false ;

        if ( ! p.isURI() )  // Not variable, literal or blank.
            return false ;

        if ( o.isVariable() )
            return false ;

        return true;
    }

    public static StmtIterator triplesToStatements(final Iterator<Triple> it, final Model refModel) {
        return new StmtIteratorImpl(Iter.map(it, refModel::asStatement)) {
            // Make sure to close the incoming iterator
            @Override
            public void close() {
                if ( it instanceof ClosableIterator<? > ) {
                    ((ClosableIterator<? >)it).close();
                } else {
                    Iter.close(it);
                }
            }
        };
    }

    public static ModelCollector intersectCollector() {
        return new ModelCollector.IntersectionModelCollector();
    }

    public static ModelCollector unionCollector() {
        return new ModelCollector.UnionModelCollector();
    }

    public static Iterator<Triple> statementsToTriples(final Iterator<Statement> it) {
        return Iter.onClose(Iter.map(it, Statement::asTriple), ()->Iter.close(it));
    }
}
