package dev ;


public class DevTDB
{
    // DynamicDS
    // ** Make the dft graph of a union graph query.
    //    Pass a graph yp to be the active graph of the QueryExecution -> chnage to QueryEngineMain.
    // FROM <urn:x-arq:DefaultGraph> FROM <urn:x-arq:UnionGraph>
    // OpExecutor in ARQ need "specialcase" fixing.
    // See [[DynDS] in QueryEngineTDB

    
    // http://sna-projects.com/krati/
    
    // Check: BlockMgrMapped
    // ** remove force/sync (non-)distinction
    // MappedByteBuffer.sync, not just sync on the FileChannel
    
    // Finish, integrate, switch to new Dataset builder.
    //  Todo init().
    // SolverLib - convert Nodes to NodeIds once, as BGP compiled.
    // check for circularities?
    
    // Loader -> warning on bad literals?
    //  Check?  Test cases.
    
    // TDBLoader.load(dsg, input, lang, base, showProgress)
    
    // ** Reorganise documentation.
    // Page on union dataset.
    // Tutorial.
    // Combine with ARQ? Fuseki? One Trail.
    
    // New setup
    //    See IndexFactory for bulkloader2
    //    Factory for common basic object (lots of args).
    //    Versions with built-in defaults.
    //    NodeTableFactory and makers in the test area.
    
    // listSubjects can be done fast for TDB
    
    // ---- BulkLoader2
    // Tune sort --buffer-size=50%
    // Prefixes to be set during bulk loading 1 & 2
    // RIOT parser issue?
    // On reading, suppress duplicates (adjacent). 
    
    // BPT
    //  .truncate : release all blocks and create a new (empty) tree. 

    // Abort long running query.
    // PrefixMapping = PrefixMap
    // Version of PrefixMap that impls (wrapper) PrefixMapping.  
    
    // ---- Documentation
    // Assembler.
    // http://openjena.org/wiki/TDB/Assembler#RDFS
    // http://openjena.org/wiki/TDB/JavaAPI#Concurrency
    // http://openjena.org/wiki/TDB/Concurrency
    // and ref to ARQ.
    // http://openjena.org/wiki/TDB/Assembler to document unionDefaultGraph
    
    // --explain.
    // http://openjena.org/wiki/TDB/Configuration
    // http://openjena.org/wiki/TDB/Optimizer
    // Update: http://openjena.org/wiki/TDB/Optimizer#Investigating_what_is_going_on
    
    // == Misc
    //    Tidy up wiki (esp "Use from java" - use datasets, show loader use)
    
    // ** Partial ranges : S P ?o , ?o start, ?o finish 
    
    // ** Advanced block work - free chain management.

    // =====
    // Enable FILTER assignment for strings (and numbers?) via dataset context setting?
    // -- Tuples
    // Interface, TupleFactory, TupleImpl
    // TupleMask (or TupleImpl itself has a length field?? TupleMask(T[] or Tuple<T>)) 
    // Sort : with colmap?
    // TupleSlice
    
    // ---- Pipeline
    // Weak inferencing (in query rewriting):
    // rdfs:subClassOf (aux table).
    // rdfs:subPropertyOf (aux table).

    // ---- Optimizer
    // Early truncation of patterns
    //  ?s <p> <foo> . ?s <q> ?v . ?s <r> ?x
    // Favour connected next triple pattern (but grounding makes this less relevant) 
    // If <r> has no solutions fall back to triple pattern 1
    // Just need to keep var -> first def mapping but all mentions may be useful. 
    // (Idea from Alisdair)
    
    // ---- NodeId:  
    // Bit 0: 0 - 63 bits of id (hash!) or block allocation for cluster.
    // Bit 0: 1 - inline
    // Schema compatibility needs to handle this carefully.
}
