/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb.sys;

import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.nio.ByteOrder ;
import java.util.Properties ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.PropertyUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class SystemTDB
{
    // NB Same logger as the TDB class because this class is the system info but kept out of TDB javadoc.
    // It's visibility is TDB, not really public. 
    private static final Logger log = LoggerFactory.getLogger(TDB.class) ;
    
    /** TDB System log - use for general messages (a few) and warnings.
     *  Generally, do not log events unless you want every user to see them every time.
     *  TDB is an embedded database - libraries and embedded systems should be seen and not heard.
     *  @see #errlog 
     */
    // This was added quite late in TDB so need to check it's used appropriately - check for Log.*
    public static final Logger syslog = LoggerFactory.getLogger("TDB") ;
    /** Send warnings and error */
    public static final Logger errlog = LoggerFactory.getLogger("TDB") ;
    
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

    // Test show no visable effect.
//    /** Unit of flushing the data when loading - data phase.  -1 means off, sync at end of load only.  */
//    public final static long LoadFlushTickPrimary   = -1 ; // 10*1000*1000 ;
//    /** Unit of flushing the data when loading - index phase  -1 means off, sync at end of load only.  */
//    public final static long LoadFlushTickSecondary = -1 ; // 10*1000*1000 ;

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

    private static final String propertyFileKey1    = tdbPropertyRoot+".settings" ;
    private static final String propertyFileKey2    = tdbSymbolPrefix+":settings" ;

    private static String propertyFileName = null ;
    static {
        propertyFileName = System.getProperty(propertyFileKey1) ;
        if ( propertyFileName == null )
            propertyFileName = System.getProperty(propertyFileKey2) ;
    }
    
    public static final boolean is64bitSystem = determineIf64Bit() ;

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
    
    /** Size, in bytes, of a segment (used for memory mapped files) */
    public static final int SegmentSize             = 8*1024*1024 ; // intValue("SegmentSize", 8*1024*1024) ;
    
    // ---- Cache sizes (within the JVM)
    
    public static final int ObjectFileWriteCacheSize = 8*1024 ;
    
    /** Size of Node to NodeId cache.
     *  Used to map from Node to NodeId spaces.
     *  Used for loading and for query preparation.
     */
    public static final int Node2NodeIdCacheSize    = intValue("Node2NodeIdCacheSize", ( is64bitSystem ? 100*1000 : 50*1000 )) ;

    /** Size of NodeId to Node cache.
     *  Used to map from NodeId to Node spaces.
     *  Used for retriveing results.
     */
    public static final int NodeId2NodeCacheSize    = intValue("NodeId2NodeCacheSize", ( is64bitSystem ? 500*1000 : 50*1000 ) ) ;
    
    /** Size of Node lookup miss cache. */
    public static final int NodeMissCacheSize       = 100 ;
    
    /** Size of the delayed-write block cache (32 bit systems only) (per file) */
    public static final int BlockWriteCacheSize     = intValue("BlockWriteCacheSize", 2*1000) ;

    /** Size of read block cache (32 bit systems only).  Increase JVM size as necessary. Per file. */
    public static final int BlockReadCacheSize      = intValue("BlockReadCacheSize", 10*1000) ;
    
    // ---- Misc
    
//    /** Number of adds/deletes between calls to sync (-ve to disable) */
//    public static final int SyncTick                = intValue("SyncTick", -1) ;

    /** Default BGP optimizer */
    public static ReorderTransformation defaultReorderTransform = ReorderLib.fixed() ;

    public static final ByteOrder NetworkOrder      = ByteOrder.BIG_ENDIAN ;
    
    /** Unsupported (for non-standard setups) 
     * @see #enableInlineLiterals
     */
    public static String propertyEnableInlineLiterals1 = "com.hp.hpl.jena.tdb.store.enableInlineLiterals" ;
    /** Unsupported (for non-standard setups) 
     * @see #enableInlineLiterals
     */
    public static String propertyEnableInlineLiterals2 = "tdb:store.enableInlineLiterals" ;
    /** <b>Unsupported</b> (for non-standard setups).
     * This controls whether literal values are inlined into NodeIds.
     * This is a major efficiency boost and is the default setting.
     * It can be set false with {@code -Dtdb:store.enableInlineLiterals=false}. 
     * Do not mix databases created with this set to different values.
     * Chaos and incorrect results will result.
     * Use with care. No support. 
     * Default setting is {@code true}   
     */
    public static final boolean enableInlineLiterals ;
    static { // Set enableInlineLiterals from system properties.
        Properties sysProperties = System.getProperties() ;
        String key = null ; 
        if ( sysProperties.containsKey(propertyEnableInlineLiterals1) )
            key = propertyFileKey1 ;
        else if ( sysProperties.containsKey(propertyEnableInlineLiterals2) )
            key = propertyFileKey2 ;
        if ( key == null )
            enableInlineLiterals = true ;  // Normal value. 
        else
            enableInlineLiterals = Boolean.valueOf(sysProperties.getProperty(key)) ;
    }

//    public static void setNullOut(boolean nullOut)
//    { SystemTDB.NullOut = nullOut ; }
//
//    /** Are we nulling out unused space in bytebuffers (records, points etc) */ 
//    public static boolean getNullOut()
//    { return SystemTDB.NullOut ; }

    /** null out (with the FillByte) freed up space in buffers */
    public static boolean NullOut = false ;
    
    /** FillByte value for NullOut */
    public static final byte FillByte = (byte)0xFF ;

    public static boolean Checking = false ;       // This isn't used enough!
    
    /**
     * New feature introduced by JENA-648 to help prevent one common cause of TDB corruption.
     * <p>
     * When enabled lock files are written to disk locations with the current owner process PID,
     * other processes will refuse to access that location while another live process owns the PID.
     * </p>
     */
    public static boolean DiskLocationMultiJvmUsagePrevention = true;

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
            PropertyUtils.loadFromFile(p, propertyFileName) ;
        } catch (FileNotFoundException ex)
        { 
            log.debug("No system properties file ("+propertyFileName+")") ;
            return null ;
        } catch (IOException ex) { IO.exception(ex) ; }
        return p ;
    }

    // --------
    
    public static final boolean isWindows = determineIfWindows() ;	// Memory mapped files behave differently.

    //Or look in File.listRoots.
    //Alternative method:
    //  http://stackoverflow.com/questions/1293533/name-of-the-operating-system-in-java-not-os-name
    
    private static boolean determineIfWindows() {
    	String s = System.getProperty("os.name") ;
    	if ( s == null )
    		return false ;
    	return s.startsWith("Windows ") ;
	}

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
    
    // So the test suite can setup thing up ... very carefully.
    /*package*/ static void internalSetFileMode(FileMode newFileMode)
    {
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
}
