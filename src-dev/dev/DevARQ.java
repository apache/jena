
package dev ;

public class DevARQ
{
    // Canonicalization: 
    //   faster forms
    //   double, float, date*
    // RIOT (etc) closing InputStream
    // Bad character encoding - exception but line/col = 1/1
    
    // RIOT: cmd line : bad parse -> ???
    // ReportRemoteVirtuoso: StAX does not parse - times out too early? 
    // XSDDuration
    
    // **Next
    // ** Sort out GraphStore.
    // ??setSilentMode - reason to keep?
    // ?? remove
    // ?? merely a DSG container?
    //   ?? c.f. UpdateExecution so no need for GS? UpdateProcessor vs UpdateExecution
    
    // ** Update documentation
    // No need for CREATE SILENT, DROP SILENT
    
    // Error handling and exception catching. LanguageParseException
    // (old name QueryParseException : superclass)
    // "QuerySolution"

    // BINDINGS
    //   Execution.

    // Strictisms:
    //   scoping checks on variables.
    //   SELECT * rules

    // mvn tests (4042) and Eclipse tests (3270) differ in size.

    // Library: Var->Var (VarRename), bNode->var (UpdateEngine), Converters
    // See NodeConverters, canonicalization -- riot.pipeline

    // JSON - write compact
    
    // result set isomorphism.
    
    // ---- SPARQL Update

    // Check: Inserting into explicitly named default graph should be made to
    // work.
    // Implicit empty graph mode even for dataset of graphs.

    // Label checking: can't use same bNode label in template and pattern
    // for DELETE/INSERT.

    // *** Pipeline: number canonicalization : Sink
    
    // ---- SPARQL Query
    // Check for out-of-scope group vars.
    // ElementSubQuery takes a Query argument so called after all sub-parsing
    // -> check then or check via visitor later.
    // :: visitor - other checks can be done.

    // count(distrinct ...) to have a smaller foot print.  hashing? order tracking?
    
    // Property path test cases
    // Aggregates to return iterators.

    // Syntax for lists: LIST(?list, ?index, ?member)

    // ---- Property paths
    // Path tests: WorkSpace/PropertyPathTests/
    // Transform: paths for new operators. e.g. {2}
    // {N,} to be {N} UNION {0,N}

    // ---- Core
    // Memory DatasetGraph to support Quad.unionGraph, defaultGraph and
    // defaultGraphGenerated
    // Reenable tests in TestGraphMem

    // ---- Expr
    // Scalar and column expressions

    // ---- Union transformation
    // Transform rewrites BGP to (graph ?_ bgp)
    // Transform rewrites QuadBlock to (graph ?_ bgp)
    // Implicit default graph and named union.
    // Next:
    // ** (graph <unionGraph> BGP) rewrites the BGP but leaves the (graph
    // <union> ...) wrapper.
    // ==> (graph <unionGraph> (distinct (graph ?_ bgp)))
    // More runtime processing and less transformation?
    // Or just strip (graph <union> ...) on the way past.

    // Tests for unionTransformation
    // TestUnionTransformTriples
    // TestUnionTransformQuads
    // TestUnionGraph
    // ** Enable in TS_Algebra.

    // ---- Testing
    // ?? Exception in thread "main"
    // com.hp.hpl.jena.sparql.junit.QueryTestException: TestItem with no name
    // (http://www.w3.org/2009/sparql/docs/tests/data-sparql11/negation/manifest#subset-01)
    // Manifest has :subset-1 ptr to :subset-01

    // ---- Misc
    // DatasetGraph.find/4 : what about in NG only? findNG/4

    // Should be able to now JoinClassify with OpModifier (see
    // JoinClassify.isLinear) [but is it worth it?]

    // ---- RIOT
    // Skip on bad terms / stop on bad term is choice of errorhandler.
    // Output bad term quads/triples to special sink.
    // Option passing (old Jena style).
    // Recovery parsing - scan to DOT?
    // NT & ASCII
    // StringUTF8
    // RiotLoader
    // Tokenizer - mode for emit whitespace and comments, and tokens for all other specials (tricky: + and -)

    // JSON - systemative convenience operations on numbers
    // JSON.parse - check that it uses a Peekreader over a RIOT parser.

    // Filename in log messages
    // Or log at start.

    // RIOT document:
    // RiotReader, DatasetLoader
    // RIOT output: FmtUtils,

    // ---- Build
    // Build: use maven resources for the etching of version.
    // Tests:
    // Re-enable normalization tests
    // POM -> run tests TS_*
    // Is this or more run from ARQTestSuite?

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
    // results = query().select(vars).pattern().filter().groupBy().agg()

    // Sort order tracking

    // NodeFactory == SSE => Merge

}
