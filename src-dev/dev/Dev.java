/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;


public class Dev
{
    // ==== ARQ
    //   list:member speed up
    //   TransformFilterPlacement
    //   GraphStore API?
    
    // == Build
    // Make artifacts then publish
    // (pom and extra pom need rewrite rules).
    // No sources or javadoc for arq-extra.
    
    // -- Tidy up
	// Build: "dev" and "main" ==> "main"
    // Two ivy modules.
    
    // -- Build process
    //   Currently only one jar - arq.jar
    //     arq and arq-extra
    //   Testing - need TS_* files and data.
    // Explain.
    //   Sort out TDB explain and logging.
    
    // Library
    // TEMP : dump necessary copies in c.h.h.j.sparql.lib until whole thing is sorted out.
    //   Combine StringUtils and StrUtils.

    // NodeFactory == SSE => Merge
    // Systematic execution logging.

    // E_Exists(Op).

    // === Optimization
    // Amalgamation: BGPs,Quads, Sequences.
    // TransformEqualityFilter ==> disjunctions as well.
    // Filter placement for quads **  
    // Extend to quads.  Triple=>quad
    // Assign squashing : assign as rename. (assign ((?x ?y)))
    // Disjunction of equalities => union.
    
    // SPARQL/Update - Sort out GraphStores/Datasets
    
    // Initial bindings && Initial table.
    // Src for testing in download?
    // { LET (...) pattern } becomes (join [assign ((...)) (table unit)] pattern
    //   which can be simplified to a sequence.
    //  Generate a sequence always? 
    
    // ---- [quad paths]
    
    // ---- SPARQL/Update
    // GraphStoreFactory.create clones the dataset, so hiding changes to the dataset.
    // Dataset.sync, Dataset.close as well as GraphStore.sync, GraphStore.close.
    

    // ---- OpAssign - needs expression prepare (for function binding)?
    // Other places using a VarExprList?
    // Does prepare really matter if failure is defined as a false for evaluation?
    
    // == SSE
    // Dev: check escapes in Literals and symbols in SSE (ParseSSEBase)
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
