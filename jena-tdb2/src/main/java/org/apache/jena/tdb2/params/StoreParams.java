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

package org.apache.jena.tdb2.params;

import java.util.Objects;

import org.apache.jena.dboe.base.block.BlockParams;
import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.dboe.index.IndexParams;
import org.apache.jena.tdb2.params.StoreParamsBuilder.Item;

/** System parameters for a TDB database instance.
 * <p>
 * Some parameters can be changed from run to run
 * and some parameters can only be changed at the point the database is
 * created.
 * <p>
 * Getting parameters settings wrong can destroy a database.
 * Alternating the block size is not encouraged and should only be
 * done if necessary.  It can silently destroy a database if set
 * to a different value than that used to create the database.  The
 * default value of 8Kbytes is good for almost use.
 *
 * @see StoreParamsBuilder  for constructing StoreParams
 * @see StoreParamsConst    for default values.
 */
public class StoreParams implements IndexParams, BlockParams, StoreParamsDynamic
{
    /* These are items you can change JVM to JVM */

    /*package*/ final String label;
    /*package*/ final Item<FileMode>           fileMode;

    /*package*/ final Item<Integer>            blockReadCacheSize;          // Direct mode block cache size
    /*package*/ final Item<Integer>            blockWriteCacheSize;         // Direct mode block cache size

    /*package*/ final Item<Integer>            Node2NodeIdCacheSize;
    /*package*/ final Item<Integer>            NodeId2NodeCacheSize;
    /*package*/ final Item<Integer>            NodeMissCacheSize;
    /*package*/ final Item<Integer>            prefixNode2NodeIdCacheSize;
    /*package*/ final Item<Integer>            prefixNodeId2NodeCacheSize;
    /*package*/ final Item<Integer>            prefixNodeMissCacheSize;

    /*
     * These are items affect database layout and
     * only can be applied when a database is created.
     * They do not affect existing databases.
     * If you want to, say, change the index structure,
     * you'll need to use the index tools.
     */
    /*package*/ final Item<Integer>            blockSize;

    /*package*/ final Item<String>             nodeTableBaseName;

    /*package*/ final Item<String>             primaryIndexTriples;
    /*package*/ final Item<String[]>           tripleIndexes;

    /*package*/ final Item<String>             primaryIndexQuads;
    /*package*/ final Item<String[]>           quadIndexes;

    /*package*/ final Item<String>             prefixTableBaseName;
    /*package*/ final Item<String>             primaryIndexPrefix;
    /*package*/ final Item<String[]>           prefixIndexes;

    /** @deprecated Prefer {@link #builder(String)}. */
    @Deprecated
    public static StoreParamsBuilder builder() {
        return builder((String)null);
    }

    /** @deprecated Prefer {@link #builder(String, StoreParams)}. */
    @Deprecated
    public static StoreParamsBuilder builder(StoreParams params) {
        return builder(null, params);
    }

    /** Build StoreParams, starting from system defaults.
     *
     * @return StoreParamsBuilder
     */
    public static StoreParamsBuilder builder(String label) { return StoreParamsBuilder.create(label); }

    /** Build StoreParams, starting from given default values.
     *
     * @return StoreParamsBuilder
     */
    public static StoreParamsBuilder builder(String label, StoreParams params) { return StoreParamsBuilder.create(label, params); }

    /*package*/ StoreParams(String label,
                            Item<FileMode> fileMode, Item<Integer> blockSize,
                            Item<Integer> blockReadCacheSize, Item<Integer> blockWriteCacheSize,

                            Item<Integer> node2NodeIdCacheSize, Item<Integer> nodeId2NodeCacheSize,
                            Item<Integer> nodeMissCacheSize,

                            Item<Integer> prefixNode2NodeIdCacheSize, Item<Integer> prefixNodeId2NodeCacheSize,
                            Item<Integer> prefixNodeMissCacheSize,

                            Item<String> nodeTableBaseName,
                            Item<String> primaryIndexTriples, Item<String[]> tripleIndexes,
                            Item<String> primaryIndexQuads, Item<String[]> quadIndexes,

                            Item<String> prefixTableBasename,
                            Item<String> primaryIndexPrefix, Item<String[]> prefixIndexes) {
        this.label                  = label;
        this.fileMode               = fileMode;
        this.blockSize              = blockSize;
        this.blockReadCacheSize     = blockReadCacheSize;
        this.blockWriteCacheSize    = blockWriteCacheSize;

        this.Node2NodeIdCacheSize   = node2NodeIdCacheSize;
        this.NodeId2NodeCacheSize   = nodeId2NodeCacheSize;
        this.NodeMissCacheSize      = nodeMissCacheSize;

        this.prefixNode2NodeIdCacheSize   = prefixNode2NodeIdCacheSize;
        this.prefixNodeId2NodeCacheSize   = prefixNodeId2NodeCacheSize;
        this.prefixNodeMissCacheSize      = prefixNodeMissCacheSize;

        this.nodeTableBaseName      = nodeTableBaseName;

        this.primaryIndexTriples    = primaryIndexTriples;
        this.tripleIndexes          = tripleIndexes;
        this.primaryIndexQuads      = primaryIndexQuads;
        this.quadIndexes            = quadIndexes;
        this.primaryIndexPrefix     = primaryIndexPrefix;
        this.prefixIndexes          = prefixIndexes;

        this.prefixTableBaseName    = prefixTableBasename;
    }

