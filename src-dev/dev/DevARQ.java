/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 */

package dev;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.Maker ;

import com.hp.hpl.jena.graph.Node ;

public class DevARQ
{
    // RIOT bug
    // Look for TODOs
    // NTriples checks line structure twice, once in parser, once in createTriple.
    // ParserParamObject
    //     Maker
    //       Turtle - resolved URIs, prefixes
    //                Prefixes - resolve URI, not a token (or token->Node, discard Node)
    //       N-triples - raw URIs,
    //     Checker
    //     ErrorHandler
    //     Exceptions only from error handlers
    //     Sink<Tuple<T>> -> Sink<Triple>, Sink<Quad> 
    // have a triple, quad checker AFTER emit. Need line/col
    // (Feature of Maker    
    //     Mode: strict, lax
    
    static final class ParserParamObject<T>
    {
        // Or in LangBase
        public final Maker maker ;
        public final ErrorHandler errorHandler ;
        public final Sink<T> sink ;
        public final boolean strict ;
        //public Enum mode ;
        
        public ParserParamObject(Maker maker, ErrorHandler errorHandler, Sink<T> sink, boolean strict)
        {
            this.maker = maker ;
            this.errorHandler = errorHandler ;
            this.sink = sink ;
            this.strict = strict ;
        }
    }

    // Extend:
    //   Add bare date/datetime to tokenizer?? Syntax?
    //   Generalize and trap non-triples at emission time.
    
    // Dataset[Graph].clear
    // Parsing SPARQL : Use RIOT checker.
 
    // Commands and default log4j.
    // Build: use maven resources for the etching of version.  
    
    // Tests:
    // Re-enable normalization tests
    //     POM -> run tests TS_*
    //   Is this or more run from ARQTestSuite?

    // 1/ Enable sameTermString optimization
    // 2/ If an equality is repeated do once only.

    // RIOT
    //   Document
    //   Filename in error messages
    //   SysRIOT.fmtMessage, ParseException
    //   Output bad term quads/triples to special sink.
    //   Checker architecture - Jena-ish IRI tuned.
    //   WebReader: Read from URL, Content negotiation.
    //
    //   Interface checker
    //   Option passing (old Jena style).
    //   Options: pass/reject bad URIs, bad literals. -> "SKIP", "PASS", "FAIL"
    //   Recovery parsing - scan to DOT?
    // RIOT Commands
    //  --sink, --skip, --time, --stats?
   
    
    // Joseki - expose "-print=opt" in validator
    
    // Check the assembly: javadoc,lib/*sources*
    // Zero-length paths
    
    // Reenable i18n tests: TS_DAWG, DAWG_Final/maniest-evaluation.ttl
    // Removed dups between DAGE and DAWG-Final
    
    // == SPARQL 1.1 Query
    // Aggregates 
    //   Sum,Min,Max,Avg for Distinct
    //   Sample, GroupConcat
    
    // regex and str()
    // SameTerm for strings by dataset context. 
    
    // Jena: Consolidate treatement of white space for floaf/double/num/abstractdate
    // Jena: upgrades 
    // Xerces 2.7.1 (released 2005-06) --> 2.9.x
    // ICU4J 3.4.4 (released 2006-02) --> 4.4.x (but it's a lot larger)
    // Woodstox 3.2.9 (released 2009-05) --> 4.0.x (actually a dependency of ARQ)
    // ARQ: Upgrade Lucene to 3.0
    
    // OpAsQuery : aggregates
    
    // Modules: atlas, RIOT(->org.openjena)
    // Migrate: Reifier2, GraphBase2, DatasetPrefixStorage
    // Autoclose of results sets from QueryEngineHTTP
    
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
    
    // SPARQL: check variable scope
    //   Groups, SELECT expressions
    //   SELECT expressions error to reuse a variable name.
    //   Aggegator upgrade and specifc operators
    //   Custom agregators
    
    // SPARQL/Update of a ja:RDFDataset - new graphs don't appear? 
    
    // Run ref engine and main engine in a test - remove ref only tests
    
    // MicroAPI.
    //   results = query().select(vars).pattern().filter().groupBy().agg()
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

    // ---- OpAssign - needs expression prepare (for function binding)?
}
