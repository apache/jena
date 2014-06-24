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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.FrontsTriple ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.AlreadyReifiedException ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Map1 ;

/**
    This class impedance-matches the reification requests of Model[Com] to the operations
    supplied by it's Graph's Reifier.
*/
public class ModelReifier
    {
    private ModelCom model;
    
    /**
        DEVEL. setting this _true_ means that nodes that reify statements
        will drag their reification quads into other nodes when they are
        added to them inside statements.
    */
    private static boolean copyingReifications = false;
    
    /**
        establish the internal state of this ModelReifier: the associated
        Model[Com] and its graph's Reifier.
    */
    public ModelReifier( ModelCom model )
        {
        this.model = model; 
        }

    /**
        Answer a fresh reification of a statement associated with a fresh bnode.
        @param s a Statement to reifiy
        @return a reified statement object who's name is a new bnode
    */
    public ReifiedStatement createReifiedStatement( Statement s )
        { return createReifiedStatement( null, s ); }

    /**
        Answer a reification of  a statement with a given uri. If that uri 
        already reifies a distinct Statement, throw an AlreadyReifiedException.
        @param uri the URI of the resource which will reify <code>s</code>
        @param s the Statement to reify
        @return a reified statement object associating <code>uri</code> with <code>s</code>.
        @throws AlreadyReifiedException if uri already reifies something else. 
    */
    public ReifiedStatement createReifiedStatement( String uri, Statement s )
        { return ReifiedStatementImpl.create( model, uri, s ); }
   
    /**
        Find any existing reified statement that reifies a givem statement. If there isn't one,
        create one.
        @param s a Statement for which to find [or create] a reification
        @return a reification for s, re-using an existing one if possible
    */
    public Resource getAnyReifiedStatement( Statement s ) 
        {
        RSIterator it = listReifiedStatements( s );
        if (it.hasNext())
            try { return it.nextRS(); } finally { it.close(); }
        else
            return createReifiedStatement( s );
        }
         
    /**
        Answer true iff a given statement is reified in this model
        @param s the statement for which a reification is sought
        @return true iff s has a reification in this model
    */
    public boolean isReified( FrontsTriple s ) 
        { return ReifierStd.hasTriple(model.getGraph(), s.asTriple() ); }

    /**
        Remove all the reifications of a given statement in this model, whatever
        their associated resources.
        @param s the statement whose reifications are to be removed
    */
    public void removeAllReifications( FrontsTriple s ) 
        { ReifierStd.remove( model.getGraph(), s.asTriple() ); }
      
    /**
        Remove a given reification from this model. Other reifications of the same statement
        are untouched.
        @param rs the reified statement to be removed
    */  
    public void removeReification( ReifiedStatement rs )
        { ReifierStd.remove( model.getGraph(), rs.asNode(), rs.getStatement().asTriple() ); }
        
    /**
        Answer an iterator that iterates over all the reified statements
        in this model.
        @return an iterator over all the reifications of the model.
    */
    public RSIterator listReifiedStatements()
        { return new RSIteratorImpl( findReifiedStatements() ); }
   
    /**
        Answer an iterator that iterates over all the reified statements in
        this model that reify a given statement.
        @param s the statement whose reifications are sought.
        @return an iterator over the reifications of s.
    */
    public RSIterator listReifiedStatements( FrontsTriple s )
        { return new RSIteratorImpl( findReifiedStatements( s.asTriple() ) ); }      
      
    /**
        the triple (s, p, o) has been asserted into the model. Any reified statements
        among them need to be added to this model.
    */ 
    public void noteIfReified( RDFNode s, RDFNode p, RDFNode o )
        {
        if (copyingReifications)
            {
            noteIfReified( s );
            noteIfReified( p );
            noteIfReified( o );
            }
        } 
        
    /**
        If _n_ is a ReifiedStatement, create a local copy of it, which
        will force the underlying reifier to take note of the mapping.
    */
    private void noteIfReified( RDFNode n )
        {
        if (n.canAs( ReifiedStatement.class ))
            {
            ReifiedStatement rs = n.as( ReifiedStatement.class );
            createReifiedStatement( rs.getURI(), rs.getStatement() );
            }
        }
        
    /**
        A mapper that maps modes to their corresponding ReifiedStatement objects. This
        cannot be static: getRS cannot be static, because the mapping is model-specific.
    */
    protected final Map1<Node, ReifiedStatement> mapToRS = new Map1<Node, ReifiedStatement>()
        {
        @Override
        public ReifiedStatement map1( Node node ) { return getRS( node ); }
        };

    private ExtendedIterator<ReifiedStatement> findReifiedStatements()
        { return ReifierStd .allNodes(model.getGraph()) .mapWith( mapToRS ); }

    private ExtendedIterator<ReifiedStatement> findReifiedStatements( Triple t )
        { return ReifierStd .allNodes(model.getGraph(), t ) .mapWith( mapToRS ); }
        
    /**
        Answer a ReifiedStatement that is based on the given node. 
        @param n the node which represents the reification (and is bound to some triple t)
        @return a ReifiedStatement associating the resource of n with the statement of t.    
    */
    private ReifiedStatement getRS( Node n )
        { 
        return ReifiedStatementImpl.createExistingReifiedStatement( model, n );
        }              
    }
