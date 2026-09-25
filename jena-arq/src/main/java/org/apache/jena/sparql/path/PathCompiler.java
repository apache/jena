/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.path;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.algebra.optimize.TransformPathFlattenAlgebra;
import org.apache.jena.sparql.algebra.optimize.TransformPathFlatten;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;

public class PathCompiler {
    /**
     * Default maximum length value used for the {@link #MAX_LENGTH_PATH_FOR_REDUCTION} control
     */
    public static final int DEFAULT_MAX_LENGTH = 10;
    /**
     * Specifies the maximum length (inclusive) of a path that will be reduced by expanding it into individual
     * invocations of the path expression linked by intermediate variables.  Defaults to {@value #DEFAULT_MAX_LENGTH}.
     * <p>
     * This control prevents a query using a path like {@code :x :p{N} :y} with a large value of {@code N} being
     * expanded into a massive algebra tree that yields no performance benefits.
     * </p>
     * @see #isReducibleLength(long)
     */
    public static int MAX_LENGTH_PATH_FOR_REDUCTION = DEFAULT_MAX_LENGTH;

    // Convert to work on OpPath.
    // Need pre (and post) BGPs.

    private VarAlloc varAlloc = new VarAlloc(ARQConstants.allocPathVariables);

    /** Testing use only. */
    public static void resetForTest() {
        //varAlloc = new VarAlloc(ARQConstants.allocPathVariables);
    }

    public PathCompiler() {}

    // Assumes one PathCompiler per query so that the varAlloc is per query.
    // See TransformPathFlatten
    //
    // Move to AlgebraCompiler and have a per-transaction scoped var generator

    // ---- Syntax-based

    /**
     * Simplify : turns constructs in simple triples and simpler TriplePaths where
     * possible
     */
    public PathBlock reduce(PathBlock pathBlock) {
        PathBlock x = new PathBlock();
        // No context during algebra generation time.
        //        VarAlloc varAlloc = VarAlloc.get(context, ARQConstants.sysVarAllocNamed) ;
        //        if ( varAlloc == null )
        //            // Panic
        //            throw new ARQInternalErrorException("No execution-scope allocator for variables") ;

        // Translate one into another.
        reduce(x, pathBlock, varAlloc);
        return x;
    }

    void reduce(PathBlock x, PathBlock pathBlock, VarAlloc varAlloc) {
        for ( TriplePath tp : pathBlock ) {
            if ( tp.isTriple() ) {
                x.add(tp);
                continue;
            }
            reduce(x, varAlloc, tp.getSubject(), tp.getPath(), tp.getObject());
        }
    }

    /**
     * Algebra-based transformation.
     * <p>
     * Does not include "|", this method is called by {@link TransformPathFlatten}.
     * See {@link TransformPathFlattenAlgebra} for union expansion.
     * </p>
     */
    public PathBlock reduce(TriplePath triplePath) {
        PathBlock x = new PathBlock();
        reduce(x, varAlloc, triplePath.getSubject(), triplePath.getPath(), triplePath.getObject());
        return x;
    }

    public PathBlock reduce(Node start, Path path, Node finish) {
        PathBlock x = new PathBlock();
        reduce(x, varAlloc, start, path, finish);
        return x;
    }

