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

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorResourceClosing ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Closeable ;

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
        
        if ( sNode.isLiteral() || sNode.isVariable() )
            return null ;
        
        if ( ! pNode.isURI() )  // Not variable, literal or blank.
            return null ;

        if ( oNode.isVariable() )
            return null ;
        
        return model.asStatement(t) ; 
//        RDFNode s = convertGraphNodeToRDFNode(sNode, model) ;
//        RDFNode p = convertGraphNodeToRDFNode(pNode, model) ;
//        if ( p instanceof Resource )
//            p = model.createProperty(((Resource)p).getURI()) ;
//        RDFNode o = convertGraphNodeToRDFNode(oNode, model) ;
//        
//        Statement stmt = model.createStatement((Resource)s, (Property)p, o) ;
//        return stmt ;
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
        return new IteratorResourceClosing<Triple>(Iter.map(it, new Transform<Statement,Triple>()
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
