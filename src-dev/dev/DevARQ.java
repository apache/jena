/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 */

package dev;


public class DevARQ
{
    // ==== 2.8.1
    // SELECT * and NOT EXISTS: don't look in the pattern for variables
    // SELECT * { ?a :p ?v  NOT EXISTS { ?a :q ?w . FILTER(?v>23) } }
    
    // ==== ARQ
    // JMX
    //   Query count
    //   Last query
    //   (list of queries)
    //   ??Datasets opened, closed
    //   Remote queries made
    // ----
    
    // Improve PathLib.ungroundedPath
    // e.g. if first step is a URI, use this to seed the process
    
    //   TransformFilterPlacement
    //   GraphStore API?
    
    // == Test
    // Convert test suite by manifest to JUnit4. JUnit4TestAdpter
    
    // Library
    // TEMP : dump necessary copies in c.h.h.j.sparql.lib until whole thing is sorted out.
    //   Combine StringUtils and StrUtils.

    // NodeFactory == SSE => Merge

    // === Optimization
    // Amalgamation: BGPs,Quads, Sequences.
    // TransformEqualityFilter ==> disjunctions as well.
    // Assign squashing : assign as rename. (assign ((?x ?y)))
    // Disjunction of equalities => union.
    
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
}
