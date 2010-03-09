/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.reorder;

import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.TERM;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.VAR;

import com.hp.hpl.jena.sparql.core.NodeConst;
import com.hp.hpl.jena.sparql.sse.Item;

import com.hp.hpl.jena.tdb.solver.stats.StatsMatcher;
import com.hp.hpl.jena.tdb.solver.stats.StatsMatcher.Pattern;

public class ReorderFixed extends ReorderTransformationBase
{
    ReorderFixed() {}
    // Fixed scheme for when we have no stats.
    // It chooses a triple pattern by order of preference.
    
    private static Item type = Item.createNode(NodeConst.nodeRDFType) ;
    
    /** The number of triples used for the base scale */
    public static int MultiTermSampleSize = 100 ; 

    /** Maximum value for a match involving two terms. */
    public static int MultiTermMax = 9 ; 
    
    public final static StatsMatcher matcher ;
    static {
        matcher = new StatsMatcher() ;
        
        //matcher.addPattern(new Pattern(1,   TERM, TERM, TERM)) ;     // SPO - built-in - not needed a s a rule
        
        // Numbers choosen as an approximation for a graph of 100 triples
        matcher.addPattern(new Pattern(2,   TERM, TERM, VAR)) ;     // SP?
        matcher.addPattern(new Pattern(5,   TERM, type, TERM)) ;    // ? type O -- worse than ?PO
        matcher.addPattern(new Pattern(3,   VAR,  TERM, TERM)) ;    // ?PO
        matcher.addPattern(new Pattern(2,   TERM, TERM, TERM)) ;    // S?O
        
        matcher.addPattern(new Pattern(10,  TERM, VAR,  VAR)) ;     // S??
        matcher.addPattern(new Pattern(20,  VAR,  VAR,  TERM)) ;    // ??O
        matcher.addPattern(new Pattern(30,  VAR,  TERM, VAR)) ;     // ?P?

        matcher.addPattern(new Pattern(MultiTermSampleSize, VAR,  VAR,  VAR)) ;     // ???
    }
    
    @Override
    public double weight(PatternTriple pt)
    {
        return matcher.match(pt) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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