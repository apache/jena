/**
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

package com.hp.hpl.jena.tdb.setup;

import org.apache.jena.atlas.json.JsonArray ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonObject ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;

/** Encode and decode {@linkplain StoreParams} */ 
public class StoreParamsCodec {
    
    public static JsonObject encodeToJson(StoreParams params) {
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("StoreParams") ;    // "StoreParams" is an internal alignment marker - not in the JSON.
        
        encode(builder, "tdb.FileMode",                 params.getFileMode().name()) ;
        encode(builder, "tdb.BlockSize",                params.getBlockSize()) ;
        encode(builder, "tdb.BlockReadCacheSize",       params.getBlockReadCacheSize()) ;
        encode(builder, "tdb.BlockWriteCacheSize",      params.getBlockWriteCacheSize()) ;
        encode(builder, "tdb.Node2NodeIdCacheSize",     params.getNode2NodeIdCacheSize()) ;
        encode(builder, "tdb.NodeId2NodeCacheSize",     params.getNodeId2NodeCacheSize()) ;
        encode(builder, "tdb.NodeMissCacheSize",        params.getNodeMissCacheSize()) ;
        encode(builder, "tdb.IndexNode2Id",             params.getIndexNode2Id()) ;
        encode(builder, "tdb.IndexId2Node",             params.getIndexId2Node()) ;
        encode(builder, "tdb.PrimaryIndexTriples",      params.getPrimaryIndexTriples()) ;
        encode(builder, "tdb.TripleIndexes",            params.getTripleIndexes()) ;
        encode(builder, "tdb.PrimaryIndexQuads",        params.getPrimaryIndexQuads()) ;
        encode(builder, "tdb.QuadIndexes",              params.getQuadIndexes()) ;
        encode(builder, "tdb.PrimaryIndexPrefix",       params.getPrimaryIndexPrefix()) ;
        encode(builder, "tdb.PrefixIndexes",            params.getPrefixIndexes()) ;
        encode(builder, "tdb.IndexPrefix",              params.getIndexPrefix()) ;
        encode(builder, "tdb.PrefixNode2Id",            params.getPrefixNode2Id()) ;
        encode(builder, "tdb.PrefixId2Node",            params.getPrefixId2Node()) ;
        builder.finishObject("StoreParams") ;
        return (JsonObject)builder.build() ;
    }

    public static StoreParams decode(JsonObject json) {
        StoreParamsBuilder builder = StoreParamsBuilder.create() ;
        
        for ( String key : json.keys() ) {
            switch(key) {
                case "tdb.FileMode" :               builder.fileMode(FileMode.valueOf(getString(json, key))) ; break ;
                case "tdb.BlockSize":               builder.blockSize(getInt(json, key)) ; break ;
                case "tdb.BlockReadCacheSize":      builder.blockReadCacheSize(getInt(json, key)) ; break ;
                case "tdb.BlockWriteCacheSize":     builder.blockWriteCacheSize(getInt(json, key)) ; break ;
                case "tdb.Node2NodeIdCacheSize":    builder.node2NodeIdCacheSize(getInt(json, key)) ; break ;
                case "tdb.NodeId2NodeCacheSize":    builder.nodeId2NodeCacheSize(getInt(json, key)) ; break ;
                case "tdb.NodeMissCacheSize":       builder.nodeMissCacheSize(getInt(json, key)) ; break ;
                case "tdb.IndexNode2Id":            builder.indexNode2Id(getString(json, key)) ; break ;
                case "tdb.IndexId2Node":            builder.indexId2Node(getString(json, key)) ; break ;
                case "tdb.PrimaryIndexTriples":     builder.primaryIndexTriples(getString(json, key)) ; break ;
                case "tdb.TripleIndexes":           builder.tripleIndexes(getStringArray(json, key)) ; break ;
                case "tdb.PrimaryIndexQuads":       builder.primaryIndexQuads(getString(json, key)) ; break ;
                case "tdb.QuadIndexes":             builder.quadIndexes(getStringArray(json, key)) ; break ;
                case "tdb.PrimaryIndexPrefix":      builder.primaryIndexPrefix(getString(json, key)) ; break ;
                case "tdb.PrefixIndexes":           builder.prefixIndexes(getStringArray(json, key)) ; break ;
                case "tdb.IndexPrefix":             builder.indexPrefix(getString(json, key)) ; break ;
                case "tdb.PrefixNode2Id":           builder.prefixNode2Id(getString(json, key)) ; break ;
                case "tdb.PrefixId2Node":           builder.prefixId2Node(getString(json, key)) ; break ;
                default:
                    throw new TDBException("StoreParams key no recognized: "+key) ;
            }
        }
        return builder.build() ;
    }

    // "Get or error" operations.
    
    private static String getString(JsonObject json, String key) {
        if ( ! json.hasKey(key) )
            throw new TDBException("StoreParamsCodec.getString: no such key: "+key) ;
        String x = json.get(key).getAsString().value() ;
        return x ;
    }

    private static Integer getInt(JsonObject json, String key) {
        if ( ! json.hasKey(key) )
            throw new TDBException("StoreParamsCodec.getInt: no such key: "+key) ;
        Integer x = json.get(key).getAsNumber().value().intValue() ;
        return x ;
    }
    
    private static String[] getStringArray(JsonObject json, String key) {
        if ( ! json.hasKey(key) )
            throw new TDBException("StoreParamsCodec.getStringArray: no such key: "+key) ;
        JsonArray a = json.get(key).getAsArray() ;
        String[] x = new String[a.size()] ;
        for ( int i = 0 ; i < a.size() ; i++ ) {
            x[i] = a.get(i).getAsString().value() ;
        }
        return x ;
    }

    // Encode helper.
    private static void encode(JsonBuilder builder, String name, Object value) {
        if ( value instanceof Number ) {
            long x = ((Number)value).longValue() ;
            builder.key(name).value(x) ;
            return ;
        }
        if ( value instanceof String ) {
            builder.key(name).value(value.toString()) ;
            return ;
        }
        if ( value instanceof String[] ) {
            String[] x = (String[])value ;
            builder.key(name) ;
            builder.startArray() ;
            for ( String s : x ) {
                builder.value(s) ;
            }
            builder.finishArray() ;
            return ;
        }
        throw new TDBException("Class of value not recognized: "+Utils.classShortName(value.getClass())) ;
    }
}
