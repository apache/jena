/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 */

package dev;


public class Dev
{
    // ==== ARQ
    // Convert test suite by manifest to JUnit4.
    // DatasetImpl cache refinement
    // JMX
    //   Query count
    //   Last query
    //   (list of queries)
    //   ??Datasets opened, closed
    //   Remote queries made
    // ----
    
    //   TransformFilterPlacement
    //   GraphStore API?
    
    // == Build
    // Multipel artifacts
    // Make artifacts then publish
    // (pom and extra pom need rewrite rules).
    // No sources or javadoc for arq-extra.
    
    // -- Tidy up
	// Build: "dev" and "main" ==> "main"
    // Two ivy modules.
    
    // -- Build process
    //   Testing - need TS_* files and data.

    // Library
    // TEMP : dump necessary copies in c.h.h.j.sparql.lib until whole thing is sorted out.
    //   Combine StringUtils and StrUtils.

    // NodeFactory == SSE => Merge
    // Systematic execution logging.

    // === Optimization
    // Amalgamation: BGPs,Quads, Sequences.
    // TransformEqualityFilter ==> disjunctions as well.
    // Assign squashing : assign as rename. (assign ((?x ?y)))
    // Disjunction of equalities => union.
    
    // SPARQL/Update - Sort out GraphStores/Datasets
    
    // Initial bindings && Initial table (do as iterator of initial bindings)
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
