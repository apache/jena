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

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorResourceClosing ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Closeable ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl ;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl ;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;


public class ModelUtils
{
    public static RDFNode convertGraphNodeToRDFNode(Node n, Model model)
    {
        if ( n.isVariable() )
            throw new QueryException("Variable: "+n) ;

        // Best way.
        if ( model != null )
             return model.asRDFNode(n) ;
        
        if ( n.isLiteral() )
            return new LiteralImpl(n, null) ;
                
        if ( n.isURI() || n.isBlank() )
            return new ResourceImpl(n, null) ;
        
        throw new ARQInternalErrorException("Unknown node type for node: "+n) ;
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
    
    
    public static StmtIterator triplesToStatements(final Iterator<Triple> it, final Model refModel)
    {
        return new StmtIteratorImpl(Iter.map(it, new Transform<Triple,Statement>()
        {
            @Override
            public Statement convert(Triple item)
            {
                return refModel.asStatement(item);
            }
        }))
        {
            // Make sure to close the incoming iterator
            @Override
            public void close()
            {
                if (it instanceof ClosableIterator<?>)
                {
                    ((ClosableIterator<?>)it).close();
                }
                else
                {
                    Iter.close(it);
                }
            }
        };
    }
    
    public static Iterator<Triple> statementsToTriples(final Iterator<Statement> it)
    {
        return new IteratorResourceClosing<>(Iter.map(it, new Transform<Statement,Triple>()
        {
            @Override
            public Triple convert(Statement item)
            {
                return item.asTriple();
            }
        }),
        new Closeable()
        {
            @Override
            public void close()
            {
                if (it instanceof ClosableIterator<?>)
                {
                    ((ClosableIterator<?>)it).close();
                }
                else
                {
                    Iter.close(it);
                }
            }
        });
    }
 
}
