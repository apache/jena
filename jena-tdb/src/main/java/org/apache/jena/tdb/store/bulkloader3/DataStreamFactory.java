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

package org.apache.jena.tdb.store.bulkloader3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.openjena.atlas.AtlasException;

public class DataStreamFactory {
    
    private static boolean use_buffered_streams = true ;
    private static boolean use_compression = true ;
    private static boolean gzip_outside = true ;
    private static int buffer_size = 8192 ; // bytes
    
    public static DataOutputStream createDataOutputStream(OutputStream out) {
        return createDataOutputStream(out, getBuffered(), getGZIPOutside(), getUseCompression(), getBufferSize());
    }
    
    public static DataInputStream createDataInputStream(InputStream in) {
        return createDataInputStream(in, getBuffered(), getGZIPOutside(), getUseCompression(), getBufferSize()) ;
    }

    private static boolean getBuffered() { return use_buffered_streams ; }
    public static void setBuffered(boolean buffered) { use_buffered_streams = buffered ; } 
    private static boolean getUseCompression() { return use_compression ; }
    public static void setUseCompression(boolean compression) { use_compression = compression ; } 
    private static boolean getGZIPOutside() { return gzip_outside; }
    public static void setGZIPOutside(boolean outside) { gzip_outside = outside ; } 
    private static int getBufferSize() { return buffer_size ; }
    public static void setBufferSize(int size) { buffer_size = size ; }


    public static DataOutputStream createDataOutputStream(OutputStream out, boolean buffered, boolean gzip_outside, boolean compression, int buffer_size) {
        try {
            if ( ! buffered ) {
                return new DataOutputStream( compression ? new GZIPOutputStream(out) : out ) ;
            } else {
                if ( gzip_outside ) {
                    return new DataOutputStream( compression ? new GZIPOutputStream(new BufferedOutputStream(out, buffer_size)) : new BufferedOutputStream(out, buffer_size) ) ;
                } else {
                    return new DataOutputStream( compression ? new BufferedOutputStream(new GZIPOutputStream(out, buffer_size)) : new BufferedOutputStream(out, buffer_size) ) ;                
                }
            }
            
        } catch (IOException e) {
            throw new AtlasException(e) ;
        }
    }
    
    public static DataInputStream createDataInputStream(InputStream in, boolean buffered, boolean gzip_outside, boolean compression, int buffer_size) {
        try {
            if ( ! buffered ) {
                return new DataInputStream( compression ? new GZIPInputStream(in) : in ) ;
            } else {
                if ( gzip_outside ) {
                    return new DataInputStream( compression ? new GZIPInputStream(new BufferedInputStream(in, buffer_size)) : new BufferedInputStream(in, buffer_size) ) ;
                } else {
                    return new DataInputStream( compression ? new BufferedInputStream(new GZIPInputStream(in, buffer_size)) : new BufferedInputStream(in, buffer_size) ) ;
                }
            }
        } catch (IOException e) {
            throw new AtlasException(e) ;
        }
    }

}
