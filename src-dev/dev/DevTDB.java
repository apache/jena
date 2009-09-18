/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class DevTDB
{
    // == 0.8.3
    // s/.meta/.prop/? 
    // ** Properties global for dataset creation.
    // See metedata stuff in BPlusTreeParams
    
    // Explain mode - See notes in class
    // Don't print optimizer type when set at info level.
    // ?? Properties to RDF using <> :property "value"

    // ** Every datasets takes a confiuration copied from the globals, not read directly.
    // Default.
    
    // Compressor
    // B+Tree checking utility.
    
    // IndexFactory understanding index type name
    //    Registry<String->T>("bplustree", IndexBuilder)
    
    // Tuning for low memory / 32 bit - smaller fixed caches - configurable
    
    // Longer: packaging of TDB and Joseki for an unpack-and-go solution.
    // At least, specific documentation.
    
    // --- Setup
    // Check surpressed deprecations and switch to Setup.*

    // Investigate tracking sorted order in BGPs (+PSO index?)
    // Assembler : TDB settings : document.
    
    // == Misc
    // Node cache on 64bit machines needs to be bigger or rebalence
    // Cache stats counters (prep for JMX but useful now)
    
    // ** Document concurrency.
    
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
