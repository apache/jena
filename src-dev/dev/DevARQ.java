/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 */

package dev;

public class DevARQ
{
    // Documentation for CSV etc.
    
    // **TestDatasetGraph all operations (check for unsupported etc). 
    // Really simple triples+Quads impl.
    // TestDataset, TestDataSource inc replaceNamedGraph

    // deleteAny(Quad) in to include wildcards.
    
    // Convert to quadism even for ARQ query if suitable dataset
    // TransformUnionQuery
    //   TestCases
    
    // Dump dataset as:
    //  NQuads
    //  TriG
    //  SPARQL Update
    
    // ==== ARQ 2.8.4 dev

    // ====
    
    // ARQ: check variable scope
    //   Groups, SELECT expressions
    //   SELECT expressions error to reuse a variable name.
    //   Aggegator upgrade and specifc operators
    //   Custom agregators
    
    // ----
    
    // == Migration:
    // Atlas, JSON

    // Example of OpExecutor
    // Deprecate StageGenerator
    
    // SPARQL/Update of a ja:RDFDataset - new graphs don't appear? 
    
    // Property paths: { ?x !rdf:type ?y }
    // What about ^ reverse?
    //{ ?x !(^rdf:type|rdf:type) ?y }
    // ^ not needed (either it's forward mode adnd can't match else backwars mode further out)
    // Test with grounded ?x and ?y
    
    // Run ref engine and main engine in a test - remove ref only tests
    
    // MicroAPI.
    //   results = query().select(vars).pattern().filter().groupby().agg()
    //    or build algebra this way.
    // ARQ warnings on cross product and unused project vars 
    
    // ==== ARQ
    // JMX
    //   Document
    //   Query count
    //   Last query
    //   (list of queries)
    //   ??Datasets opened, closed
    //   Remote queries made
    // ----
    
    // OpJoin optimization: If one side is a table, then sequence that first.
    
    // Library
    //   Combine StringUtils and StrUtils.

    // NodeFactory == SSE => Merge

    // === Optimization
    // Amalgamation: BGPs, Quads, Sequences.
    // Assign squashing : assign as rename. (assign ((?x ?y)))
    
    // Initial bindings && Initial table (do as iterator of initial bindings)
    // { LET (...) pattern } becomes (join [assign ((...)) (table unit)] pattern
    //    which can be simplified to a sequence.
    //  Generate a sequence always? 
    
    // == Clearup
    // Prefer OpExecutor much more.
    //   Skelleton for StageGenerator
    
    // ---- [quad paths]
    
    // ---- SPARQL/Update
    // GraphStoreFactory.create clones the dataset, so hiding changes to the dataset.
    // Dataset.sync, Dataset.close as well as GraphStore.sync, GraphStore.close.

    // ---- OpAssign - needs expression prepare (for function binding)?
}
