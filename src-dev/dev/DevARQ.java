/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 */

package dev;

public class DevARQ
{
    // OpAsQuery : aggregates
    
    // Modules: atlas, RIOT
    // Migrate: Reifier2, GraphBase2, DatasetPrefixStorage
    // Autoclose of results sets from QueryEngineHTTP
    
    // == SPARQL 1.1 Query
    //   Rework aggregates to better match the formalism in the spec; stream/accumulatros based.
    //   Implement Group_Concat, sample
    
    // == SPARQL 1.1 Update
    //   Syntax tests
    
    // ----
    // http://www.w3.org/TR/xmlschema11-2/ : precisionDecimal : dayTimeDuration : yearTimeDuration 
    // Version registration: URI + ver string ; apf:version
    
    // Dataset to indicate if plain string and XSD string are the same to affact FILTER(?x="string") optimziation.
    // Does dataset contexts fix this?
    
    // Documentation for CSV etc.
    // Union Transform,  TransformUnionQuery
    //   TestCases
    //   The use of a bNode in (graph) needs coping with the distinct.
    //   Or a "really don't bind" treatment of that bnode. Var.ANON
    //   ref.Eval and main.OpExecutor.execute(OpGraph)->QueryIterGraphInner.nextIterator
    //   Modify bindings (all!) for Var.ANON 
    // Swapping DatasetGraph
    
    // Aggregates
    
    // Dump dataset as:
    //  NQuads
    //  TriG
    //  SPARQL Update
    
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
