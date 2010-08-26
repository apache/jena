package dev;

public class DevARQ
{
    // Update request execution.
    // Update migration.
    // uparse : Submission
    
    // Update execution.
    // Transition: SUBMISSION
    // --------
   
    // INSERT/DELETE DATA [INTO? <uri>] {} => INSERT/DELETE DATA { GRAPH <uri> {}}

    // INSERT INTO? <uri> {} (WHERE {})? => WITH <uri> INSERT {} WHERE {}  
    // DELETE FROM? <uri> {} (WHERE {})? => WITH <uri> DELETE {} WHERE {}  
    
    // MODIFY [<uri>*] D{} I{} W{} => WITH* D{} I{} W{}
    
    // LOAD <url> (INTO <uri>)? ==> LOAD (DEFAULT | INTO GRAPH <uri>)

    // CLEAR / CREATE / DROP 
    //    GRAPH <uri> no change
    //    empty => DEFAULT
    // --------
    
    // rename QueryParseException or sort out an UpdateException
    // and a geneal name check.
    
    // Check for out-of-scope group vars.
    
    // Property path test cases
    // Aggregates to return iterators.
    
    // ---- Property paths
    // Path tests: WorkSpace/PropertyPathTests/
    // Transform: paths for new operators. e.g. {2}
    //   {N,} to be {N} UNION {0,N}
    
    // ---- Expr
    // Scalar and column expressions
    
    // ---- Union transformation
    // Transform rewrites BGP to (graph ?_ bgp)
    // Transform rewrites QuadBlock to (graph ?_ bgp)
    // Implicit default graph and named union.
    // Next:
    // ** (graph <unionGraph> BGP) rewrites the BGP but leaves the (graph <union> ...) wrapper.
    //  ==> (graph <unionGraph> (distinct (graph ?_ bgp)))
    // More runtime processing and less transformation?
    // Or just strip (graph <union> ...) on the way past.
    
    // Tests for unionTransformation
    //   TestUnionTransformTriples
    //   TestUnionTransformQuads
    //   TestUnionGraph
    // ** Enable in TS_Algebra.
  
    // ---- Testing
    // ?? Exception in thread "main" com.hp.hpl.jena.sparql.junit.QueryTestException: TestItem with no name (http://www.w3.org/2009/sparql/docs/tests/data-sparql11/negation/manifest#subset-01)
    // Manifest has :subset-1 ptr to :subset-01

    // ---- Misc
    // DatasetGraph.find/4 : what about in NG only?  findNG/4
    
    // Should be able to now JoinClassify with OpModifier (see JoinClassify.isLinear) [but is it worth it?]
    
    // ---- RIOT
    // Skip on bad terms / stop on bad term is choice of errorhandler.
    // Output bad term quads/triples to special sink.
    //   Option passing (old Jena style).
    //   Recovery parsing - scan to DOT?
    // NT & ASCII
    // StringUTF8
    // RiotLoader

    // Filename in log messages
    // Or log at start.
    
    // RIOT document:
    //   RiotReader, DatasetLoader
    // RIOT output: FmtUtils, 

    // ---- Build
    // Build: use maven resources for the etching of version.  
    // Tests:
    // Re-enable normalization tests
    //     POM -> run tests TS_*
    //   Is this or more run from ARQTestSuite?

    // ---- Optimization
    // 1/ Enable sameTermString optimization
    // 2/ If an equality is repeated do once only.
    // 3/ Amalgamation: BGPs, Quads, Sequences.
    // 4/ Assign squashing : assign as rename. (assign ((?x ?y)))
    // 5/ Optimise FILTER(?x=?y) assuming ?x and ?y must be bound.

    // ---- Documentation
    // Documentation for CSV etc.

    // ---- [quad paths]

    // ---- MicroAPI.
    // QueryBuilder.
    //   results = query().select(vars).pattern().filter().groupBy().agg()

    // Sort order tracking 
    
    // NodeFactory == SSE => Merge
    
}
