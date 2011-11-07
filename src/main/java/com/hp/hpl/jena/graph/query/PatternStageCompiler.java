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
    @Override
    public Element fixed( Node value )
        { return new Fixed( value ); }
        
    @Override
    public Element bound( Node n, int index )
        { return new Bound( index ); }
        
    @Override
    public Element bind( Node n, int index )
        { return new Bind( index ); }
        
    @Override
    public Element any()
        { return Element.ANY; }
    }
