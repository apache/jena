/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PatternStageCompiler.java,v 1.7 2005-02-21 11:52:24 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    PatternStageCompiler serves two purposes: it contains the standard algorithm
    for compiling patterns-as-triples to patterns-as-Pattern(s), and it has the
    standard implementation of PatternCompiler in terms of ordinary Elements.
    
    @author kers
*/
public final class PatternStageCompiler implements PatternCompiler
    {
    /** no state, so the constructor is boring.
    */
    public PatternStageCompiler()
        {}
      
    /**
        to compile an array of triples, compile each triple and form the corresponding
        array of Patterns. *preserve the order*. 
    */  
    public static Pattern [] compile( PatternCompiler compiler, Mapping map, Triple [] source )
        {
        Pattern [] compiled = new Pattern[source.length];
        for (int i = 0; i < source.length; i += 1) compiled[i] = compile( compiler, source[i], map );
        return compiled;
        }
       
    /**
        to compile a triple, compile each node and form a Pattern  from the resulting Elements.
    */
    private static Pattern compile( PatternCompiler compiler, Triple t, Mapping map )
        {
        Node S = t.getSubject(), P = t.getPredicate(), O = t.getObject();
        return new Pattern( compile( compiler, S, map ), compile( compiler, P, map ), compile( compiler, O, map ) );
        }
        
    /**
        to compile a Node, special-case variables and ANYs. Surely this code is
        better in Node, but I really don't like exporting so much query information
        into the Node data-type. Hmm. Perhaps some node types are better off in
        the .query package.
    */
    private static Element compile( PatternCompiler compiler, Node X, Mapping map )
        {
        if (X.equals( Query.ANY )) return compiler.any();
        if (X.isVariable()) 
            {
            if (map.hasBound( X ))
                return compiler.bound( X, map.indexOf( X ) );
            else
                return compiler.bind( X, map.newIndex( X ) );
            }
        return compiler.fixed( X );
        }

    /*
        satisfy the interface
    */            
    public Element fixed( Node value )
        { return new Fixed( value ); }
        
    public Element bound( Node n, int index )
        { return new Bound( index ); }
        
    public Element bind( Node n, int index )
        { return new Bind( index ); }
        
    public Element any()
        { return Element.ANY; }
    }
/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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