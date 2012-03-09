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

            // This is the rewrite of 
            //    "x {N,} y" to "x :p{N} ?V . ?V :p* y"
            //    "x {N,M} y" to "x :p{N} ?V . ?V {0,M} y"
            // The spec defines {n,m} to be 
            //   {n} union {n+1} union ... union {m}
            // which leads to a lot of repeated work.
            
            if ( pMod.getMin() > 0 )
            {
                Path p1 = PathFactory.pathFixedLength(pMod.getSubPath(), pMod.getMin()) ;
                Path p2 ;
                
                if ( pMod.getMax() < 0 )
                    p2 = PathFactory.pathZeroOrMoreN(pMod.getSubPath()) ;
                else
                {
                    long len2 = pMod.getMax()-pMod.getMin() ;
                    if ( len2 < 0 ) len2 = 0 ;
                    p2 = PathFactory.pathMod(pMod.getSubPath(),0, len2) ;
                }
                
                Node v = varAlloc.allocVar() ;
                
                // Start at the fixed end.
                if ( ! startNode.isVariable() || endNode.isVariable() )
                {
                    reduce(x, varAlloc, startNode, p1, v) ;
                    reduce(x, varAlloc, v, p2, endNode) ;
                }
                else
                {
                    // endNode fixed, start node not.
                    reduce(x, varAlloc, v, p2, endNode) ;
                    reduce(x, varAlloc, startNode, p1, v) ;
                }
                return ;
            }
            
            
            // Else drop through
        }
        
        // Nothing can be done.
        x.add(new TriplePath(startNode, path, endNode)) ;
    }
}
