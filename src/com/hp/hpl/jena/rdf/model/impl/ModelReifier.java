/*
	(c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
	[see end of file]
	$Id: ModelReifier.java,v 1.1 2003-03-26 11:54:44 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.enhanced.*;

public class ModelReifier
    {
    private ModelCom model;
    private Reifier reifier;
    
    /**
        DEVEL. setting this _true_ means that nodes that reify statements
        will drag their reification quads into other modes when they are
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
        this.reifier = model.asGraph().getReifier();
        }
        
    /**
        create a fresh reification of _s_ based on a fresh bnode.
    */
    public ReifiedStatement createReifiedStatement( Statement s )
        { return createReifiedStatement( null, s ); }

    /**
        create a reification of _s_ with the given _uri_. If that _uri_ 
        already reifies a distinct Statement, throw an AlreadyReifiedException
    */
    public ReifiedStatement createReifiedStatement( String uri, Statement s )
        { return ReifiedStatementImpl.create( model, uri, s ); }
   
    /**
        find any existing reified statement that reifies _s_. If there isn't one,
        create one.
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
        @return true iff _s_ has a reification in this model
    */
    public boolean isReified( Statement s ) 
        { return reifier.hasTriple( s.asTriple() ); }

    /**
        remove all the reifications of _s_ in this model, whatever
        their associated resources.
    */
    public void removeAllReifications( Statement s ) 
        { reifier.remove( s.asTriple() ); }
      
    /**
        remove only this reification from this model.
    */  
    public void removeReification( ReifiedStatement rs )
        { reifier.remove( rs.asNode(), rs.getStatement().asTriple() ); }
        
    /**
        return an iterator that iterates over all the reified statements
        in this model.
    */
    public RSIterator listReifiedStatements()
        { return listReifiedStatements( Filter.any ); }
   
    /**
        return an iterator that iterates over all the reified statements in
        this model that reify _s_.
    */
    public RSIterator listReifiedStatements( Statement s )
        { return listReifiedStatements( matching( s ) ); }
   
    /**
        we need to push the filter into findReifiedStatements.
    */
    public RSIterator listReifiedStatements( Filter f )
        { return new RSIteratorImpl( findReifiedStatements() .filterKeep ( f ) ); }
      
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
            ReifiedStatement rs = (ReifiedStatement) n.as( ReifiedStatement.class );
            createReifiedStatement( rs.getURI(), rs.getStatement() );
            }
        }

    /**
        a Filter that accepts only RDFNodes that correspond to ReifiedStatements
    */
    private Filter matching( final Statement st )
        {
        return new Filter()
            {
            public boolean accept( Object o )
                {
                ReifiedStatement rs = (ReifiedStatement) ((RDFNode) o).as( ReifiedStatement.class );
                return rs.getStatement().equals( st );
                }
            };
        }
                       
    private ExtendedIterator findReifiedStatements()
        {
        Map1 map = new Map1()
            {
            public Object map1( Object node ) { return getRS( (Node) node ); }
            };
        return reifier .allNodes() .mapWith( map );
        }
        
    /**
        return a ReifiedStatement that is based on the node _n_. 
    */
    private ReifiedStatement getRS( Node n )
        {
        Triple t = reifier.getTriple( n );
        Statement s = IteratorFactory.asStatement( t, model );
        return ReifiedStatementImpl.create( model, n, s );
        }              
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

