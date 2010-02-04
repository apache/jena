/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 */

package dev;



public class DevARQ
{
    // BGP ordering and filter placement in memory models. 
    // Every optimization operation needs an on/ioff switch.
    // Stats handler enough?
    
    // Example of OpExecutor
    // Deprecate StageGenerator
    
    // Check version info works
    
    // SPARQL/Update of a ja:RDFDataset - new graphs don't appear? 
    
    // Property paths: { ?x !rdf:type ?y } 
    
    // De-duplicate aggregator code.
    // Junit3 ==> Junit4
    
    // Run ref engine and main engine in a test - remove ref only tests
    
    // CSV and TSV
    //   Reengineer code.  Proper escaping.  strSafe in CSVOutput
    //   Input: ResultSetFactory.load
    //   Tests
    
    // MicroAPI.
    //   results = query().select(vars).pattern().filter().groupby().agg()
    //    or build algebra this way.
    // ARQ warnings on cross product and unused project vars 
    
    // public class FmtUtils
    // Consider a temporary SerialzationContext that does not abbreviate bNodes.
    // PrefixMapping impl around PrefixMap.
    
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
    
    // Improve PathLib.ungroundedPath
    // e.g. if first step is a URI, use this to seed the process
    
    // == Test
    // Convert test suite by manifest to JUnit4. JUnit4TestAdapter
    //   Partial: all the Test* use JUnit4TestAdapter
    //   To do: Organise suites better.
    //          TS ==> JUnit4
    //          No more JUnit3.
    
    // Library
    // TEMP : dump necessary copies in c.h.h.j.sparql.lib until whole thing is sorted out.
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