    /** The system default settings. This is the normal set to use.
     *  It is the set of values used when no StoreParams is provided,
     *  which is the normal usage.
     */
    public static StoreParams getDftStoreParams() {
        return StoreParamsConst.dftStoreParams;
    }

    public static StoreParams getDftMemStoreParams() {
        return StoreParamsConst.dftMemStoreParams;
    }


    /** A {@code StoreParams} that provides a smaller
     * in-JVM foot print.  This is compatible with
     * any database but it is wise to use this consistently,
     * that is, use when created and when opened later.
     * It reduces cache sizes and runs the database in "direct"
     * file mode so as not to use memory mapped files
     * in addition to the JVM space.
     */
    public static StoreParams getSmallStoreParams() {
        return StoreParamsConst.smallStoreParams;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public FileMode getFileMode() {
        return fileMode.value;
    }

    @Override
    public boolean isSetFileMode() {
        return fileMode.isSet;
    }

    @Override
    public Integer getBlockSize() {
        return blockSize.value;
    }

    @Override
    public Integer getBlockReadCacheSize() {
        return blockReadCacheSize.value;
    }

    @Override
    public boolean isSetBlockReadCacheSize() {
        return blockReadCacheSize.isSet;
    }

    @Override
    public Integer getBlockWriteCacheSize() {
        return blockWriteCacheSize.value;
    }

    @Override
    public boolean isSetBlockWriteCacheSize() {
        return blockWriteCacheSize.isSet;
    }

    @Override
    public Integer getNode2NodeIdCacheSize() {
        return Node2NodeIdCacheSize.value;
    }

    @Override
    public boolean isSetNodeId2NodeCacheSize() {
        return NodeId2NodeCacheSize.isSet;
    }

    @Override
    public boolean isSetNode2NodeIdCacheSize() {
        return Node2NodeIdCacheSize.isSet;
    }

    @Override
    public Integer getNodeId2NodeCacheSize() {
        return NodeId2NodeCacheSize.value;
    }

    @Override
    public Integer getNodeMissCacheSize() {
        return NodeMissCacheSize.value;
    }

    @Override
    public boolean isSetNodeMissCacheSize() {
        return NodeMissCacheSize.isSet;
    }

    @Override
    public Integer getPrefixNode2NodeIdCacheSize() {
        return prefixNode2NodeIdCacheSize.value;
    }

    @Override
    public boolean isSetPrefixNodeId2NodeCacheSize() {
        return NodeId2NodeCacheSize.isSet;
    }

    @Override
    public boolean isSetPrefixNode2NodeIdCacheSize() {
        return Node2NodeIdCacheSize.isSet;
    }

    @Override
    public Integer getPrefixNodeId2NodeCacheSize() {
        return NodeId2NodeCacheSize.value;
    }

    @Override
    public Integer getPrefixNodeMissCacheSize() {
        return NodeMissCacheSize.value;
    }

    @Override
    public boolean isSetPrefixNodeMissCacheSize() {
        return NodeMissCacheSize.isSet;
    }

    public String getNodeTableBaseName() {
        return nodeTableBaseName.value;
    }

    public boolean isSetNodeTableBaseName() {
        return nodeTableBaseName.isSet;
    }

    public String getPrimaryIndexTriples() {
        return primaryIndexTriples.value;
    }

    public String[] getTripleIndexes() {
        return tripleIndexes.value;
    }

    public String getPrimaryIndexQuads() {
        return primaryIndexQuads.value;
    }

    public String[] getQuadIndexes() {
        return quadIndexes.value;
    }

    public String getPrefixTableBaseName() {
        return prefixTableBaseName.value;
    }

    public boolean isSetPrefixBaseName() {
        return prefixTableBaseName.isSet;
    }



    public String getPrimaryIndexPrefix() {
        return primaryIndexPrefix.value;
    }

    public String[] getPrefixIndexes() {
        return prefixIndexes.value;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        if ( label != null )
            fmt(buff, "label", label, true);

        fmt(buff, "fileMode", getFileMode().toString(), fileMode.isSet);
        fmt(buff, "blockSize", getBlockSize(), blockSize.isSet);
        fmt(buff, "readCacheSize", getBlockReadCacheSize(), blockReadCacheSize.isSet);
        fmt(buff, "writeCacheSize", getBlockWriteCacheSize(), blockWriteCacheSize.isSet);
        fmt(buff, "Node2NodeIdCacheSize", getNode2NodeIdCacheSize(), Node2NodeIdCacheSize.isSet);
        fmt(buff, "NodeId2NodeCacheSize", getNodeId2NodeCacheSize(), NodeId2NodeCacheSize.isSet);
        fmt(buff, "NodeMissCacheSize", getNodeMissCacheSize(), NodeMissCacheSize.isSet);

        fmt(buff, "nodeTableBaseName", getNodeTableBaseName(), nodeTableBaseName.isSet);
        fmt(buff, "primaryIndexTriples", getPrimaryIndexTriples(), primaryIndexTriples.isSet);
        fmt(buff, "tripleIndexes", getTripleIndexes(), tripleIndexes.isSet);
        fmt(buff, "primaryIndexQuads", getPrimaryIndexQuads(), primaryIndexQuads.isSet);
        fmt(buff, "quadIndexes", getQuadIndexes(), quadIndexes.isSet);

        fmt(buff, "prefixTableBaseName", getPrefixTableBaseName(), prefixTableBaseName.isSet);
        fmt(buff, "primaryIndexPrefix", getPrimaryIndexPrefix(), primaryIndexPrefix.isSet);
        fmt(buff, "prefixIndexes", getPrefixIndexes(), prefixIndexes.isSet);

        return buff.toString();
    }

    private void fmt(StringBuilder buff, String name, String[] strings, boolean isSet) {
        String dftStr = "";
        if ( ! isSet )
            dftStr = "dft:";
        buff.append(String.format("%-20s   %s[%s]\n", name, dftStr, String.join(", ", strings)));
    }

    private void fmt(StringBuilder buff, String name, String value, boolean isSet) {
        String dftStr = "";
        if ( ! isSet )
            dftStr = "dft:";
        buff.append(String.format("%-20s   %s%s\n", name, dftStr, value));
    }

    private void fmt(StringBuilder buff, String name, int value, boolean isSet) {
        String dftStr = "";
        if ( ! isSet )
            dftStr = "dft:";
        buff.append(String.format("%-20s   %s%s\n", name, dftStr, value));
    }

    /** Equality but ignore "isSet" */
    public static boolean sameValues(StoreParams params1, StoreParams params2) {
        if ( params1 == null && params2 == null )
            return true;
        if ( params1 == null )
            return false;
        if ( params2 == null )
            return false;
        if ( !sameValues(params1.fileMode, params2.fileMode) )
            return false;
        if ( !sameValues(params1.blockSize, params2.blockSize) )
            return false;
        if ( !sameValues(params1.blockReadCacheSize, params2.blockReadCacheSize) )
            return false;
        if ( !sameValues(params1.blockWriteCacheSize, params2.blockWriteCacheSize) )
            return false;
        if ( !sameValues(params1.Node2NodeIdCacheSize, params2.Node2NodeIdCacheSize) )
            return false;
        if ( !sameValues(params1.NodeId2NodeCacheSize, params2.NodeId2NodeCacheSize) )
            return false;
        if ( !sameValues(params1.NodeMissCacheSize, params2.NodeMissCacheSize) )
            return false;
        if ( !sameValues(params1.nodeTableBaseName, params2.nodeTableBaseName) )
            return false;
        if ( !sameValues(params1.primaryIndexTriples, params2.primaryIndexTriples) )
            return false;
        if ( !sameValues(params1.tripleIndexes, params2.tripleIndexes) )
            return false;
        if ( !sameValues(params1.primaryIndexQuads, params2.primaryIndexQuads) )
            return false;
        if ( !sameValues(params1.quadIndexes, params2.quadIndexes) )
            return false;
        if ( !sameValues(params1.prefixTableBaseName, params2.prefixTableBaseName) )
            return false;
        if ( !sameValues(params1.primaryIndexPrefix, params2.primaryIndexPrefix) )
            return false;
        if ( !sameValues(params1.prefixIndexes, params2.prefixIndexes) )
            return false;
        return true;
    }

    private static <X> boolean sameValues(Item<X> item1, Item<X> item2) {
        return Objects.deepEquals(item1.value, item2.value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Node2NodeIdCacheSize == null) ? 0 : Node2NodeIdCacheSize.hashCode());
        result = prime * result + ((NodeId2NodeCacheSize == null) ? 0 : NodeId2NodeCacheSize.hashCode());
        result = prime * result + ((NodeMissCacheSize == null) ? 0 : NodeMissCacheSize.hashCode());
        result = prime * result + ((blockReadCacheSize == null) ? 0 : blockReadCacheSize.hashCode());
        result = prime * result + ((blockSize == null) ? 0 : blockSize.hashCode());
        result = prime * result + ((blockWriteCacheSize == null) ? 0 : blockWriteCacheSize.hashCode());
        result = prime * result + ((fileMode == null) ? 0 : fileMode.hashCode());
        result = prime * result + ((nodeTableBaseName == null) ? 0 : nodeTableBaseName.hashCode());
        result = prime * result + ((prefixTableBaseName == null) ? 0 : prefixTableBaseName.hashCode());
        result = prime * result + ((prefixIndexes == null) ? 0 : prefixIndexes.hashCode());
        result = prime * result + ((primaryIndexPrefix == null) ? 0 : primaryIndexPrefix.hashCode());
        result = prime * result + ((primaryIndexQuads == null) ? 0 : primaryIndexQuads.hashCode());
        result = prime * result + ((primaryIndexTriples == null) ? 0 : primaryIndexTriples.hashCode());
        result = prime * result + ((quadIndexes == null) ? 0 : quadIndexes.hashCode());
        result = prime * result + ((tripleIndexes == null) ? 0 : tripleIndexes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        StoreParams other = (StoreParams)obj;
        if ( Node2NodeIdCacheSize == null ) {
            if ( other.Node2NodeIdCacheSize != null )
                return false;
        } else if ( !Node2NodeIdCacheSize.equals(other.Node2NodeIdCacheSize) )
            return false;
        if ( NodeId2NodeCacheSize == null ) {
            if ( other.NodeId2NodeCacheSize != null )
                return false;
        } else if ( !NodeId2NodeCacheSize.equals(other.NodeId2NodeCacheSize) )
            return false;
        if ( NodeMissCacheSize == null ) {
            if ( other.NodeMissCacheSize != null )
                return false;
        } else if ( !NodeMissCacheSize.equals(other.NodeMissCacheSize) )
            return false;
        if ( blockReadCacheSize == null ) {
            if ( other.blockReadCacheSize != null )
                return false;
        } else if ( !blockReadCacheSize.equals(other.blockReadCacheSize) )
            return false;
        if ( blockSize == null ) {
            if ( other.blockSize != null )
                return false;
        } else if ( !blockSize.equals(other.blockSize) )
            return false;
        if ( blockWriteCacheSize == null ) {
            if ( other.blockWriteCacheSize != null )
                return false;
        } else if ( !blockWriteCacheSize.equals(other.blockWriteCacheSize) )
            return false;
        if ( fileMode == null ) {
            if ( other.fileMode != null )
                return false;
        } else if ( !fileMode.equals(other.fileMode) )
            return false;
        if ( nodeTableBaseName == null ) {
            if ( other.nodeTableBaseName != null )
                return false;
        } else if ( !nodeTableBaseName.equals(other.nodeTableBaseName) )
            return false;
        if ( prefixTableBaseName == null ) {
            if ( other.prefixTableBaseName != null )
                return false;
        } else if ( !prefixTableBaseName.equals(other.prefixTableBaseName) )
            return false;
        if ( prefixIndexes == null ) {
            if ( other.prefixIndexes != null )
                return false;
        } else if ( !prefixIndexes.equals(other.prefixIndexes) )
            return false;
        if ( primaryIndexPrefix == null ) {
            if ( other.primaryIndexPrefix != null )
                return false;
        } else if ( !primaryIndexPrefix.equals(other.primaryIndexPrefix) )
            return false;
        if ( primaryIndexQuads == null ) {
            if ( other.primaryIndexQuads != null )
                return false;
        } else if ( !primaryIndexQuads.equals(other.primaryIndexQuads) )
            return false;
        if ( primaryIndexTriples == null ) {
            if ( other.primaryIndexTriples != null )
                return false;
        } else if ( !primaryIndexTriples.equals(other.primaryIndexTriples) )
            return false;
        if ( quadIndexes == null ) {
            if ( other.quadIndexes != null )
                return false;
        } else if ( !quadIndexes.equals(other.quadIndexes) )
            return false;
        if ( tripleIndexes == null ) {
            if ( other.tripleIndexes != null )
                return false;
        } else if ( !tripleIndexes.equals(other.tripleIndexes) )
            return false;
        return true;
    }
}

