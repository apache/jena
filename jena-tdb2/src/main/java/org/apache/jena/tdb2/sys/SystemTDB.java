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

package org.apache.jena.tdb2.sys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.PropertyUtils;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.dboe.base.file.ProcessFileLock;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.sys.SysDB;
import org.apache.jena.dboe.sys.SystemIndex;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemTDB
{
    static { JenaSystem.init(); }

    private SystemTDB() { }

    // NB Same logger as the TDB class because this class is the system info but kept out of TDB javadoc.
    // It's visibility is TDB, not really public.
    private static final Logger log = LoggerFactory.getLogger(TDB2.class);

    /** TDB System log - use for general messages (a few) and warnings.
     *  Generally, do not log events unless you want every user to see them every time.
     *  TDB is an embedded database - libraries and embedded systems should be seen and not heard.
     *  @see #errlog
     */
    // This was added quite late in TDB so need to check it's used appropriately - check for Log.*
    public static final Logger syslog = LoggerFactory.getLogger(TDB2.class);
    /** Send warnings and error */
    public static final Logger errlog = LoggerFactory.getLogger(TDB2.class);

    // ---- Constants that can't be changed without invalidating on-disk data.

    /** Size, in bytes, of the persistent representation of a node id */
    public static final int SizeOfNodeId            = NodeId.SIZE;

    /** Size, in bytes, of a pointer between blocks */
    public static final int SizeOfPointer           = SysDB.SizeOfInt;

    // ---- Node table related

    /** Size, in bytes, of a triple index record. */
    public static final int LenIndexTripleRecord    = 3 * NodeId.SIZE;
    /** Size, in bytes, of a quad index record. */
    public static final int LenIndexQuadRecord      = 4 * NodeId.SIZE;

    /** Size, in bytes, of a Node hash. */
    public static final int LenNodeHash             = 128/8;

    // ---- Symbols and similar

    // ---- Record factories
    public static final RecordFactory indexRecordTripleFactory = new RecordFactory(LenIndexTripleRecord, 0);
    public static final RecordFactory indexRecordQuadFactory = new RecordFactory(LenIndexQuadRecord, 0);
    public static final RecordFactory nodeRecordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId);

    /** TDB1 symbol space */
    public static final String symbolNamespace1     = "http://jena.hpl.hp.com/TDB#";
    /** TDB2 symbol space */
    public static final String symbolNamespace2     = "http://jena.apache.org/TDB#";

    /** Root of TDB-defined parameter names. */
    public static final String symbolNamespace      = symbolNamespace2;

    /** Root of TDB-defined parameter short names.
     * <p>
     * Note: this is different for TDB1 and TDB2.
     */
    public static final String tdbSymbolPrefix      = "tdb2";

    /**
     * Root of any TDB-defined Java system properties.
     * <p>
     * Note: this is different for TDB1 and TDB2.
     */
    public static final String tdbPropertyRoot      = "org.apache.jena.tdb";

    /** Log duplicates during loading */
    public static final Symbol symLogDuplicates     = allocSymbol("logDuplicates");

    /** File mode : one of "direct", "mapped", "default" */
    public static final Symbol symFileMode          = allocSymbol("fileMode");

    /** Index type */
    public static final Symbol symIndexType         = allocSymbol("indexType");

    /** Experimental : triple and quad filtering at scan level */
    public static final Symbol symTupleFilter       = allocSymbol("tupleFilter");

    private static final String PropertyFileKey1    = tdbPropertyRoot+".settings";
    private static final String PropertyFileKey2    = tdbSymbolPrefix+":settings";

    private static final String propertyFileName;
    static {
        String x = System.getProperty(PropertyFileKey1);
        if ( x == null )
            x = System.getProperty(PropertyFileKey2);
        propertyFileName = x;
    }

    public static final boolean is64bitSystem = SysDB.is64bitSystem;

    private static Properties properties = readPropertiesFile();

    // To make the class initialize
    static public void init() {}

    // Convenience renaming. Do not change this constant; change in SystemIndex.
    /** Default size, in bytes, of a block */
    public static final int BlockSize               = SystemIndex.BlockSize;

    // ---- Cache sizes (within the JVM)

    /** Size of Node to NodeId cache.
     *  Used to map from Node to NodeId spaces.
     *  Used for loading and for query preparation.
     */
    public static final int Node2NodeIdCacheSize    = intValue("Node2NodeIdCacheSize", ( is64bitSystem ? 200*1000 : 10*1000 ));

    /** Size of NodeId to Node cache.
     *  Used to map from NodeId to Node spaces.
     *  Used for retrieving results.
     */
    public static final int NodeId2NodeCacheSize    = intValue("NodeId2NodeCacheSize", ( is64bitSystem ? 1000*1000 : 20*1000 ) );

    /** Size of Node lookup miss cache. */
    public static final int NodeMissCacheSize       = 1000;

    /** Size of Node to NodeId cache for prefixes. */
    public static final int PrefixNode2NodeIdCacheSize    = intValue("PrefixNode2NodeIdCacheSize", 250);

    /** Size of NodeId to Node cache for prefixes. */
    public static final int PrefixNodeId2NodeCacheSize    = intValue("PrefixNodeId2NodeCacheSize", 1000 );

    /** Size of Node lookup miss cache for prefixes. */
    public static final int PrefixNodeMissCacheSize       = 100;

    /** Size of the delayed-write block cache (32 bit systems only). Per file. */
    public static final int BlockWriteCacheSize     = intValue("BlockWriteCacheSize", 1000);

    /** Size of read block cache (32 bit systems only). Per file. */
    public static final int BlockReadCacheSize      = intValue("BlockReadCacheSize", 5*1000);

    // ---- Misc

    /** Default BGP optimizer */
    private static ReorderTransformation defaultReorderTransform = ReorderLib.fixed();

    public static void setDefaultReorderTransform(ReorderTransformation reorderTransform) {
        defaultReorderTransform = reorderTransform;
    }

    public static ReorderTransformation getDefaultReorderTransform() {
        return defaultReorderTransform;
    }

    /** Unsupported (for non-standard setups)
     * @see #enableInlineLiterals
     */
    private static String propertyEnableInlineLiterals1 = "org.apache.jena.tdb.store.enableInlineLiterals";

    /** Unsupported (for non-standard setups)
     * @see #enableInlineLiterals
     */
    private static String propertyEnableInlineLiterals2 = "tdb:store.enableInlineLiterals";

    /** <b>Unsupported</b> (for non-standard setups).
     * This controls whether literal values are inlined into NodeIds.
     * This is a major efficiency boost and is the default setting.
     * It can be set false with {@code -Dtdb:store.enableInlineLiterals=false}.
     * Do not mix databases created with this set to different values.
     * Chaos and incorrect results will result.
     * Use with care. No support.
     * Default setting is {@code true}
     */
    public static final boolean enableInlineLiterals = setEnableInlineLiterals();

    private static boolean setEnableInlineLiterals() { // Set enableInlineLiterals from system properties.
        Properties sysProperties = System.getProperties();
        String key = null;
        if ( sysProperties.containsKey(propertyEnableInlineLiterals1) )
            key = PropertyFileKey1;
        else if ( sysProperties.containsKey(propertyEnableInlineLiterals2) )
            key = PropertyFileKey2;
        if ( key == null )
            return true;  // Normal value.
        else
            return Boolean.valueOf(sysProperties.getProperty(key));
    }

    /** null out (with the FillByte) freed up space in buffers */
    public static final boolean NullOut = false;

    /** FillByte value for NullOut */
    public static final byte FillByte = (byte)0xFF;

    public static final boolean Checking = false;       // This isn't used enough!

    /**
     * When enabled, a {@link ProcessFileLock} is used to block other processes opening this database.
     */
    public static final boolean DiskLocationMultiJvmUsagePrevention = true;

    public static void panic(Class<? > clazz, String string) {
        Log.error(clazz, string);
        throw new TDBException(string);
    }

    public static Symbol allocSymbol(String shortName) {
        if ( shortName.startsWith(SystemTDB.tdbSymbolPrefix) )
            throw new TDBException("Symbol short name begins with the TDB namespace prefix: " + shortName);
        if ( shortName.startsWith("http:") )
            throw new TDBException("Symbol short name begins with http: " + shortName);
        return allocSymbol(SystemTDB.symbolNamespace, shortName);
    }

    public static Symbol allocSymbol(String namespace, String shortName) {
        return Symbol.create(namespace + shortName);
    }

    // ----

    private static int intValue(String prefix, String name, int defaultValue) {
        if ( !prefix.endsWith(".") )
            name = prefix + "." + name;
        else
            name = prefix + name;
        return intValue(name, defaultValue);
    }

    private static int intValue(String name, int defaultValue) {
        if ( name == null )
            return defaultValue;
        if ( name.length() == 0 )
            throw new TDBException("Empty string for value name");

        if ( properties == null )
            return defaultValue;

        String x = properties.getProperty(name);
        if ( x == null )
            return defaultValue;
        TDB2.logInfo.info("Set: " + name + " = " + x);
        int v = Integer.parseInt(x);
        return v;
    }

    private static Properties readPropertiesFile() {
        if ( propertyFileName == null )
            return null;

        Properties p = new Properties();
        try {
            TDB2.logInfo.info("Using properties from '" + propertyFileName + "'");
            PropertyUtils.loadFromFile(p, propertyFileName);
        } catch (FileNotFoundException ex) {
            log.debug("No system properties file (" + propertyFileName + ")");
            return null;
        } catch (IOException ex) {
            IO.exception(ex);
        }
        return p;
    }

    // ---- File mode

    private static FileMode fileMode = null;

    public static FileMode fileMode() {
        if ( fileMode == null )
            fileMode = determineFileMode();
        return fileMode;
    }

    public static void setFileMode(FileMode newFileMode) {
        if ( fileMode != null ) {
            log.warn("System file mode already determined - setting it has no effect");
            return;
        }
        fileMode = newFileMode;
    }

    // So the test suite can setup thing up ... very carefully.
    /* package */ static void internalSetFileMode(FileMode newFileMode) {
        fileMode = newFileMode;
    }

    private static FileMode determineFileMode() {
        // Be careful that this is not called very, very early, before --set might be
        // seen.
        // Hence delayed access above in fileMode().

        String x = ARQ.getContext().getAsString(SystemTDB.symFileMode, "default");

        if ( x.equalsIgnoreCase("direct") ) {
            TDB2.logInfo.info("File mode: direct (forced)");
            return FileMode.direct;
        }
        if ( x.equalsIgnoreCase("mapped") )
        {
            TDB2.logInfo.info("File mode: mapped (forced)");
            return FileMode.mapped;
        }

        if ( x.equalsIgnoreCase("default") )
        {
            if ( is64bitSystem )
            {
                TDB2.logInfo.debug("File mode: Mapped");
                return FileMode.mapped;
            }
            TDB2.logInfo.debug("File mode: Direct");
            return FileMode.direct;
        }
        throw new TDBException("Unrecognized file mode (not one of 'default', 'direct' or 'mapped': "+x);
    }
}
