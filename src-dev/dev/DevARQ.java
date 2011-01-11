package dev ;

public class DevARQ
{
    // round(1.3) -> "1"^^xsd:decimal.
    // floor, ceil.
    
    // Apache codec.
    
    // .filemanager
    // FileUtils, TypedStream conversion.
    // Check use of FileUtils.
    
    // Most Locator.open don't know about types => extensions.
    // Super class for "guessing" -> RIOT
    
    
    // >>>> Tasks

    // OutputLangUtils
    
    // Algebra.asUnionQuery .. to access the functionality
    
    // Path evaluation as per spec.
    
    // Multithreaded parser.
    
    // ?? bNode label checks (UPDATE: can't use same bNode label in template and pattern for DELETE/INSERT.)

    // Pull up TransformDynamicDataset, TransformGraphRename from TDB.
    // ---- Union Transform
    // Tests for unionTransformation
    // TestUnionTransformTriples
    // TestUnionTransformQuads
    // TestUnionGraph
    // ** Enable in TS_Algebra.
    
    // Better access to union query??? 
    // By transform, query context.
    //  Version for tru=iples evaluation (TransformUnionQuery) and quads evaluation (A2 from TDB)
    // Document - move from TDB

    // <<<< Tasks

    // QueryParseException for updates is confusing.
    //   All QueryException  -> SparqlException or LanguageParseException
    
    // Prologure from RIOT
    // PrefixMap from RIOT
    
    // Migrations/enable:
    //   TransformUnionQuery
    //   TransformDynamicDataset
    //   TransformGraphRename
    //   TransformPropertyPathFlatten
    
    // ---- Documentation
    // Downplay: PathLib.install.
    // Legacy StageGenerator (http://openjena.org/ARQ/arq-query-eval.html)
    // src-examples of OpExecutor+QueryEngine
    // http://www.openjena.org/wiki/ARQ/Concurrency
    // add qparse,uparse to wiki/ARQ/
    // Documentation for CSV etc.
    // Supported types

    // DatasetGraph : connections
    
    // Event type -> list of parts vs regex filtering (?)
    // Base 64, MD5, SHA :: Apache commons codec.

    // ---- SPARQL 1.1
    // Update: can't use same bNode label in template and pattern for DELETE/INSERT.
    // BINDINGS execution
    // Aggregates to return iterators.
    
    // Property path rewrites as per spec (i.e. earlier than evaluation)
    //   Path tests: WorkSpace/PropertyPathTests/
    //   Transform: paths for new operators. e.g. {2}
    //   {N,} to be {N} UNION {0,N}

    // ---- Pipeline:
    // Architecture: 
    // Canonicalization: Fix URIs. () [] SPC
    // Number canonicalization.
    // Canonicalized lang tags?
    //   owl:equivalentClass, owl:equivalentProperty,
    //   owl:inverseOf, owl:SymmetricProperty, owl:TransitiveProperty
    //   rdfs:subClassOf (aux table).
    //   rdfs:subPropertyOf (aux table).

    // ---- Events.
    //  Hierarchy.

    // ---- "ARQ 3"
    // Minor changes that break compatible in some way 
    // -- Custom functions
    //   E_Function intercepts at evalSpecial to maintain compatibility.
    //   Should really change Function.eval to strict functions (eval'ed arguments and no binding).   
    
    // ---- RIOT
    // Extract public API (RiotReader etc) - document
    // closing InputStream
    // Bad character encoding - exception but line/col = 1/1
    //   Feed into the per-byte UTF-8 decoder?
    // Filename in messages.
    // Errors after file name! Print file name once if error.
    // Recovery parsing - scan to DOT?
    
    // XSDDuration

    // ---- Commands
    // --data to accept TriG and N-Quads
    
    // Result set isomorphism - need backtracking
    
    // Syntax for lists: LIST(?list, ?index, ?member)

    // ---- Core
    // Memory DatasetGraph to support Quad.unionGraph, defaultGraph and

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

    // ---- Build
    // Build: use maven resources for the etching of version.

    // ---- Optimization
    // 1/ Enable sameTermString optimization
    // 2/ If an equality is repeated do once only.
    // 3/ Amalgamation: BGPs, Quads, Sequences.
    // 4/ Assign squashing : assign as rename. (assign ((?x ?y)))
    // 5/ Optimise FILTER(?x=?y) assuming ?x and ?y must be bound.

    // ---- MicroAPI.
    // QueryBuilder.
    // results = query().select(vars).pattern().filter().groupBy().agg()
}
