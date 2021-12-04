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

package org.apache.jena.dboe.sys;

import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.base.block.FileMode;

public class SystemIndex
{
    /** Size, in bytes, of a pointer between blocks */
    public static final int SizeOfPointer           = SysDB.SizeOfInt;

    public static final boolean is64bitSystem       = SysDB.is64bitSystem;

    // To make the class initialize
    static public void init() {}

    /** Default size, in bytes, of a block */
    public static final int BlockSize               = 8*1024;

    /** Size, in bytes, of a block for in-memory stores (testing) */
    public static final int BlockSizeTest           = 1024;

    /** Size, in bytes, of a segment (used for memory mapped files) */
    public static final int SegmentSize             = 8*1024*1024; // intValue("SegmentSize", 8*1024*1024) ;

    // ---- Cache sizes (within the JVM)

    /** Size of Node to NodeId cache.
     *  Used to map from Node to NodeId spaces.
     *  Used for loading and for query preparation.
     */
    public static final int Node2NodeIdCacheSize    = intValue("Node2NodeIdCacheSize", ( is64bitSystem ? 100*1000 : 50*1000 ));

    /** Size of NodeId to Node cache.
     *  Used to map from NodeId to Node spaces.
     *  Used for retriveing results.
     */
    public static final int NodeId2NodeCacheSize    = intValue("NodeId2NodeCacheSize", ( is64bitSystem ? 500*1000 : 50*1000 ) );

    /** Size of Node lookup miss cache. */
    public static final int NodeMissCacheSize       = 100;

    /** Size of the delayed-write block cache (32 bit systems only) (per file) */
    public static final int BlockWriteCacheSize     = intValue("BlockWriteCacheSize", 2*1000);

    /** Size of read block cache (32 bit systems only).  Increase JVM size as necessary. Per file. */
    public static final int BlockReadCacheSize      = intValue("BlockReadCacheSize", 10*1000);

    private static int intValue(String name, int dft) { return dft; }

    public static void setNullOut(boolean nullOut)
    { NullOut = nullOut; }

    /** Are we nulling out unused space in bytebuffers (records, points etc) */
    public static boolean getNullOut()
    { return NullOut; }

    /** null out (with the FillByte) freed up space in buffers */
    public static boolean NullOut = false;

    /** FillByte value for NullOut */
    public static final byte FillByte = (byte)0xFF;

    public static boolean Checking = false;       // This isn't used enough!

    // ---- File mode

    private static FileMode fileMode = null;
    public static FileMode fileMode() {
        if ( fileMode == null )
            fileMode = determineFileMode();
        return fileMode;
    }

    public static void setFileMode(FileMode newFileMode) {
        if ( fileMode != null ) {
            SysDB.log.warn("System file mode already determined - setting it has no effect");
            return;
        }
        fileMode = newFileMode;
    }

    // So the test suite can setup thing up ... very carefully.
    /*package*/ static void internalSetFileMode(FileMode newFileMode) {
        fileMode = newFileMode;
    }

    private static FileMode determineFileMode() {
        // Be careful that this is not called very, very early, before --set might be seen.
        // Hence delayed access above in fileMode().

        //String x = ARQ.getContext().getAsString(SystemTDB.symFileMode, "default");
        String x = "default";

        if ( x.equalsIgnoreCase("direct") ) {
            SysDB.syslog.info("File mode: direct (forced)");
            return FileMode.direct;
        }
        if ( x.equalsIgnoreCase("mapped") ) {
            SysDB.syslog.info("File mode: mapped (forced)");
            return FileMode.mapped;
        }

        if ( x.equalsIgnoreCase("default") ) {
            if ( is64bitSystem ) {
                SysDB.syslog.debug("File mode: Mapped");
                return FileMode.mapped;
            }
            SysDB.syslog.debug("File mode: Direct");
            return FileMode.direct;
        }
        throw new DBOpEnvException("Unrecognized file mode (not one of 'default', 'direct' or 'mapped': "+x);
    }
}
