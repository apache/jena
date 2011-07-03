package dev ;

public class DevARQ
{
    // FILTER(?x=?y) optimization - at least FILTER(sameTerm(?x,?y))
    // FILTER(?x=<x> && ...) does not push down on the ?x=<x>
    // Do FILTER placement before filter equality -- but dnager of breaking up BGPs.
    
    // QueryEngineHTTP - content negotiation, Apache httpClient.
    
    // Langbase - calls EventManager.send... but Turtle prefixes done after (read is not finished)
    //   JenaReaderRIOT to do the event? 
    // One token call to Tokenizer.parse -- Replace SSE.parseNode stuff.
    
    // Split pom into:
    //   downloadmaker
    //   Everything else.
    
    // Rework documentation - extensions are now SPARQL 1.1-isms
    // Update library function page
    
    // Config assembler.
    //   [ arq:queryTimeout "123" ] 
    // vs:
    //   [ ja:feature arq:queryTimeout ; ja:value "123" ]
    //   [ ja:feature "arq:queryTimeout" ; ja:value "123" ]
    // Allows non-URIs more easily.
    //   ja:feature "arq:queryTimeout=123"
    
    // In-memory query to use reordering.
    
    // Timeout: content info - 
    //  set in QueryExecutionBase constructor
    //    (or QueryExecutionFactory.make(Query query, Dataset dataset, Context context))
    //   1/ copy in dataset context earlier, not in QueryEngineMain.setupContext
    //   2/ Move to QueryExecutionBase constructor.
    
    // 18.
    
    // OpExecutor example.
    // Datasetfactory.create() -> autocreate graphs?
    
    // Optimization: remember (some!) query executions because index-join causes a lot of repeats.
    
    // ---- Projects
    // * FileManager2
    //     Tests - need testing/Atlas/... or tmp/
    //     Work out what to keep from FileUtils.
    //     FileUtils2.
    //     ?? WebContent to work on ContentTypes, not strings. (what about default charsets?)

    // * Generalized union query
    //   DatasetGraph to understand union graph name 
    // * Generalized dynamic datasets
    //     Union graph (OpExecutor.specialcase)
    //     Named defaut graph (OpExecutor.specialcase)
    //     Pull up from TDB - DynamicDatasets (quads and BGPs and paths) 

    // * RIOT Output
    // * RIOT I/O architecture
    // Hard wired N-Triples/N-Quads parser. LangNTriples2 - Tokenize for N-triples tokens only.
    // NT-Perf next:
    //   TokenizerText seems to be nearly as fast a LangNTriples4 so where is the time going in RIOT?
    //   Particularly Node literals.
    //    ** Node creation  125KTPS with, 200KTPS without.
    //    ** LangNTriples4  265K
    
    // DynamicDS
    //  Reverted to dataset rewrite. See [[DynDS]] in QueryEngineTDB and QueryUnionRead
    //  Migrate to ARQ.
    //    Pass a graph up to be the active graph of the QueryExecution -> change to QueryEngineMain.
    //  Does not work for: FROM <urn:x-arq:DefaultGraph> FROM <urn:x-arq:UnionGraph>
    //  Have a magic dataset implementation that can hold hidden graphs (i.e. not is listNodes but show in getGraph) 
    //  Propagate  <urn:x-arq:DefaultGraph> FROM <urn:x-arq:UnionGraph>
    //   OpExecutor in ARQ need "specialcase" fixing.??
    
    // Dataset which allows graph to be added around an undelying dataset. 
    
    // symUnionDefaultGraph and DESCRIBE
    // Example with query timeout.
    
    // Configuration generally.
    //   Via assemblers ==? 
    //   Via a global file.
    
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
    // MIME type negotiation for QueryEngineHTTP.execSelect.
    
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
    
    // Canonicalize
    //   IRIs - corect, best %-encoding
    //   Numbers to decimals, not integers (not default!)
    
    // ----
    // Listners for dadaset changes -> drive LARQ.
    
    // SPARQL parser; reuse charstream objects? using Reinit()  
    // JavaCharStream allocates a 4k char buffer on every call.
    // Pool of parsers.
   
    // OpTopN - OpExecutor.execute(OpTop).
    // BSBM Explore/Update
    // Remote SPARQL Update 
    
    // Mem dataset with union graph
    // Union graph for all in query execution sequence.
    // ** Documentation - assemblers for datasets
    // qExec.getContext().set(TDB.symUnionDefaultGraph, true) ;
    
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
    // Base 64 :: Apache commons codec.

    // ---- SPARQL 1.1
    // Update: can't use same bNode label in template and pattern for DELETE/INSERT.
    // BINDINGS execution
    // Aggregates to return iterators.
    
    // Property path rewrites as per spec (i.e. earlier than evaluation)

    // ---- Pipeline:
    // Architecture: 
    // Canonicalization: Fix URIs. () [] SPC
    // Canonicaize IRIs (see IRI RFC). http://tools.ietf.org/html/rfc3986#page-38 (IRI missing in lib!)
    //   De-% unreserved characters.
    // Number canonicalization.
    //   Always to decimal?
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
