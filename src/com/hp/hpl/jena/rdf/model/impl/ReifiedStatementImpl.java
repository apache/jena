/*
	(c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
	[see end of file]
	$Id: ReifiedStatementImpl.java,v 1.3 2003-06-06 09:28:22 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;


/**
    A ReifiedStatementImpl encodes a Statement and behaves as a Resource.
*/

public class ReifiedStatementImpl extends ResourceImpl implements ReifiedStatement
    {
    /** the Statement that this ReifiedStatement represents */
    protected Statement statement;
    
    /**
        private constructor, relies (ugh) on super(uri, model) generating
        bnode if uril == null. 
    */

    private ReifiedStatementImpl( Model m, String uri, Statement s ) 
        {
        super( uri, m ); 
        assertStatement( s ); 
        }
        
    private ReifiedStatementImpl( EnhGraph m, Node n, Statement s )
        {
        super( n, m );
        assertStatement( s );
        }
        
    private void assertStatement( Statement s )
        {
        statement = s;
        }
        
    /** 
        answer [a .equals() version of] the Statement that this ReifiedStatement
        represents.
    */
    public Statement getStatement() 
        { return statement; }
       
    static final public Implementation factory = new Implementation() 
        {
        /**
            convert a _node_ into a ReifiedStatement in the enhanced graph 
            _eg_ by looking into this graph's reifier to find the binding for the
            node; throw a DoesNotReify exception if there's no mapping. 
        */
        public EnhNode wrap( Node n, EnhGraph eg ) 
            {
            Triple x = n.getTriple( eg.asGraph().getReifier() );
            if (x == null) throw new DoesNotReifyException( n );
            Statement st = StatementImpl.toStatement( x, eg );
            return new ReifiedStatementImpl( eg, n, st );
            }
            
        /**
            see if it's possible to convert _n_
        */
        public boolean canWrap( Node n, EnhGraph eg )
            { return n.getTriple( eg.asGraph().getReifier() ) != null; }
        };
        
    public boolean isValid()
        { return this.asNode().getTriple( getModel().getGraph().getReifier() ) != null; }
        
    /**
        tell the underlying graph's reifier that this ReifiedStatement's node
        represents any Statement with this statement's triple. (May throw an
        exception if the node is already reified to something different.)
    */        
    private ReifiedStatementImpl cache()
        {
        getModel().getGraph().getReifier().reifyAs( this.asNode(), statement.asTriple() );
        return this;
        }
      
    /**
        factory method. answer a ReifiedStatement which encodes the
        Statement _s_. The mapping is remembered.
    */  
    public static ReifiedStatement create( Statement s )
        { return create( s.getModel(), (String) null, s ); }

    /**
        factory method. answer a ReifiedStatement which encodes the
        Statement _s_ with uri _uri_. The mapping is remembered.
    */        
    public static ReifiedStatementImpl create( Model m, String uri, Statement s )
        { return new ReifiedStatementImpl( m, uri, s ).cache(); }        
        
    public static ReifiedStatementImpl create( EnhGraph eg, Node n, Statement s )
        { return new ReifiedStatementImpl( eg, n, s ).cache(); }
    
    public String toString()
        { return super.toString() + "=>" + statement; }
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
