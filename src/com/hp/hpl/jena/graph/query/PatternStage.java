/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PatternStage.java,v 1.14 2003-10-15 09:22:36 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A PatternStage is a Stage that handles some bunch of related patterns; those patterns
    are encoded as Triples.
    
    @author hedgehog
*/

public class PatternStage extends Stage
    {
    protected Graph graph;
    protected Pattern [] compiled;
    protected ExpressionSet [] guards;
    protected Set [] boundVariables;
    
    public PatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple [] triples )
        {
        this.graph = graph;
        this.compiled = compile( map, triples );
        this.boundVariables = makeBoundVariables( triples );
        this.guards = makeGuards( map, constraints, triples.length );
        }
                
    protected Set [] makeBoundVariables( Triple [] triples )
        {
        int length = triples.length;
    	Set [] result = new Set[length];
        Set prev = new HashSet();
        for (int i = 0; i < length; i += 1) 
            prev = result[i] = Util.union( prev, Util.variablesOf( triples[i] ) );
        return result;
        }
    
    protected ExpressionSet [] makeGuards( Mapping map, ExpressionSet constraints, int length )
        {        
    	ExpressionSet [] result = new ExpressionSet [length];
        for (int i = 0; i < length; i += 1) result[i] = new ExpressionSet();
        Iterator it = constraints.iterator();
        while (it.hasNext())
            plantWhereFullyBound( (Expression) it.next(), it, map, result );
        return result;
        }
    
    protected void plantWhereFullyBound( Expression e, Iterator it, Mapping map, ExpressionSet [] es )
        {
        for (int i = 0; i < boundVariables.length; i += 1)
            if (canEval( e, i )) { es[i].add( e.prepare( map ) ); it.remove(); return; }
        }
    
    private boolean canEval( Expression e, int index )
        { return boundVariables[index].containsAll( Expression.Util.variablesOf( e ) ); }

    protected Pattern [] compile( Mapping map, Triple [] triples )
        { return compile( compiler, map, triples ); }
        
    protected Pattern [] compile( PatternCompiler pc, Mapping map, Triple [] source )
        { return PatternStageCompiler.compile( pc, map, source ); }
        
    private static final PatternCompiler compiler = new PatternStageCompiler();
        
    public Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
        new Thread() { public void run() { PatternStage.this.run( stream, result ); } } .start();
        return result;
        }
        
    protected void run( Pipe source, Pipe sink )
        {
        while (stillOpen && source.hasNext()) nest( sink, source.get(), 0 );
        sink.close();
        }        
        
    protected void nest( Pipe sink, Domain current, int index )
        {
        if (index == compiled.length)
            sink.put( current.copy() );
        else
            {
            Pattern p = compiled[index];
            ExpressionSet guard = guards[index];
            ClosableIterator it = graph.find( p.asTripleMatch( current ) );
            while (stillOpen && it.hasNext())
                if (p.match( current, (Triple) it.next()) && guard.evalBool( current )) 
                    nest( sink, current, index + 1 );
            it.close();
            }
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
