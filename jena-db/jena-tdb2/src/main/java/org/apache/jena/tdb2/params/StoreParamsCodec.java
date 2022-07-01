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

import static org.apache.jena.tdb2.params.StoreParamsConst.*;

import java.io.*;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.json.*;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.tdb2.TDBException;

/** Encode and decode {@link StoreParams} */
public class StoreParamsCodec {

    /** Write to a file */
    public static void write(Location location, StoreParams params) {
        write(location.getPath(Names.TDB_CONFIG_FILE) ,params);
    }

    /** Write to a file */
    public static void write(String filename, StoreParams params) {
        try (OutputStream out = new FileOutputStream(filename);
             OutputStream out2 = new BufferedOutputStream(out); ) {
            JsonObject object = encodeToJson(params);
            JSON.write(out2, object);
            out2.write('\n');
        }
        catch (IOException ex) { IO.exception(ex); }
    }

    /**
     * Read from a file if possible.
     * Return null for memory locations, file not found or syntax errors.
     */
    public static StoreParams read(Location location) {
        if ( location.isMem() )
            return null;
        return read(location.getPath(Names.TDB_CONFIG_FILE));
    }

    /**
     * Read from a file if possible.
     * Return null if the file is not found or has a syntax error.
     */
    public static StoreParams read(String filename) {
        try {
            InputStream in = IO.openFileEx(filename);
            if ( in == null )
                return null;
            JsonObject obj = JSON.parse(in);
            return StoreParamsCodec.decode(obj);
        } catch (FileNotFoundException ex) {
            return null;
        } catch (JsonParseException ex) {
            FmtLog.warn(StoreParamsCodec.class, "Ignoring store params : Syntax error in '%s': [line:%d, col:%d] %s", filename, ex.getLine(), ex.getColumn(), ex.getMessage());
            return null;
        } catch (IOException e) {
            IO.exception(e);
            return null;
        }
    }

    public static JsonObject encodeToJson(StoreParams params) {
        JsonBuilder builder = new JsonBuilder();
        builder.startObject("StoreParams");    // "StoreParams" is an internal alignment marker - not in the JSON.

        encode(builder, key(fFileMode),                 params.getFileMode().name());
        encode(builder, key(fBlockSize),                params.getBlockSize());
        encode(builder, key(fBlockReadCacheSize),       params.getBlockReadCacheSize());
        encode(builder, key(fBlockWriteCacheSize),      params.getBlockWriteCacheSize());
        encode(builder, key(fNode2NodeIdCacheSize),     params.getNode2NodeIdCacheSize());
        encode(builder, key(fNodeId2NodeCacheSize),     params.getNodeId2NodeCacheSize());
        encode(builder, key(fNodeMissCacheSize),        params.getNodeMissCacheSize());
        encode(builder, key(fNodeTableBaseName),        params.getNodeTableBaseName());
        encode(builder, key(fPrimaryIndexTriples),      params.getPrimaryIndexTriples());
        encode(builder, key(fTripleIndexes),            params.getTripleIndexes());
        encode(builder, key(fPrimaryIndexQuads),        params.getPrimaryIndexQuads());
        encode(builder, key(fQuadIndexes),              params.getQuadIndexes());
        encode(builder, key(fPrefixTableBaseName),      params.getPrefixTableBaseName());
        encode(builder, key(fPrimaryIndexPrefix),       params.getPrimaryIndexPrefix());
        encode(builder, key(fPrefixIndexes),            params.getPrefixIndexes());

        builder.finishObject("StoreParams");
        return (JsonObject)builder.build();
    }

    private static final String jsonKeyPrefix= "tdb.";

    private static String key(String string) {
        if ( string.startsWith(jsonKeyPrefix))
            throw new TDBException("Key name already starts with '"+jsonKeyPrefix+"'");
        return jsonKeyPrefix+string;
    }

    private static String unkey(String string) {
        if ( ! string.startsWith(jsonKeyPrefix) )
            throw new TDBException("JSON key name does not start with '"+jsonKeyPrefix+"'");
        return string.substring(jsonKeyPrefix.length());
    }

    public static StoreParams decode(JsonObject json) {
        StoreParamsBuilder builder = StoreParams.builder();

        for ( String key : json.keys() ) {
            String short_key = unkey(key);
            switch(short_key) {
                case fFileMode :               builder.fileMode(FileMode.valueOf(getString(json, key)));   break ;
                case fBlockSize:               builder.blockSize(getInt(json, key));                       break ;
                case fBlockReadCacheSize:      builder.blockReadCacheSize(getInt(json, key));              break ;
                case fBlockWriteCacheSize:     builder.blockWriteCacheSize(getInt(json, key));             break ;
                case fNode2NodeIdCacheSize:    builder.node2NodeIdCacheSize(getInt(json, key));            break ;
                case fNodeId2NodeCacheSize:    builder.nodeId2NodeCacheSize(getInt(json, key));            break ;
                case fNodeMissCacheSize:       builder.nodeMissCacheSize(getInt(json, key));               break ;

                case fNodeTableBaseName:       builder.nodeTableBaseName(getString(json, key));            break ;
                case fPrimaryIndexTriples:     builder.primaryIndexTriples(getString(json, key));          break ;
                case fTripleIndexes:           builder.tripleIndexes(getStringArray(json, key));           break ;
                case fPrimaryIndexQuads:       builder.primaryIndexQuads(getString(json, key));            break ;
                case fQuadIndexes:             builder.quadIndexes(getStringArray(json, key));             break ;

                case fPrefixTableBaseName:     builder.prefixTableBaseName(getString(json, key));          break ;
                case fPrimaryIndexPrefix:      builder.primaryIndexPrefix(getString(json, key));           break ;
                case fPrefixIndexes:           builder.prefixIndexes(getStringArray(json, key));           break ;

                default:
                    throw new TDBException("StoreParams key no recognized: "+key);
            }
        }
        return builder.build();
    }

    // "Get or error" operations.

    private static String getString(JsonObject json, String key) {
        if ( ! json.hasKey(key) )
            throw new TDBException("StoreParamsCodec.getString: no such key: "+key);
        String x = json.get(key).getAsString().value();
        return x;
    }

    private static Integer getInt(JsonObject json, String key) {
        if ( ! json.hasKey(key) )
            throw new TDBException("StoreParamsCodec.getInt: no such key: "+key);
        Integer x = json.get(key).getAsNumber().value().intValue();
        return x;
    }

    private static String[] getStringArray(JsonObject json, String key) {
        if ( ! json.hasKey(key) )
            throw new TDBException("StoreParamsCodec.getStringArray: no such key: "+key);
        JsonArray a = json.get(key).getAsArray();
        String[] x = new String[a.size()];
        for ( int i = 0; i < a.size() ; i++ ) {
            x[i] = a.get(i).getAsString().value();
        }
        return x;
    }

    // Encode helper.
    private static void encode(JsonBuilder builder, String name, Object value) {
        if ( value instanceof Number ) {
            long x = ((Number)value).longValue();
            builder.key(name).value(x);
            return;
        }
        if ( value instanceof String ) {
            builder.key(name).value(value.toString());
            return;
        }
        if ( value instanceof String[] ) {
            String[] x = (String[])value;
            builder.key(name);
            builder.startArray();
            for ( String s : x ) {
                builder.value(s);
            }
            builder.finishArray();
            return;
        }
        throw new TDBException("Class of value not recognized: "+Lib.classShortName(value.getClass()));
    }
}
