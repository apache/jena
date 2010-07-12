/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 */

package dev;

public class DevARQ
{
    // SPARQL Update
    
    // Static check of legal vars in SELECT with group.
    
    // QueryEngines MainQuad and RefQuad
    // RefQuad:  execution of quad patterns, graph+subquery 
    // MainQuad: execution of quad patterns 
    //   Tests to run all engines.
    
    // RIOT
    // Version mgt
    // RIOT.init()
    // Skip on bad terms / stop on bad term is choice of errorhandler.
    // Output bad term quads/triples to special sink.
    //   Option passing (old Jena style).
    //   Recovery parsing - scan to DOT?
    // NT & ASCII

    // Filename in log messages
    // Or log at start.
    
    // RIOT document:
    //   RiotReader, DatasetLoader
    
    // Atlas:
    // Move stuff from sparql.util/sparql.lib to Atlas.
    
    // Extend:
    //   Add bare date/datetime to tokenizer?? Syntax?
    //   Generalize and trap non-triples at emission time.
    
    // Dataset[Graph].clear
    // Parsing SPARQL : Use RIOT checker.
 
    // Non-strict
    //   leading digits in blank node ids (need strict mode in Tokenizer)
    //   ??
    
    // Build: use maven resources for the etching of version.  
    // Tests:
    // Re-enable normalization tests
    //     POM -> run tests TS_*
    //   Is this or more run from ARQTestSuite?

    // 1/ Enable sameTermString optimization
    // 2/ If an equality is repeated do once only.

    // RIOT
    //   WebReader: Read from URL, Content negotiation.
    //   Options: pass/reject bad URIs, bad literals. -> "SKIP", "PASS", "FAIL"
    
    // Joseki - expose "-print=opt" in validator
    
    // Check the assembly: javadoc,lib/*sources*
    // Zero-length paths
    
    // Reenable i18n tests: TS_DAWG, DAWG_Final/maniest-evaluation.ttl
    // Removed dups between DAGE and DAWG-Final
    
    // Jena: Consolidate treatement of white space for floaf/double/num/abstractdate
    // Jena: upgrades 
    // Xerces 2.7.1 (released 2005-06) --> 2.9.x
    // ICU4J 3.4.4 (released 2006-02) --> 4.4.x (but it's a lot larger)
    // Woodstox 3.2.9 (released 2009-05) --> 4.0.x (actually a dependency of ARQ)
    // ARQ: Upgrade Lucene to 3.0
    
    // OpAsQuery : aggregates
    
    // == SPARQL 1.1 Update
    //   Syntax tests
    
    // ----
    // http://www.w3.org/TR/xmlschema11-2/ : precisionDecimal : dayTimeDuration : yearTimeDuration 

    // Documentation for CSV etc.

    // Union Transform,  TransformUnionQuery
    //   TestCases
    //   The use of a bNode in (graph) needs coping with the distinct.
    //   Or a "really don't bind" treatment of that bnode. Var.ANON
    //   ref.Eval and main.OpExecutor.execute(OpGraph)->QueryIterGraphInner.nextIterator
    //   Modify bindings (all!) for Var.ANON 
    // Swapping DatasetGraph
    
    // MicroAPI.
    //   results = query().select(vars).pattern().filter().groupBy().agg()
    //    or build algebra this way.
    // ARQ warnings on cross product and unused project vars 
    
    // Library
    //   Combine StringUtils and StrUtils.

    // NodeFactory == SSE => Merge

    // === Optimization
    // Amalgamation: BGPs, Quads, Sequences.
    // Assign squashing : assign as rename. (assign ((?x ?y)))
    
    // Initial bindings && Initial table (do as iterator of initial bindings)
    // { LET (...) pattern } becomes (join [assign ((...)) (table unit)] pattern
    //    which can be simplified to a sequence.
    
    // == Clearup
    // Prefer OpExecutor much more.
    //   Skelleton for StageGenerator
    
    // ---- [quad paths]
    
    // ---- SPARQL/Update
    // GraphStoreFactory.create clones the dataset, so hiding changes to the dataset.
    // Dataset.sync, Dataset.close as well as GraphStore.sync, GraphStore.close.
}
