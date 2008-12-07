/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // ==== 0.7.0 "quads"
    // Test of union default graph etc.
    // tdb:unionDefaultGraph -- enable quad mode?
    // Sort out file naming.
    // Commands
    // Documentation
    //   Dataset assembler
    //   Symbol settings.
    // ====
    
    // Even more directly manipulate the indexes (close once used, linear scan of SPO). 
    
    // Quads:
    //   Filter placement : (filter (quads...))
    //   Quad loader.
    //   Generalised the pattern stuff.
    //   Dataset management
    //   Tidy OpExecutorTDB.execute(quads)

    // OpRange - OpIndex - generative stream of possibilities.

    // Nodec.enc(Node, BB, idx) ; Nodec.dec(BB,idx)->Node
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
    //   Reopne the whole graph (??)
    // TupleTable.size - at least an isEmpty 
    //   Not just an empty block manager.

    // Cheap parsing of Node table.
    // [incremental]
    
    // ---- Assembler and configuration
    //      Setting options (per graph)
    //      Variable indexes
    
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
    // ARC Cache Scheme

    //   TransformCopy 
    //      - maybe Transform should be "Op transform(OpABC)"
    //      - and TransformCopy(list) is TransformRewrite. (there are no non-TransformCopy's)
    
    // ------------------------------------------
    
    // ---- Graph
    // QueryHandler to access subjectsFor etc. 
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.

    // Version of NodeTable that does Logical => Physical id translation
    //    And a PageMgr wrapper for same?

    // Inlines => Inline56, Inline64

    // Update BDB form
}