    private static void reduce(PathBlock x, VarAlloc varAlloc, Node startNode, Path path, Node endNode) {
        if ( path instanceof P_Link pLink) {
            Node pred = pLink.getNode();
            Triple t = Triple.create(startNode, pred, endNode);
            x.add(new TriplePath(t));
            return;
        }

        if ( path instanceof P_Seq pSeq) {
            Node v = varAlloc.allocVar();
            if ( Var.isVar(startNode) && !Var.isVar(endNode) ) {
                // start at the grounded term.
                reduce(x, varAlloc, v, pSeq.getRight(), endNode);
                reduce(x, varAlloc, startNode, pSeq.getLeft(), v);
            } else {
                reduce(x, varAlloc, startNode, pSeq.getLeft(), v);
                reduce(x, varAlloc, v, pSeq.getRight(), endNode);
            }
            return;
        }

        if ( path instanceof P_Inverse pInverse) {
            reduce(x, varAlloc, endNode, pInverse.getSubPath(), startNode);
            return;
        }

        if ( path instanceof P_FixedLength pFixedLen ) {
            long N = pFixedLen.getCount();
            if (isReducibleLength(N)) {
                // Don't do {0}
                // Also if the path is too long the reduction won't generate any performance benefits
                reduceFixedLength(x, varAlloc, startNode, endNode, N, pFixedLen.getSubPath());
                return;
            }
        }

        if ( path instanceof P_Mod pMod ) {
            if ( pMod.isFixedLength() && pMod.getFixedLength() > 0 ) {
                long N = pMod.getFixedLength();
                if (isReducibleLength(N)) {
                    reduceFixedLength(x, varAlloc, startNode, endNode, N, pMod.getSubPath());
                    return;
                }
            }

            // This is the rewrite of
            //    "x {N,} y" to "x :p{N} ?V . ?V :p* y"
            //    "x {N,M} y" to "x :p{N} ?V . ?V {0,M} y"
            // The spec defines {n,m} to be
            //   {n} union {n+1} union ... union {m}
            // which leads to a lot of repeated work.

            if ( pMod.getMin() > 0 ) {
                Path p1 = PathFactory.pathFixedLength(pMod.getSubPath(), pMod.getMin());
                Path p2;

                if ( pMod.getMax() < 0 )
                    p2 = PathFactory.pathZeroOrMoreN(pMod.getSubPath());
                else {
                    if ( pMod.getMin() > pMod.getMax() )
                        throw new ARQException("Bad path: " + pMod);

                    long len2 = pMod.getMax() - pMod.getMin();
                    if ( len2 < 0 )
                        len2 = 0;
                    p2 = PathFactory.pathMod(pMod.getSubPath(), 0, len2);
                }

                Node v = varAlloc.allocVar();

                // Start at the fixed end.
                if ( !startNode.isVariable() || endNode.isVariable() ) {
                    reduce(x, varAlloc, startNode, p1, v);
                    reduce(x, varAlloc, v, p2, endNode);
                } else {
                    // endNode fixed, start node not.
                    reduce(x, varAlloc, v, p2, endNode);
                    reduce(x, varAlloc, startNode, p1, v);
                }
                return;
            }

            // Else drop through
        }

        // Nothing can be done.
        x.add(new TriplePath(startNode, path, endNode));
    }

    /**
     * Checks whether a given length of path is considered reducible
     * <p>
     * Only paths that are non-zero length and less than, or equal to, the configured
     * {@link #MAX_LENGTH_PATH_FOR_REDUCTION} (default 10) are considered reducible.  Anything else is left as-is
     * as either reduction would change semantics (for zero-length paths), or could lead to very large algebra tree
     * which would negatively impact performance.
     * </p>
     * @param N Path length
     * @return True if reducible, false otherwise
     */
    public static boolean isReducibleLength(long N) {
        return N > 0 && N <= MAX_LENGTH_PATH_FOR_REDUCTION;
    }

    /**
     * Reduces a fixed length path by expanding the {@code n} steps into individual path invocations with intermediate
     * variables.
     * @param x             Path block to append into
     * @param varAlloc      Variable allocator
     * @param startNode     Start node
     * @param endNode       End node
     * @param n             Fixed path length
     * @param subPath       Sub path to use in each expanded step
     */
    private static void reduceFixedLength(PathBlock x, VarAlloc varAlloc, Node startNode, Node endNode, long n,
                                          Path subPath) {
        Node stepStart = startNode;

        for (long i = 0; i < n - 1 ; i++ ) {
            Node v = varAlloc.allocVar();
            reduce(x, varAlloc, stepStart, subPath, v);
            stepStart = v;
        }
        reduce(x, varAlloc, stepStart, subPath, endNode);
    }
}
