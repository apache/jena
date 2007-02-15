/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.algebra;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.algebra.op.OpDatasetNames;
import com.hp.hpl.jena.query.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.query.core.*;
import com.hp.hpl.jena.query.syntax.Element;
import com.hp.hpl.jena.query.syntax.ElementGroup;
import com.hp.hpl.jena.query.syntax.ElementNamedGraph;

public class AlgebraGeneratorQuad extends AlgebraGenerator 
{
    public static Op compile(Query query)
    {
        Op pattern = compile(query.getQueryPattern()) ;
        return compileModifiers(query, pattern) ;
    }

    public static Op compile(Element elt)
    {
        return new AlgebraGeneratorQuad().compileGraphPattern(elt) ;
    }
    
    /** Compile query modifiers */
    public static Op compileModifiers(Query query, Op pattern)
    {
        return AlgebraGenerator.compileModifiers(query, pattern) ;
    }

    private Node currentGraph = Quad.defaultGraph ;
    
    protected AlgebraGeneratorQuad()
    { super() ; }
    
    protected Op compile(BasicPattern pattern)
    {
        return new OpQuadPattern(currentGraph, pattern) ;
    }

    protected Op compile(ElementNamedGraph eltGraph)
    {
        Node graphNode = eltGraph.getGraphNameNode() ;
        Node g = currentGraph ;
        currentGraph = graphNode ;
        
        if ( eltGraph.getElement() instanceof ElementGroup )
        {
            if ( ((ElementGroup)eltGraph.getElement()).isEmpty() )
            {
                // GRAPH ?g {} or GRAPH <v> {}
                return new OpDatasetNames(graphNode) ;
            }
        }
        Op sub = compileGraphPattern(eltGraph.getElement()) ;
        currentGraph = g ;
        return sub ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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