/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

/** Names of things in TDB */
public class Names
{
    public static final String primaryIndexTriples      = "SPO" ; 
    public static final String[] tripleIndexes          = { primaryIndexTriples, "POS", "OSP" } ;

    public static final String primaryIndexQuads        = "GSPO" ; 
    public static final String[] quadIndexes            = { primaryIndexQuads, "GPOS", "GOSP", "POSG", "OSPG", "SPOG"} ;
    
    public static final String primaryIndexPrefix       = "GPU" ;
    public static final String[] prefixIndexes          = { primaryIndexPrefix } ;
    
    /** B+Trees - nodes file and records file */
    public static final String bptExtTree               = "idn" ;
    public static final String bptExtRecords            = "dat" ;

    /** BTrees - single file per tree */
    public static final String btExt                    = "idx" ;
    
    /** ExtHash - dictionary file*/
    public static final String extHashExt               = "exh" ;
    public static final String extHashBucketExt         = "dat" ;
    
    /** Node file */
    public static final String extNodeData              = "dat" ;           // Extension of node files
    public static final String extJournal               = "jrnl" ;          // Extension of node files.
    public static final String journalFile              = "journal"+"."+extJournal ;
    
    public static final String indexId2Node             = "nodes" ;         // Node table
    public static final String indexNode2Id             = "node2id";        // Node hash to id table
    
    //public static final String indexId2Node           = "id2node";        // Would be the Index for node(hash) to id  

    /** Prefixes file */
    public static final String prefixId2Node            = "prefixes" ;      // Prefix node table 
    public static final String prefixNode2Id            = "prefix2id";      // Prefix node table for index Node/hash->id
    public static final String indexPrefix              = "prefixIdx";      // Prefix index name
    
    /** Optimizer / stats */
    public static final String optStats                 = "stats.opt" ;
    public static final String optFixed                 = "fixed.opt" ;
    public static final String optNone                  = "none.opt" ; 
    public static final String optDefault               = optFixed ;
    
    public static final String extMeta                  = "info" ;
    public static final String directoryMetafile        = "this" ;          // Root name of the directory for a metafile.  

    /** Name to indicate in-memory */ 
    public static final String memName                  = "--mem--" ;

    public static boolean isMem(String name)            { return memName.equals(name) ; }
    
    // ---- Names for Java properties in metadata files
    
    public static String makeKey(String root, String...elements)
    { return makeName(root, elements) ; }
    
    public static String makeName(String root, String...elements)
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append(root) ;
        for ( String s : elements )
        {
            if ( ! s.startsWith(".") )
                sb.append(".") ;
            sb.append(s) ;
        }
        return sb.toString() ;
    }
    
    // Component elements.
    public static final String elNode                 = "node" ;
    public static final String elBPlusTree            = "bptree" ;
    public static final String elIndex                = "index" ;
    public static final String elObject               = "object" ;
    
    public static final String elType                 = "type" ;
    public static final String elLayout               = "layout" ;
    public static final String elVersion              = "version" ;
    public static final String elTimestamp            = "timestamp" ;
    public static final String elCreated              = "created" ;
    
    // Root names.
    public static final String keyNS                    = "tdb" ;
    public static final String keyNSNode                = makeName(keyNS, elNode) ;
    public static final String keyNSBPlusTree           = makeName(keyNS, elBPlusTree) ;
    
    // Location metadata - in the directory wide metadata file.
    public static final String kVersion               = makeName(keyNS, elVersion) ;
    public static final String kCreatedDate           = makeName(keyNS, elTimestamp) ;    

    // Node table metadata
    public static final String kNodeTableType         = makeName(keyNS, elNode, elType) ;
    public static final String kNodeTableLayout       = makeName(keyNS, elNode, elLayout) ;

//    // Object file
//    public static final String kObjectTableType         = makeName(keyNS, elObject, elType) ;
//    
//    // Default index metadata - in the index metadata file.
//    
//    public static final String kIndexType             = makeName(keyNS, elIndex, elType) ;
//    public static final String kIndexFileLayout       = makeName(keyNS, elIndex, elLayout) ;
    
    // See also BPlusTreeParams for keyNSBPlusTree derived names.
    
//    // Current values
//    public static final String currentIndexType         = IndexType.BPlusTree.getName() ;
//    public static final String currentIndexFileVersion  = "v1" ;

    // Property strings - Setup
    
    public static final String pNode2NodeIdCacheSize       = "tdb.cache.node2nodeid.size" ;
    public static final String pNodeId2NodeCacheSize       = "tdb.cache.nodeid2node.size" ;
    public static final String pBlockWriteCacheSize        = "tdb.cache.blockwrite.size" ;
    public static final String pBlockReadCacheSize         = "tdb.cache.blockread.size" ;
    public static final String pSyncTick                   = "tdb.synctick" ;
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