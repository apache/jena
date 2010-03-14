/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.nio.ByteOrder ;
import java.util.Properties ;

import org.openjena.atlas.lib.PropertyUtils ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexType ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class SystemTDB
{
    // NB Same logger as the TDB class because this class is the system info but kept out of TDB javadoc.
    // It's visibility is TDB, not really public. 
    private static final Logger log = LoggerFactory.getLogger(TDB.class) ;
    
    public static final String TDB_NS = "http://jena.hpl.hp.com/TDB#" ;
    
    // ---- Constants that can't be changed without invalidating on-disk data.  
    
    /** Size, in bytes, of a Java long */
    public static final int SizeOfLong              = Long.SIZE/Byte.SIZE ;
    
    /** Size, in bytes, of a Java int */
    public static final int SizeOfInt               = Integer.SIZE/Byte.SIZE ;
    
    /** Size, in bytes, of the persistent representation of a node id */
    public static final int SizeOfNodeId            = NodeId.SIZE ;

    /** Size, in bytes, of a pointer between blocks */
    public static final int SizeOfPointer           = SizeOfInt ;
    
    // ---- Node table related
    
    /** Size, in bytes, of a triple index record. */
    public static final int LenIndexTripleRecord    = 3 * NodeId.SIZE ;
    /** Size, in bytes, of a quad index record. */
    public static final int LenIndexQuadRecord      = 4 * NodeId.SIZE ;
    
    /** Size, in bytes, of a Node hash.
     * In TDB 0.7.X and before this was 8 bytes (64/8).
     * In TDB 0.8.0 and above it is 16 bytes (128/8).
     * These two systems are not compatible.
     */
    //public static final int LenNodeHash             = SizeOfLong ; // TDB <= 0.7.X
    public static final int LenNodeHash             = 128/8 ; // TDB >= 0.8.0

    // ---- Symbols and similar
    
    // ---- Record factories
    public final static RecordFactory indexRecordTripleFactory = new RecordFactory(LenIndexTripleRecord, 0) ;
    public final static RecordFactory indexRecordQuadFactory = new RecordFactory(LenIndexQuadRecord, 0) ;
    public final static RecordFactory nodeRecordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId) ;

    /** Root of TDB-defined parameter names */
    public static final String symbolNamespace      = "http://jena.hpl.hp.com/TDB#" ;

    /** Root of TDB-defined parameter short names */  
    public static final String tdbSymbolPrefix      = "tdb" ;
    
    /** Root of any TDB-defined Java system properties */   
    public static final String tdbPropertyRoot      = "com.hp.hpl.jena.tdb" ;

    /** Log duplicates during loading */
    public static final Symbol symLogDuplicates     = allocSymbol("logDuplicates") ;

    /** File mode : one of "direct", "mapped", "default" */ 
    public static final Symbol symFileMode          = allocSymbol("fileMode") ;

    /** Index type */
    public static final Symbol symIndexType         = allocSymbol("indexType") ;

    /** Experimental : triple and quad filtering at scan level */
    public static final Symbol symTupleFilter       = allocSymbol("tupleFilter") ;

    /** Experimental : graphs forming the default graph (List&lt;String&gt;) */
    public static final Symbol symDatasetDefaultGraphs     = allocSymbol("datasetDefaultGraphs") ;
    
    /** Experimental : graphs forming the named graphs (List&lt;String&gt;) */
    public static final Symbol symDatasetNamedGraphs       = allocSymbol("datasetNamedGraphs") ;

    private static final String propertyFileKey1    = tdbPropertyRoot+".settings" ;
    private static final String propertyFileKey2    = tdbSymbolPrefix+":settings" ;

    private static String propertyFileName = null ;
    static {
        propertyFileName = System.getProperty(propertyFileKey1) ;
        if ( propertyFileName == null )
            propertyFileName = System.getProperty(propertyFileKey2) ;
    }

    private static Properties properties = readPropertiesFile() ;

    // To make the class initialize
    static public void init() {}
    
    /** Size, in bytes, of a block */
    public static final int BlockSize               = 8*1024 ; // intValue("BlockSize", 8*1024) ;

    /** Size, in bytes, of a block for testing */
    public static final int BlockSizeTest           = 1024 ; // intValue("BlockSizeTest", 1024) ;

    /** Size, in bytes, of a block for testing */
    public static final int BlockSizeTestMem         = 500 ;

