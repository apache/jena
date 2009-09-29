/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class DevTDB
{
    // == 0.8.3
    // Check use of CacheLRU - shoudl it be sync'ed or make it teh callers responsibility?
    // NB get operations must be write-safe.
    // Const for names.
    // [Half] Per DB properties for variable stuff (see SystemTDB.readPropertiesFile)
    //    Remove SystemTDB.intValue and friends.
    // See metadata stuff in BPlusTreeParams
    // Document
    //   concurrency  []
    //   Settings     []
    //   Explain mode [DONE]
    // Test.
    
    // Don't print optimizer type when set at info level.

    // ----
    // B+Tree checking utility.
    
    // IndexFactory understanding index type name
    //    Registry<String->T>("bplustree", IndexBuilder)
    
    // Longer: packaging of TDB and Joseki for an unpack-and-go solution.
    // At least, specific documentation.
    
    // --- Setup
    // Check supressed deprecations and switch to Setup.*

    // == Misc
    // Node cache on 64bit machines needs to be bigger or rebalence
    // Cache stats counters (prep for JMX but useful now)
    
    // ==== RIOT
    // ** Connect the errorhandler set via JenaReaderbase to the one used by RIOT-Turtle/RIOT-NTriples.
    // Properties on the readers
    // Error handler, not exceptions, in Turtle.
    //   Check for continuation.
    // TriG
    
    // == tdbdump && tdbrestore
    // ---- Optimizer
    //   The disjunction/UNION rewrite (ARQ)
    
    // ---- Documentation
    
    // ---- BlockMgrs
    // Consistency - do not manage in block managers except where MRSW not safe.
    // TupleTable.size - at least an isEmpty 
    // ==> Reopenable
    // ==> .release(id)
    // ==> Accurate size (?? meaningful beyond isEmpty/notEmpty?)
    // ==> Metablocks.
    //   ==> Moveable roots.
    
    // ---- Misc
    // Inlines => Inline56, Inline64, ??

    // ---- tdbloader: 
    //   ** (script) to work on gzip files
}
