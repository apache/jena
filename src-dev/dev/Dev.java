/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;


public class Dev
{
    // Next:
    // Integrate TransformFilterPlacement, TransformPropertyFunction
    // [Some sort of flags to make what has been done??]
    // Optimize: QueryEngineMain.QueryEngineMain(Op op,)
    // so does not happen for ref engine (property functions).

    //  Two stage:
    //    Algebra.compile (inc simplify).
    //    Algebra.optimize(op, context)
    //      (done at start of execution, not engine creation so context can have been updated). 
    
    // Stage 0 - always
    //    Simplify
    // Stage 1 - general algebra rewrites
    //    ? Filter placement
    //    ? Equality filter

    // Stage 2 - per execution -- context and dataset available.
    //    ? Property function
    //    ? BGP rewrites
    
    /*
     * Move PF to Transforms
     * 
     * TransformFilterPlacement  [DONE] - not enabled, yet.
     * TransformPropertyFunction [DONE] - not enabled, yet.
     * Simplify [DONE] 
     *    - algebra.opt.TransformSimplify 
     *    -- called by AlgebraGenerator because of SimplifyEarly
     * Equality filter [DONE]
     *    - algebra.opt.TransformEqualityFilter
     *    -- called via Algebra.compile(,optimize)
     *    
     * TransformRemoveLabels
     * TransformReorderBGP
     * TransformQuadization
     *   - needs to work on the way down as well!
     *     Could redo from (graph) down  
     * 
     */
    
    // Annotations (at least of join)
    // Relationship to "label"
    
    /*
     * Getting into an algebra for *execution*
     * Separate system or extension of algebra?
     *   Extension - but be clear of what's where.

    + Have a "marker" op meaning current working table - the current enclosing sequnce (or Unit table if none?)
          (seq) (current)
      Hence:    
          (filter (current) ...)
          (leftJoin (current) other)
    
    + Transform framework :
        Match => Action for a single rewrite?  Need to worry about this?
          Transform collection.
            Move PF to Transforms
            FilterPlacement
            Simply
            Equality filter
    Direct (flow based) execution in main - remove the FilterPlacement and BGP optimizer to an optimize step. 
     */
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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