//    /** Size, in bytes, of a memory block */
//    public static final int BlockSizeMem            = 32*8 ; //intValue("BlockSizeMem", 32*8 ) ;

    /** order of an in-memory BTree or B+Tree */
    public static final int OrderMem                = 5 ; // intValue("OrderMem", 5) ;
    
    // -- Settable parameters

    public static Properties global = new Properties() ;
    
    /** Size, in bytes, of a segment (used for memory mapped files) */
    public static final int SegmentSize             = 8*1024*1024 ; // intValue("SegmentSize", 8*1024*1024) ;
    
    // ---- Cache sizes (within the JVM)
    
    /** Size of Node to NodeId cache.
     *  Used to map from Node to NodeId spaces.
     *  Used for loading and for query preparation.
     */
    public static final int Node2NodeIdCacheSize    = intValue("Node2NodeIdCacheSize", 100*1000) ;

    /** Size of NodeId to Node cache.
     *  Used to map from NodeId to Node spaces.
     *  Used for retriveing results.
     */
    public static final int NodeId2NodeCacheSize    = intValue("NodeId2NodeCacheSize", 100*1000) ;

    /** Size of the delayed-write block cache (32 bit systems only) (per file) */
    public static final int BlockWriteCacheSize     = intValue("BlockWriteCacheSize", 2*1000) ;

    /** Size of read block cache (32 bit systems only).  Increase JVM size as necessary. Per file. */
    public static final int BlockReadCacheSize      = intValue("BlockReadCacheSize", 10*1000) ;
    
    // ---- Misc
    
    /** Number of adds/deletes between calls to sync (-ve to disable) */
    public static final int SyncTick                = intValue("SyncTick", 100*1000) ;

    // Choice is made in GraphTDBFactory
    public static ReorderTransformation defaultOptimizer = null ; //ReorderLib.fixed() ;

    public static final ByteOrder NetworkOrder      = ByteOrder.BIG_ENDIAN ;
    
    public static boolean NullOut = false ;

    public static boolean Checking = false ;       // This isn't used enough!

    // BDB related.
    //public static final int BDB_cacheSizePercent    = intValue("BDB_cacheSizePercent", 75) ;
    
    public static void panic(Class<?> clazz, String string)
    {
        Log.fatal(clazz, string) ;
        throw new TDBException(string) ;
    }
    
    public static Symbol allocSymbol(String shortName)
    { 
        if ( shortName.startsWith(SystemTDB.tdbSymbolPrefix)) 
            throw new TDBException("Symbol short name begins with the TDB namespace prefix: "+shortName) ;
        if ( shortName.startsWith("http:")) 
            throw new TDBException("Symbol short name begins with http: "+shortName) ;
        return allocSymbol(SystemTDB.symbolNamespace, shortName) ;
    }
    
    private static Symbol allocSymbol(String namespace, String shortName)
    {
        return Symbol.create(namespace+shortName) ;
    }
    
    // ----
    
    private static int intValue(String prefix, String name, int defaultValue)
    {
        if ( ! prefix.endsWith(".") )
            name = prefix+"."+name ;
        else
            name = prefix+name ;
        return intValue(name, defaultValue) ;
    }
    
    private static int intValue(String name, int defaultValue)
    {
        if ( name == null ) return defaultValue ;
        if ( name.length() == 0 ) throw new TDBException("Empty string for value name") ;
        
        if ( properties == null )
            return defaultValue ;

        String x = properties.getProperty(name) ;
        if ( x == null )
            return defaultValue ; 
        TDB.logInfo.info("Set: "+name+" = "+x) ;
        int v = Integer.parseInt(x) ;
        return v ;
    }

    private static Properties readPropertiesFile()
    {
        if ( propertyFileName == null )
            return null ;
        
        Properties p = new Properties() ;
        try
        {
            TDB.logInfo.info("Using properties from '"+propertyFileName+"'") ;
            PropertyUtils.loadFromFile(properties, propertyFileName) ;
        } catch (FileNotFoundException ex)
        { 
            log.debug("No system properties file ("+propertyFileName+")") ;
            return null ;
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return p ;
    }

    
    // --------
    // Tie to location but that means one instance per graph
    
    public static final boolean is64bitSystem = determineIf64Bit() ;

    private static boolean determineIf64Bit()
    {
        String s = System.getProperty("sun.arch.data.model") ;
        if ( s != null )
        {
            boolean b = s.equals("64") ; 
            TDB.logInfo.debug("System architecture: "+(b?"64 bit":"32 bit")) ;
            return b ;
        }
        // Not a SUN VM
        s = System.getProperty("java.vm.info") ;
        if ( s == null )
        {
            log.warn("Can't determine the data model") ;
            return false ;    
        }
        log.debug("Can't determine the data model from 'sun.arch.data.model' - using java.vm.info") ;
        boolean b = s.contains("64") ;
        TDB.logInfo.debug("System architecture: (from java.vm.info) "+(b?"64 bit":"32 bit")) ;
        return b ;
    }
    
    // Not in use yet.
    private static void determineJVMSize()
    {
        Runtime runtime = Runtime.getRuntime() ;
        
        long totalMemory = runtime.totalMemory() ;
        long maxMemory = runtime.maxMemory() ;
        int cpus = runtime.availableProcessors() ;
        
        runtime.addShutdownHook(new Thread(new SyncCacheRunnable())) ;
    }
    
    static class SyncCacheRunnable implements Runnable
    {
        public void run()
        {
            try {
                TDBMaker.syncDatasetCache() ;
            } catch (Exception ex) {} 
        }
    }
    
    // ---- File mode
    
    private static FileMode fileMode = null ;
    public static FileMode fileMode()
    { 
        if ( fileMode == null )
            fileMode = determineFileMode() ;
        return fileMode ;
    }

    public static void setFileMode(FileMode newFileMode)
    {
        if ( fileMode != null )
        {
            log.warn("System file mode already determined - setting it has no effect") ;
            return ;
        }
        fileMode = newFileMode ;
    }
    
    private static FileMode determineFileMode()
    {
        // Be careful that this is not called very, very early, before --set might be seen.
        // Hence delayed access above in fileMode().
        
        String x = ARQ.getContext().getAsString(SystemTDB.symFileMode, "default") ;

        if ( x.equalsIgnoreCase("direct") )
        {
            TDB.logInfo.info("File mode: direct (forced)") ;
            return FileMode.direct ;
        }
        if ( x.equalsIgnoreCase("mapped") )
        {
            TDB.logInfo.info("File mode: mapped (forced)") ;
            return FileMode.mapped ;
        }
        
        if ( x.equalsIgnoreCase("default") )
        {
            if ( is64bitSystem )
            {
                TDB.logInfo.debug("File mode: Mapped") ;
                return FileMode.mapped ;
            }
            TDB.logInfo.debug("File mode: Direct") ;
            return FileMode.direct ;
        }
        throw new TDBException("Unrecognized file mode (not one of 'default', 'direct' or 'mapped': "+x) ;
    }
    
    // ---- Index type
    
    public static final String indexTypeBTree          = "BTree" ;
    public static final String indexTypeBPlusTree      = "BPlusTree" ;
    public static final String indexTypeExtHash        = "ExtHash" ;
    
    public static final String defaultIndexType        = indexTypeBPlusTree ; 
    
    // Delay until needed so application can set symIndexType
    private static IndexType indexType = null ;

    public static IndexType getIndexType()
    {
        if ( indexType != null )
            return indexType ;
        
        boolean defaultSetting = false ;
        String x = TDB.getContext().getAsString(SystemTDB.symIndexType) ;
        if ( x == null )
        {
            defaultSetting = true ;
            x = SystemTDB.defaultIndexType ;
        }
        IndexType iType = IndexType.get(x) ;
        if ( !defaultSetting )
            LoggerFactory.getLogger(IndexType.class).info("Index type: "+iType) ;
        
        indexType = iType ;
        return iType ;
    }
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