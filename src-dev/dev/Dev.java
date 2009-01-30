/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // Quad pattern execution/optimization with GRAPH ?g { .... }
    // FILTER (between(x,y)) => range on index.

    // removeAll (improved)
    //    More test cases needed
    // listSubjects etc
    
    // Documentation
    //  Concurrency policy
    // Ideal, B+Tree should be able to have different orders in leaves and branches.
    
    // Consistency - do not manage in block managers except where MRSW no safe.
    // ==> Reopenable
    // ==> .release(id)
    // ==> Accurate size (?? meaningful beyond isEmpty/notEmpty?)
    // ==> Metablocks.
    //   ==> Moveable roots.
    
    // Tidy up this file :-)
    
    // ---- Interactions with ARQ
    // + Other indexes
    // + SPARQL/Update
    //   Dataset is cloned on a Joseki/SPARQL/update operation
    //   Need ARQ change + TDB to provide a GraphStore.
    
    // ---- Generic TupleIndex<T extends Something> for NodeId or Node
    // e.g. Need mapper from T to TupleIndexRecord to abstract. 
    
    // TDBFactory - cache graphs - graph.close is return to cache (and sync) 
    
    // Even more directly manipulate the indexes (close once used, non-caching linear scan of SPO). 
    // Build: resolve jars and compile against them. 
    
    // Quads:
    //   Filter placement : (filter (quads...))
    //   Quad loader.
    //   Generalised the pattern stuff.
    //   Dataset management
    //   Tidy OpExecutorTDB.execute(quads)

    // OpRange - OpIndex(?) - generative stream of possibilities.
    // { ?s :p ?x . ?s :q ?w } specials?  "OpAllProperties"

    // Nodec.enc(Node, BB, idx) ; Nodec.dec(BB,idx)->Node [Written - not active]
    //   ObjectFile: need to get BB.
    // Logical/physical id experiment
    
    // ---- Tuple reader tests
    // [incremental]
    // Checking, N-ary and non-RDF.
    // Prefix mapping?  Migrate to a dump format.
    // Byte version.
    
    // ---- BlockMgrs
    // Reopenable BlockMgrs (and the object file?) 
    //   Needed?  SPO close followed by linear scans only.
    //   Reopen the whole graph (??)
    // TupleTable.size - at least an isEmpty 
    //   Not just an empty block manager.

    // Cheap parsing of Node table.
    // [incremental]
    
    // ---- Materialized views
    // Key->Action paradigm
    //   Simple pattern keys (property, 2P). 
    
    // ---- Range queries (filter E (bgp T)) ==> (range T E....) where E has one var and the one var is in T
    //      Given NodeId structure, should be doable for ints and dates.
    
    // ---- Loader - traverse of SPO via a direct block mgr?
    //   For quads
    // ---- tdbloader: 
    //   ** (script) to work on gzip files
    // ---- 32 bit mode.
    // ARC (Adaptive Replacement Cache) Cache Scheme

    //   TransformCopy 
    //      - maybe Transform should be "Op transform(OpABC)"
    //      - and TransformCopy(list) is TransformRewrite. (there are no non-TransformCopy's)
    
    // ------------------------------------------
    
    // ---- Graph
    // QueryHandler to access subjectsFor etc. 
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.

    // ---- Experimental
    // Version of NodeTable that does Logical => Physical id translation
    //    And a PageMgr wrapper for same?

    // Inlines => Inline56, Inline64

    // Update BDB form
}
