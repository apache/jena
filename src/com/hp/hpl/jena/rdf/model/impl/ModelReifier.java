/*
	(c) Copyright 2003, Hewlett-Packard Development Company, LP
	[see end of file]
	$Id: ModelReifier.java,v 1.9 2003-09-08 10:54:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    This class impedance-matches the reification requests of Model[Com] to the operations
    supplied by it's Graph's Reifier.
    
    @author kers 
*/
public class ModelReifier
    {
    private ModelCom model;
    private Reifier reifier;
    
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
        this.reifier = model.asGraph().getReifier();
        }
        
    public Reifier.Style getReificationStyle()
        { return reifier.getStyle(); }
        
    /**
        Answer a version of the model, but with all its reifiying statements
        added.
        @param m a model that may have reified statements
        @return a new model, the union of m and the reification statements of m
    */
    public static Model withHiddenStatements( Model m )
        { 
        Graph mGraph = m.getGraph();
        Graph hiddenTriples = mGraph.getReifier().getHiddenTriples();
        return new ModelCom( new Union( mGraph, hiddenTriples ) );
        }
    
    /**
        Answer a model that consists of the hidden reification statements of this model.
        @return a new model containing the hidden statements of this model
    */    
    public Model getHiddenStatements()
        { return new ModelCom( reifier.getHiddenTriples() ); }
        
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
    public boolean isReified( Statement s ) 
        { return reifier.hasTriple( s.asTriple() ); }

    /**
        Remove all the reifications of a given statement in this model, whatever
        their associated resources.
        @param s the statement whose reifications are to be removed
    */
    public void removeAllReifications( Statement s ) 
        { reifier.remove( s.asTriple() ); }
      
    /**
        Remove a given reification from this model. Other reifications of the same statement
        are untouched.
        @param rs the reified statement to be removed
    */  
    public void removeReification( ReifiedStatement rs )
        { reifier.remove( rs.asNode(), rs.getStatement().asTriple() ); }
        
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
    public RSIterator listReifiedStatements( Statement s )
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
            ReifiedStatement rs = (ReifiedStatement) n.as( ReifiedStatement.class );
            createReifiedStatement( rs.getURI(), rs.getStatement() );
            }
        }
        
    /**
        A mapper that maps modes to their corresponding ReifiedStatement objects. This
        cannot be static: getRS cannot be static, because the mapping is model-specific.
    */
    protected final Map1 mapToRS = new Map1()
        {
        public Object map1( Object node ) { return getRS( (Node) node ); }
        };

    private ExtendedIterator findReifiedStatements()
        { return reifier .allNodes() .mapWith( mapToRS ); }

    private ExtendedIterator findReifiedStatements( Triple t )
        { return reifier .allNodes( t ) .mapWith( mapToRS ); }
        
    /**
        Answer a ReifiedStatement that is based on the given node. 
        @param n the node which represents the reification (and is bound to some triple t)
        @return a ReifiedStatement associating the resource of n with the statement of t.    
    */
    private ReifiedStatement getRS( Node n )
        {
        Triple t = reifier.getTriple( n );
        Statement s = IteratorFactory.asStatement( t, model );
        return ReifiedStatementImpl.create( model, n, s );
        }              
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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

