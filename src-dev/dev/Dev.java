/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    
    // TupleTable
    // --
    // Test TupleIndex
    // Test TupleTable
    //   Then a Tuple<NodeID><->Tuple<Node> wrapper
    //   Then a Triple/Quad <=> Tuple<Node> wrapper
    //   The TripleTable, QuadTable.
    // --
    // Testing:
    // TestTupleIndex - not just SPO.
    // And more tests.  Scan and no scan.
    // TestTupleTable.
    
    /*
    Need to be more systematic about naming.
    
    See Desc.record which needs to get the unmap 
    And extract()
    
    Problem in naming is that Desc wants in 0,1,2, order for the INDEX ORDER form.
    ColMap is 0,1,2 in the ORIGINAL ORDER

    

    getMappedSlot(i, Tuple) means get the i slot in the mapped form. 

    mapOrder -> 
    Mapping SPO->POS  means getMapped(0) -> 1
     
    POS:
    ColumnMapping(i, SPO) -> (0, P)

    colMap.pack(idx, tuple) makes SPO goto POS.
    colMap.unpack(idx, tuple) makes SPO goto POS.

    map: turns SPO into POS

    "pack" and unpack
    Insert and extract
    */
    
    // Does TupleTable or TupleIndex do the reordering?
    // Currently, TupleTable but better TupleIndex  
    // Avoid excessive tuple->mapped tuple->record
    
    
    
    // ---- Reopenable BlockMgrs (and the object file?) 

    // --  ?? ARQ Optimizer and controlling optimizations per graph (esp FilterPlacement).
    
    // Umbel data: http://umbel.org/documentation.html
    
    // ---- Assembler and configuration
    //    Configuring the built-in constants.
    //    Setting options (globally, per model)
    //    Variable indexes
    
    // ---- Materialized views
    
    // ---- Range queries (filter E (bgp T)) ==> (range T E....) where E has one var and the one var is in T
    //    Given NodeId structure, should be doable for ints and dates.
    
    // ---- Reifier
    
    // ---- Quads
    //   Common abstractions
    //       quad table (share with SDB)
    //       dataset as graph + quads
    //       dataset management
    
    // ---- Loader - traverse of SPO via a direct block mgr?
    //   ** Close indexes not in use in a given phase
    //  GraphTDB.primaryTraverse() -> Iterator<Tuple<NodeId>>??
    //    Do properly : reopenable indexes (under the triple index)
    //      Reopenable block mgr's .passivate/.activate
    //    Open second index on same
    //    - shared formatting with GraphLoadMonitor
    // ---- tdbloader: 
    //   ** (script) to work on gzip files
    //   ** Write stats (where to hook in to get unique?)
    // ---- 32 bit mode.
    // Occasional large drops (1K).  GC?  BufferPool for direct mode?
    // allocateBuffer / returnBuffer
    // NodeTable : pool for buffers for writing nodes?

    
    //   TransformCopy 
    //      - maybe Transform should be "Op transform(OpABC)"
    //      - and TransformCopy(list) is TransformRewrite. (there are no non-TransformCopy's)
    
    // ---- TestStats, VarCounter.
    // ---- A way to force display of optimizer even for the deep parts (assumes no inputs?)
    
    // ------------------------------------------
    
    // ---- New cache API alloc/return/invalidate (shrink/grow?) : stats
    // Weak references and more space.
    
    // ---- Node Table
    //     Compression
    
    // ---- Graph
    // QueryHandler to access subjectsFor etc. 
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.

    // Version of NodeTable that does Logical => Physical id translation
    //    And a PageMgr wrapper for same?

    // Inlines => Inline56, Inline64

    // Update BDB form
}
