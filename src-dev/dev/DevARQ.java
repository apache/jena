/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 */

package dev;

public class DevARQ
{
    // Check the assembly: javadoc,lib/*sources*
    // Zero-length paths
    
    // Aggregates for SPARQL 1.1
    // Accumulator base classes
    // FunctionEnv everywhere
    // rgex and str()
    // SameTerm for strings by dataset context. 
    
    // Jena: Consolidate treatement of white space for floaf/double/num/abstractdate
    // Jena: upgrades 
    // Xerces 2.7.1 (released 2005-06) --> 2.9.x
    // ICU4J 3.4.4 (released 2006-02) --> 4.4.x (but it'sa lot larger)
    // Woodstox 3.2.9 (released 2009-05) --> 4.0.x (actually a dependency of ARQ)
    // ARQ: Upgrade Lucene to 3.0
    
    // OpAsQuery : aggregates
    
    // Modules: atlas, RIOT(->org.openjena)
    // Migrate: Reifier2, GraphBase2, DatasetPrefixStorage
    // Autoclose of results sets from QueryEngineHTTP
    
    // == SPARQL 1.1 Query
    //   Rework aggregates to better match the formalism in the spec; stream/accumulatros based.
    //   Implement Group_Concat, sample
    
    // == SPARQL 1.1 Update
    //   Syntax tests
    
    // ----
    // http://www.w3.org/TR/xmlschema11-2/ : precisionDecimal : dayTimeDuration : yearTimeDuration 

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
