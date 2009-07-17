/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

import com.hp.hpl.jena.tdb.index.IndexType;

/** Names of things in TDB */
public class Names
{
    public static final String primaryIndexTriples      = "SPO" ; 
    public static final String[] tripleIndexes          = { primaryIndexTriples, "POS", "OSP" } ;

    public static final String primaryIndexQuads        = "GSPO" ; 
    public static final String[] quadIndexes            = { primaryIndexQuads, "GPOS", "GOSP", "POSG", "OSPG", "SPOG"} ;
    
    
    /** B+Trees - nodes file and records file */
    public static final String bptExt1                  = "idn" ;
    public static final String bptExt2                  = "dat" ;

    /** BTrees - single file per tree */
    public static final String btExt                    = "idx" ;
    
    /** ExtHash - dictionary file*/
    public static final String extHashExt               = "exh" ;
    public static final String extHashBucketExt         = "dat" ;
    
    /** Node file */
    public static final String extNodeData              = "dat" ;           // Extension of node files.
    
    public static final String nodeTable                = "nodes" ;         // Node table
    public static final String indexNode2Id             = "node2id";        // Node hash to id table
    
    //public static final String indexId2Node           = "id2node";        // Would be the Index for node(hash) to id  

    /** Prefixes file */
    public static final String prefixNodeTable          = "prefixes" ;      // Prefix node table 
    public static final String indexPrefix2Id           = "prefix2id";      // Prefix node table for index Node/hash->id
    public static final String indexPrefix              = "prefixIdx";      // Primary key on the prefixes table.
    
    /** Optimizer / stats */
    public static final String optStats                 = "stats.opt";
    public static final String optDefault               = "fixed.opt";      // Currently, it's just the presence of this file that matters.
    public static final String optNone                  = "none.opt"; 
    
//    /** Properties information for a FileGroup*/ 
//    public static final String metaData             = "info" ;
    
    public static final String extMeta                  = "meta" ;
    public static final String directoryMetafile        = "this" ;          // Root name of the directory for a metafile.  

    /** Name to indicate in-memory */ 
    public static final String memName                  = "--mem--" ;

    public static boolean isMem(String name)            { return memName.equals(name) ; }
    
    // ---- Names for Java properties in metadata files
    
    /* Metadata names - global */
    private static String makeMetadataKey(String root, String keyShortName)
    { 
        if ( keyShortName.startsWith(".") )
            return root+keyShortName ;
        else
            return root+"."+keyShortName ;
    }

    // Root names.
    public static final String keyNS                    = "tdb" ;
    public static final String keyNSBPlusTree           = "tdb.bptree" ;
    // Location metadata
    public static final String keyVersion               = makeMetadataKey(keyNS, "version") ;
    public static final String keyCreatedDate           = makeMetadataKey(keyNS, "createtimestamp") ;    
    
    // Index metadata
    public static final String keyIndexType             = makeMetadataKey(keyNS, "indexType") ;
    public static final String keyIndexFileVersion      = makeMetadataKey(keyNS, "indexFileVersion") ;
    
    // See also BPlusTreeParams for keyNSBPlusTree derived names.
    
    // Current values
    public static final String currentIndexType         = IndexType.BPlusTree.getName() ;
    public static final String currentIndexFileVersion  = "v1" ;

    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */