/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.transaction.txn;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.nio.ByteBuffer ;
import java.util.UUID ;
import java.util.concurrent.locks.Lock ;
import java.util.function.Supplier ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.shared.uuid.JenaUUID ;

/** Misc class */
public class L {
    
    // Not to be confused with UUID.nameUUIDFromBytes (a helper for version 3 UUIDs)
    /**
     * Java UUID to bytes (most significant first)
     */
    public static byte[] uuidAsBytes(UUID uuid) {
        return uuidAsBytes(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()) ;
    }
    
    /**
     * Jena UUID to bytes (most significant first)
     */
    public static byte[] uuidAsBytes(JenaUUID uuid) {
        return uuidAsBytes(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()) ;
    }
    
    /** UUID, as two longs, as bytes */ 
    public static byte[] uuidAsBytes(long mostSignificantBits, long leastSignificantBits) {
        byte[] bytes = new byte[16] ;
        Bytes.setLong(mostSignificantBits, bytes, 0); 
        Bytes.setLong(leastSignificantBits, bytes, 8);
        return bytes ;
    }
    
    /** A UUID string to bytes */ 
    public static byte[] uuidAsBytes(String str) {
        return uuidAsBytes(UUID.fromString(str)) ;
    }
    
    public static String uuidToString(long mostSignificantBits, long leastSignificantBits) {
        return new UUID(mostSignificantBits, leastSignificantBits).toString() ;
        //JenaUUID.toString(mostSignificantBits, leastSignificantBits)
    }
    
    public static  <V> V withLock(Lock lock, Supplier<V> r) {
        lock.lock();
        try { return r.get() ; }
        finally { lock.unlock() ; }
    }
    
    // Surely there is a utility in the std library to do this?
    public static void withLock(Lock lock, Runnable r) {
        lock.lock();
        try { r.run(); } 
        finally { lock.unlock() ; }
    }
    
    // ==> IO.writeWholeFileAsUTF8
    
    /** Write a string to a file as UTF-8. The file is closed after the operation.
     * @param filename
     * @param content String to be writtem
     * @throws IOException
     */
    
    public static void writeStringAsUTF8(String filename, String content) throws IOException {
        try ( OutputStream out = IO.openOutputFileEx(filename) ) {
            writeStringAsUTF8(out, content) ;
            out.flush() ;
        }
    }

    /** Read a whole stream as UTF-8
     * 
     * @param out       OutputStream to be read
     * @param content   String to be written
     * @throws  IOException
     */
    public static void writeStringAsUTF8(OutputStream out, String content) throws IOException {
        Writer w = new OutputStreamWriter(out, IO.encodingUTF8) ;
        w.write(content);
        w.flush();
        // Not close.
    }

    // ==> IO.writeWholeFileAsUTF8
    
    /** String to ByteBuffer */
    public static ByteBuffer stringToByteBuffer(String str) {
        byte[] b = StrUtils.asUTF8bytes(str) ;
        return ByteBuffer.wrap(b) ;
    }
    
    /** ByteBuffer to String */
    public static String byteBufferToString(ByteBuffer bb) {
        byte[] b = new byte[bb.remaining()] ;
        bb.get(b) ;
        return StrUtils.fromUTF8bytes(b) ;
    }


}

