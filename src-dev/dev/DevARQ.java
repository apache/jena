package dev ;

public class DevARQ
{
    // ---- Projects
    // * FileManager2
    //     Tests - need testing/Atlas/... or tmp/
    //     Work out what to keep from FileUtils.
    //     FileUtils2.
    //     ?? WebContent to work on ContentTypes, not strings. (what about default charsets?)
    // * Generalized union query
    // * RIOT Output
    // * RIOT I/O architecture
    // * OutStreamUTF8.
    // Apache commons codec for MD5 etc.
    
    /*
    JSON CONSTRUCT {
        { ?name: [ ?lat, ?long ] }
      }
      WHERE {
        ?x rdfs:label ?name ;
           geo:lat ?lat ;
           geo:long ?long ;
      }
    */
    
    // ---- RIOT
    // Parallel parser
    // Extract public API (RiotReader etc) - document
    // Closing InputStream
    // Errors after file name! Print file name once if error.
    // Recovery parsing - scan to DOT?

    // riot --inputLabels -> use input labels
    //   input choices: normal(generate), preserve labels, decoded safe to internal.
    //   output choices: normal(short label _:b0), preserve (if input = perserve => same else safe internal), safe internal, raw (illegal)
    // Safe needs to be smarter?
    // Tests

    // riot --canonicalize --normalize
    // riot --bnodeIRIs=on|off
    // riot --rdfs
    
    // ----
    // Listners for dadaset changes -> drive LARQ.
    
    // SPARQL parser; reuse charstream objects? using Reinit()  
    // JavaCharStream allocates a 4k char buffer on every call.
    // Pool of parsers.
   
    // OpTopN - OpExecutor.execute(OpTop).
    // BSBM Explore/Update
    // Remote SPARQL Update 
    
    // JENA-29 : Cancellation --> timeouts
    // JENA-47 : Timeout query
    // JENA-48 : QueryIterAbortCancellationRequestException - is this needed?
    //    Check in sort that don't get one rsult (after a lot of sorting).
    // JENA-49 : immediate cancellation if not .hasNext called yet.
    // And effect on XML results.
    
    // Mem dataset with union graph
    // ** Documentation - assemblers for datasets
    
    // HTTP result set iterator to know when it's closed.
    
    // OpExecute example.
    
    // More test in DatasetGraphTests
    // Rename Quad.defaultGraphIRI?
    // Parsers use: Quad.defaultGraphNodeGenerated
    //   Should find/4 do the same? Via:
    // DatasetGraphBase.triples2quadsDftGraph
    
    // SSE - move to RIOT based tokenizer.
    // TokenizeText:
    // Just STRING, no CNTL=>KEYWORD = Symbol, catchall Keyword/Symbol
    
    // Apache codec.
    
    // .filemanager
    // FileUtils, TypedStream conversion.
    // Check use of FileUtils.
    
    // >>>> Tasks

    // Algebra.asUnionQuery .. to access the functionality
    
    // Path evaluation as per spec.
    // tdbloader2; filenames.
    
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
    // Canonicaize IRIs (see IRI RFC). http://tools.ietf.org/html/rfc3986#page-38 (IRI missing in lib!)
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
