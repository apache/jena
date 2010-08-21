package dev;

public class DevARQ
{
    // SPARQL update parsing
    // Check that IRIs are checked.
    
    // Property path test cases
    // Aggregates to return iterators.
    
    // Interface for addDefaultGraphIRI, addNamedGraphIRI and reuse FROM and USING
    
    // Path tests: WorkSpace/PropertyPathTests/

	// Optimise FILTER(?x=?y) assuming ?x and ?y must be bound.
	
    // Transform: paths for new operators. e.g. {2}
    //   {N,} to be {N} UNION {0,N}
    
    // Tests for negated property sets
    
    // Inference pipeline
    // TDB+Inference
    //   TDB/BulkLoader.loadTriples$ (loadQuads$)
    //     to add a inference sink wrapper.
    
    // Union transformation
    // Transform rewrites BGP to (graph ?_ bgp)
    // Transform rewrites QuadBlock to (graph ?_ bgp)
    // Implicit default graph and named union.
    //   Split in two?
    
    // Next:
    // ** (graph <unionGraph> BGP) rewrites the BGP but leaves the (graph <union> ...) wrapper.
    //  ==> (graph <unionGraph> (distinct (graph ?_ bgp)))
    // More runtime processing and less transformation?
    // Or just strip (graph <union> ...) on the way past.
    
    // OpExecutor for BGP over union graph.
    // OpExecutor.execute(OpQuadPattern, )
    
    // Tests for unionTransformation
    //   TestUnionTransformTriples
    //   TestUnionTransformQuads
    //   TestUnionGraph
    // ** Enable in TS_Algebra.
  
    // Comparison (ORDER BY) costs
    // Holger's slow datatype example.
    
    // PropertyPaths
    
    // Exception in thread "main" com.hp.hpl.jena.sparql.junit.QueryTestException: TestItem with no name (http://www.w3.org/2009/sparql/docs/tests/data-sparql11/negation/manifest#subset-01)
    // Manifest has :subset-1 ptr to :subset-01
    
    // Inference
    // StringUTF8
    // RiotLoader

    // Thorsten's suggestion for POM.xml.
    // Build: sources, javadoc to elsewhere, mvn_update
    //   lib-src
    // documentation: API -> dataset, not model.
    
    // DatasetGraph.find/4 : what about in NG only?  findNG/4
    
    // Scoping:
    // Consider wrapping passed-in binding for project subquery, rather than a renaming scheme.
    
    // Should be able to now JoinClassify with OpModifier (see JoinClassify.isLinear) [but is it worth it?]
    
    // SPARQL Update
    // Static check of legal vars in SELECT with group.
    
    // QueryEngines MainQuad and RefQuad
    // RefQuad:  execution of quad patterns, graph+subquery 
    // MainQuad: execution of quad patterns 
    //   Tests to run all engines.
    
    // ---- RIOT
    // Version mgt
    // Add ??read(Model model, file) c.f. DatasetLoader
    // 
    // Skip on bad terms / stop on bad term is choice of errorhandler.
    // Output bad term quads/triples to special sink.
    //   Option passing (old Jena style).
    //   Recovery parsing - scan to DOT?
    // NT & ASCII

    // Filename in log messages
    // Or log at start.
    
    // RIOT document:
    //   RiotReader, DatasetLoader
    // RIOT output: FmtUtils, 
    
    
    // Atlas:
    // Move stuff from sparql.util/sparql.lib to Atlas.
    //   Remaining: Timer
    
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
    // Tests
    
    // MicroAPI.
    //   results = query().select(vars).pattern().filter().groupBy().agg()
    //    or build algebra this way.
    // ARQ warnings on cross product and unused project vars 
    
    // Library
    //   Combine StringUtils and StrUtils.

    // Sort order tracking 
    
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
