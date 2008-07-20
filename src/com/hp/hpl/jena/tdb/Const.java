/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import static com.hp.hpl.jena.tdb.TDB.log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteOrder;
import java.util.Properties;

import com.hp.hpl.jena.util.FileUtils;

import com.hp.hpl.jena.sparql.util.Symbol;

import com.hp.hpl.jena.query.ARQ;

import com.hp.hpl.jena.tdb.base.block.FileMode;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

public class Const
{
    // Get these via a properties file
    
    public static final String TDB_NS = "http://jena.hpl.hp.com/TDB#" ;
    
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
    public static final int LenIndexRecord         = 3 * NodeId.SIZE ;
    
    /** Size, in bytes, of a Node hash. */
    public static final int LenNodeHash            = SizeOfLong ;

    // File unit sizes
    
    /** Size, in bytes, of a block */
    public static final int BlockSize               = intValue("BlockSize", 8*1024) ;
    
    /** Size, in bytes, of a memory block */
    public static final int BlockSizeMem            = intValue("BlockSizeMem", 32*8 ) ;

    /** Size, in bytes, of a segment (used for memory mapped files) */
    public static final int SegmentSize             = intValue("SegmentSize", 8*1024*1024) ;
    
    // ---- Caches
    
    /** Size of Node<->NodeId caches */
    public static final int NodeCacheSize           = intValue("NodeCacheSize", 100*1000) ;

    /** Size of the delayed-write block cache (32 bit systems only) (per file) */
    public static final int BlockWriteCacheSize     = intValue("BlockWriteCacheSize", 2000) ;

    /** Size of read block cache (32 bit systems only).  Increase JVM size as necessary. Per file. */
    public static final int BlockReadCacheSize      = intValue("BlockReadCacheSize", 10*1000) ;
    
    // ---- Misc
    
    /** Number of adds/deletes between calls to sync (-ve to disable) */
    public static final int SyncTick                = intValue("SyncTick", 5*1000) ;

    public static final ByteOrder NetworkOrder      = ByteOrder.BIG_ENDIAN ;
    
    // BDB related.
    public static final int BDB_cacheSizePercent    = intValue("BDB_cacheSizePercent", 75) ;
    
    public static Symbol allocSymbol(String shortName)
    { 
        if ( shortName.startsWith(TDB.tdbSymbolPrefix)) 
            throw new TDBException("Symbol short name begins with the TDB namespace prefix: "+shortName) ;
        if ( shortName.startsWith("http:")) 
            throw new TDBException("Symbol short name begins with http: "+shortName) ;
        return allocSymbol(TDB.symbolNamespace, shortName) ;
    }
    
    private static Symbol allocSymbol(String namespace, String shortName)
    {
        return Symbol.create(namespace+shortName) ;
    }
    
    // --------
    
    // Tie to location but that means one instance per graph
    // More in the context !
    
    private static String propertyFileName = "" ;
    private static Properties properties = null ; //readPropertiesFile() ;
    
    private static int intValue(String name, int defaultValue)
    {
        if ( name == null ) return defaultValue ;
        if ( name.length() == 0 ) throw new TDBException("Empty string for value name") ;
        
        if ( properties == null )
            return defaultValue ;
        String x = properties.getProperty(name) ;
        if ( x == null )
            return defaultValue ;  
        int v = Integer.parseInt(x) ;
        return v ;
    }
    

    
    private static Properties readPropertiesFile()
    {
        Properties p = new Properties() ;
        try
        {
            Reader r = FileUtils.asBufferedUTF8(new FileInputStream(propertyFileName)) ;
            p.load(r) ;
        } catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return p ;
    }
    // --------
    
    public static final boolean is64bitSystem = determineIf64Bit() ;

    private static boolean determineIf64Bit()
    {
        String s = System.getProperty("sun.arch.data.model") ;
        if ( s != null )
        {
            boolean b = s.equals("64") ; 
            log.debug("System architecture: "+(b?"64 bit":"32 bit")) ;
            return b ;
        }
        // Not a SUN VM
        s = System.getProperty("java.vm.info") ;
        if ( s == null )
        {
            log.warn("Can't determine the data model") ;
            return false ;    
        }
        log.info("Can't determine the data model from 'sun.arch.data.model' - using java.vm.info") ;
        boolean b = s.contains("64") ;
        log.info("System architecture: "+(b?"64 bit":"32 bit")) ;
        return b ;
    }
    
    private static FileMode fileMode = null ;
    public static FileMode fileMode()
    { 
        if ( fileMode == null )
            fileMode = determineFileMode() ;
        return fileMode ;
    }

    private static FileMode determineFileMode()
    {
        // Called very, very early, before --set might be seen.
        // Hence delayed access above.
        
        String x = ARQ.getContext().getAsString(TDB.symFileMode, "default") ;

        if ( x.equalsIgnoreCase("direct") )
        {
            log.info("File mode: mapped") ;
            return FileMode.direct ;
        }
        if ( x.equalsIgnoreCase("mapped") )
        {
            log.info("File mode: mapped") ;
            return FileMode.mapped ;
        }
        
        if ( x.equalsIgnoreCase("default") )
        {
            if ( is64bitSystem )
            {
                log.debug("FileMode: Mapped") ;
                return FileMode.mapped ;
            }
            log.debug("FileMode: Direct") ;
            return FileMode.direct ;
        }
        throw new TDBException("Unrecognized file mode (not one of 'default', 'direct' or 'mapped': "+x) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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