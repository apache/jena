/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: FasterPatternStage.java,v 1.8 2005-07-07 13:19:44 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.CollectionFactory;

public class FasterPatternStage extends Stage
    {
    protected GraphMemFaster graph;
    protected Pattern [] compiled;
    protected ValuatorSet [] guards;
    protected Set [] boundVariables;
    
//    protected class SuperPattern extends Pattern
//        {
//        public SuperPattern( Element S, Element P, Element O )
//            { super( S, P, O );  }
//        
//        public Iterator find( Domain current )
//            {
//            Node Sn = nullToAny( S.asNodeMatch( current ) );
//            Node Pn = nullToAny( P.asNodeMatch( current ) );
//            Node On = nullToAny( O.asNodeMatch( current ) );
//            return graph.findFaster( Sn, Pn, On );
//            }
//        }
    
    public FasterPatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple [] triples )
        {
        this.graph = (GraphMemFaster) graph;
        this.boundVariables = makeBoundVariables( triples );
        Pattern [] unoptimised = compile( map, triples );
        this.guards = makeGuards( map, constraints, triples.length );
        this.compiled = optimise( unoptimised, this.guards );
        }
                
    /**
        Answer an array of sets exactly as long as the argument array of Triples.
        The i'th element of the answer is the set of all variables that have been 
        matched when the i'th triple has been matched.
    */
    protected Set [] makeBoundVariables( Triple [] triples )
        {
        int length = triples.length;
        Set [] result = new Set[length];
        Set prev = CollectionFactory.createHashedSet();
        for (int i = 0; i < length; i += 1) 
            prev = result[i] = Util.union( prev, Util.variablesOf( triples[i] ) );
        return result;
        }
    
    /**
        Answer an array of ExpressionSets exactly as long as the supplied length.
        The i'th ExpressionSet contains the prepared [against <code>map</code>]
        expressions that can be evaluated as soon as the i'th triple has been matched.
        By "can be evaluated as soon as" we mean that all its variables are bound.
        The original ExpressionSet is updated by removing those elements that can
        be so evaluated.
        
        @param map the Mapping to prepare Expressions against
        @param constraints the set of constraint expressions to plant
        @param length the number of evaluation slots available
        @return the array of prepared ExpressionSets
    */
    protected ValuatorSet [] makeGuards( Mapping map, ExpressionSet constraints, int length )
        {        
        ValuatorSet [] result = new ValuatorSet [length];
        for (int i = 0; i < length; i += 1) result[i] = new ValuatorSet();
        Iterator it = constraints.iterator();
        while (it.hasNext())
            plantWhereFullyBound( (Expression) it.next(), it, map, result );
        return result;
        }
    
    /**
        Find the earliest triple index where this expression can be evaluated, add it
        to the appropriate expression set, and remove it from the original via the
        iterator.
    */
    protected void plantWhereFullyBound( Expression e, Iterator it, Mapping map, ValuatorSet [] es )
        {
        for (int i = 0; i < boundVariables.length; i += 1)
            if (canEval( e, i )) 
                { 
                es[i].add( e.prepare( map ) ); 
                it.remove(); 
                return; 
                }
        }
    
    /**
        Answer true iff this Expression can be evaluated after the index'th triple
        has been matched, ie, all the variables of the expression have been bound.
    */
    protected boolean canEval( Expression e, int index )
        { return Expression.Util.containsAllVariablesOf( boundVariables[index], e ); }
    
    protected Pattern [] compile( Mapping map, Triple [] triples )
        { return compile( compiler, map, triples ); }
        
    protected Pattern [] compile( PatternCompiler pc, Mapping map, Triple [] source )
        { return PatternStageCompiler.compile( pc, map, source ); }
        
    protected Pattern [] optimise( Pattern [] patterns, ValuatorSet [] guards )
        {
        Pattern [] result = new Pattern[patterns.length];
        for (int i = 0; i < patterns.length; i += 1) 
            {
            final ValuatorSet s = guards[i];
            Pattern p = optimise( patterns[i] );
            if (s.isNonTrivial())
                {
                result[i] = new Pattern( p.S, p.P, p.O )
                    {
                    public boolean match( Domain d, Triple t )
                        { return super.match( d, t ) && s.evalBool( d ); }
                    };
                }
            else
                result[i] = p; // new Pattern( p.S, p.P, p.O );
            }
        return result;
        }
    
    protected boolean sameVariable( Element x, Element Y )
        {
        return x.getIndex() == Y.getIndex();
        }
    
    protected Pattern optimise( Pattern p )
        {
        int bits =
            (p.S instanceof Bind ? 4 : 0)
            | (p.P instanceof Bind || (p.P instanceof Bound && sameVariable( p.S, p.P )) ? 2 : 0)
            | (p.O instanceof Bind || (p.O instanceof Bound && (sameVariable( p.S, p.O ) || sameVariable( p.P, p.O ))) ? 1 : 0)
            ;
        switch (bits)
            {
            case 7:
                return p;

            case 1:
                return new Pattern( p.S, p.P, p.O )
                    {
                    public boolean match( Domain d, Triple t )
                        { return O.match( d, t.getObject() ); }
                    };
                
            case 5:
                return new Pattern( p.S, p.P, p.O ) 
                    {
                    public boolean match( Domain d, Triple t )
                        { 
                        return S.match( d, t.getSubject() ) 
                        && O.match( d, t.getObject() ); }
                    };

            case 4:                
                return new Pattern( p.S, p.P, p.O ) 
                    {
                    public boolean match( Domain d, Triple t )
                        { return S.match( d, t.getSubject() ); }
                    };

            case 0:
                return new Pattern( p.S, p.P, p.O ) 
                    {
                    public boolean match( Domain d, Triple t )
                        { return true; }
                    };
                    
            case 6:  
                return new Pattern( p.S, p.P, p.O ) 
                    {
                    public boolean match( Domain d, Triple t )
                        { 
                        return S.match( d, t.getSubject() ) 
                        && P.match( d, t.getPredicate() ); 
                        }
                    };
                    
            case 3:
                return new Pattern( p.S, p.P, p.O )
                    {
                    public boolean match( Domain d, Triple t )
                        {
                        return P.match( d, t.getPredicate() )
                        && O.match( d, t.getObject() );
                        }
                    };
                    
            case 2:
                return new Pattern( p.S, p.P, p.O )
                    {
                    public boolean match( Domain d, Triple t )
                        { return P.match( d, t.getPredicate() ); }
                    };
            }
        return p; 
        }
    
    private static final PatternCompiler compiler = new PatternStageCompiler();
        
    private static int count = 0;
    
    public synchronized Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
        new Thread( "PatternStage-" + ++count ) { public void run() { FasterPatternStage.this.run( stream, result ); } } .start();
        return result;
        }
        
    protected void run( Pipe source, Pipe sink )
        {
        try { while (stillOpen && source.hasNext()) nest( sink, source.get(), 0 ); }
        catch (Exception e) { sink.close( e ); return; }
        sink.close();
        }        

    private static Node nullToAny( Node n )
        { return n == null ? Node.ANY : n; }  
    
    protected void nest( Pipe sink, Domain current, int index )
        { 
        if (index == compiled.length)
            sink.put( current.copy() );
        else
            {
            Pattern p = compiled[index];
            Node Sn = nullToAny( p.S.asNodeMatch( current ) );
            Node Pn = nullToAny( p.P.asNodeMatch( current ) );
            Node On = nullToAny( p.O.asNodeMatch( current ) );
            Iterator it = graph.findFaster( Sn, Pn, On );
            while (stillOpen && it.hasNext())
                if (p.match( current, (Triple) it.next() )) nest( sink, current, index + 1 );
            }
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/