/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ConstraintStage.java,v 1.9 2003-10-06 05:37:40 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
    A ConstraintStage implements the constraint evaluation part of a
    query. The constraint, expressed as a graph in which each triple
    SPO constrains the value denoted by S and O to be related by
    P, is compiled into Predicate objects. The meaning of each P is
    given by a mapping from predicate URIs to PredicateFactories.
    
    @author kers
*/

public class ConstraintStage extends Stage
    {
    protected ExpressionSet constraint;
    protected Mapping map;
        
    /**
        constructor: compile the graph _g_ into a Predicate using the
        supplied _map_ for bindings of variables.
    */
    public ConstraintStage( Mapping map, ExpressionSet constraint )
        { this.constraint = constraint; 
        checkConstraint( map, constraint );
        this.map = map; }
        
    protected void checkConstraint( Mapping map, ExpressionSet constraint )
        { // TODO this properly
        Node n = Node.create( "deadwood" );
        constraint.evalBool( map, new Domain( new Node [] {n,n,n,n,n,n,n,n,n,n,n} ) );    
        }
        
    /**
        the translated graph is the AND-composition of the translated
        component triples.
    */
//    private Predicate translate( Mapping map, Graph g )
//        {
//        Predicate result = Predicate.TRUE;
//        ClosableIterator it = GraphUtil.findAll( g );
//        while (it.hasNext()) result = result.and( translate( map, (Triple) it.next() ) );
//        return result;
//        }

    /**
        The subject and object fields of _t_ are converted to Valuators using
        the given _map_. The predicate is used to find a factory in the factory map.
    */
//    private Predicate translate( Mapping map, Triple t )
//        {
//        Node pred = t.getPredicate();
//        Valuator L = translate( map, t.getSubject() ), R = translate( map, t.getObject() );
//        PredicateFactory f = (PredicateFactory) factories.get( pred );
//        if (f == null) 
//            throw new UnsupportedOperationException( pred.toString() );
//        else 
//            return f.construct( L, R );
//        }
      
    /**
        it's possible that this code should belong in Node and its children
    */  
//    private Valuator translate( Mapping map, Node X )
//        {
//        return X.isVariable()
//            ? new ValuatorVariable( map.indexOf( X ) )
//            : (Valuator) new ValuatorConst( X )
//            ;
//        }
//                
    /**
        the map which relates predicate nodes to the corresponding predicate
        factories.
    */
    private static HashMap factories = new HashMap();

    /**
        associate the predicate factory _f_ with _uri_, which must be a legal
        URI string. You can't change an existing binding.
    */
    static public void addFactory( String uri, PredicateFactory f )
        {
        Node n = Node.createURI( uri );
        if (factories.containsKey( n ))
            throw new UnsupportedOperationException( "cannot redefine: " + f );
        else
            factories.put( n, f );
        }
               
    static final PredicateFactory makeEQ = new PredicateFactory()
        { public Predicate construct( Valuator L, Valuator R ) { return new Relation_EQ( L, R ); }};

    static final PredicateFactory makeNE = new PredicateFactory()
        { public Predicate construct( Valuator L, Valuator R ) { return new Relation_NE( L, R ); }};

    static final PredicateFactory makeMATCHES = new PredicateFactory()
        { public Predicate construct( Valuator L, Valuator R ) { return new Relation_MATCHES( L, R ); }};

    static class Relation_MATCHES extends Relation
        {
        Relation_MATCHES( Valuator L, Valuator R ) { super( L, R ); }   
        
        private String asString( Node n )
            {
            if (n.isLiteral()) return n.getLiteral().getLexicalForm();
            else return n.toString();    
            }
            
        public boolean matches( Node L, Node R )
            { 
                String x = asString( L ), y = asString( R );
                return x.indexOf( y ) > -1; }
            
        public boolean evaluateBool( Domain d )
            { return matches( valueL( d ), valueR( d ) ); }
        }
        
    static
        {
        addFactory( "q:eq" , makeEQ );
        addFactory( "q:ne", makeNE );        
        addFactory( "q:matches", makeMATCHES );
        }
                
   private boolean evalConstraint( Domain d, ExpressionSet e )
        {
        return constraint.evalBool( map, d );
        }
    
    /**
        the delivery component: read the domain elements out of the
        input pipe, and only pass on those that satisfy the predicate.
    */
    public Pipe deliver( final Pipe L )
        {
        final Pipe mine = previous.deliver( new BufferPipe() );
        new Thread( "a ConstraintStage" )
        	{
        	public void run()
        		{
		        while (mine.hasNext())
		            {
		            Domain d = mine.get();
		            if (evalConstraint( d, constraint )) L.put( d );
		            }
		        L.close();
        		}
        	} .start();
        return L;
        }
    }

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
