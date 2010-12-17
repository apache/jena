/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.PathBlock ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.VarAlloc ;

public class PathCompiler
{
    // Convert to work on OpPath.
    // Need pre (and post) BGPs.
    
    private static VarAlloc varAlloc = new VarAlloc(ARQConstants.allocVarAnonMarker+"P") ;
    
    // Move to AlgebraCompiler and have a per-transaction scoped var generator 
    
    // ---- Syntax-based
    
    /** Simplify : turns constructs in simple triples and simpler TriplePaths where possible */ 
    public PathBlock reduce(PathBlock pathBlock)
    {
        PathBlock x = new PathBlock() ;
        // No context during algebra generation time.
//        VarAlloc varAlloc = VarAlloc.get(context, ARQConstants.sysVarAllocNamed) ;
//        if ( varAlloc == null )
//            // Panic
//            throw new ARQInternalErrorException("No execution-scope allocator for variables") ;
        
        // Translate one into another.
        reduce(x, pathBlock, varAlloc) ;
        return x ;
    }
   
    void reduce(PathBlock x, PathBlock pathBlock, VarAlloc varAlloc )
    {
        for ( TriplePath tp : pathBlock )
        {
            if ( tp.isTriple() )
            {
                x.add(tp) ;
                continue ;
            }
            reduce(x, varAlloc, tp.getSubject(), tp.getPath(), tp.getObject()) ;
        }
    }
    
    // ---- Algebra-based transformation.
    public PathBlock reduce(TriplePath triplePath)
    {
        PathBlock x = new PathBlock() ;
        reduce(x, varAlloc, triplePath.getSubject(), triplePath.getPath(), triplePath.getObject()) ;
        return x ;
    }
    
    public PathBlock reduce(Node start, Path path, Node finish)
    {
        PathBlock x = new PathBlock() ;
        reduce(x, varAlloc, start, path, finish) ;
        return x ;
    }
    
    
    private static void reduce(PathBlock x, VarAlloc varAlloc, Node startNode, Path path, Node endNode)
    {
        // V-i-s-i-t-o-r!
        
        if ( path instanceof P_Link )
        {
            Node pred = ((P_Link)path).getNode() ;
            Triple t = new Triple(startNode, pred, endNode) ; 
            x.add(new TriplePath(t)) ;
            return ;
        }

        if ( path instanceof P_Seq )
        {
            P_Seq ps = (P_Seq)path ;
            Node v = varAlloc.allocVar() ;
            reduce(x, varAlloc, startNode, ps.getLeft(), v) ;
            reduce(x, varAlloc, v, ps.getRight(), endNode) ;
            return ;
        }

        if ( path instanceof P_Inverse )
        {
            reduce(x, varAlloc, endNode, ((P_Inverse)path).getSubPath(), startNode) ;
            return ;
        }

        if ( path instanceof P_FixedLength )
        {
            P_FixedLength pFixed = (P_FixedLength)path ;
            long N = pFixed.getCount() ;
            if ( N > 0 )
            {
                // Don't do {0}
                Node stepStart = startNode ;
    
                for ( long i = 0 ; i < N-1 ; i++ )
                {
                    Node v = varAlloc.allocVar() ;
                    reduce(x, varAlloc, stepStart, pFixed.getSubPath(), v) ;
                    stepStart = v ;
                }
                reduce(x, varAlloc, stepStart, pFixed.getSubPath(), endNode) ;
                return ;
            }
        }
        
        if ( path instanceof P_Mod )
        {
            P_Mod pMod = (P_Mod)path ;
            if ( pMod.isFixedLength() && pMod.getFixedLength() > 0 )
            {
                long N = pMod.getFixedLength() ;
                if ( N > 0 )
                {
                    Node stepStart = startNode ;

                    for ( long i = 0 ; i < N-1 ; i++ )
                    {
                        Node v = varAlloc.allocVar() ;
                        reduce(x, varAlloc, stepStart, pMod.getSubPath(), v) ;
                        stepStart = v ;
                    }
                    reduce(x, varAlloc, stepStart, pMod.getSubPath(), endNode) ;
                    return ;
                }
            }
            // Not fixed - drop through, including zero length paths.
        }
        
        // Nothing can be done.
        x.add(new TriplePath(startNode, path, endNode)) ;
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