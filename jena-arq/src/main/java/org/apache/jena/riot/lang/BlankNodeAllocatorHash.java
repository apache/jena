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

package org.apache.jena.riot.lang;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.codec.digest.MurmurHash3;
import org.apache.jena.atlas.lib.*;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * Allocate bnode labels using a per-run seed and the label presented.
 * <p>
 * This is the most scalable, always legal allocator.
 * <p>
 * New allocators must be created per parser run, or .reset() called. These are
 * fed to a digest to give a bit string, (currently MD5, to get a 128bit bit
 * string) that is used to form a bNode AnonId of hex digits.
 * <p>
 * In addition, there is a cache of label{@literal ->}node allocations, using the natural
 * tendency to locality in a database dump. (subject bNodes, bNodes in lists
 * and other data values structures like unit values).
 * <p>
 * Not thread safe.
 */

public class BlankNodeAllocatorHash implements BlankNodeAllocator {

    private static int          CacheSize       = 1000;
    private byte[]              seedBytes       = null;  
    private byte[]              counterBytes    = new byte[10]; 
    private Cache<String, Node> cache           = null;
    private long                counter         = 0;

    public BlankNodeAllocatorHash() {
        reset();
        cache = CacheFactory.createCache(CacheSize);
    }

    /**
     * Gets a fresh seed value
     * <p>
     * Note that this is called almost immediately by the constructor
     * and on this initial call you will not yet have access to any 
     * implementation specific information used to select the seed.
     * </p>
     * <p>
     * Implementations <strong>must</strong> return a non-null value
     * so if you can't decide a seed prior to seeing your derived
     * implementations constructor inputs you should return a temporary
     * fake value initially.  You can then call {@link #reset()} in your 
     * own constructor after you've taken the necessary steps that allow
     * you to decide how to generate your own seed. 
     * </p>
     * @return Seed value
     */
    protected UUID freshSeed() {
        return UUID.randomUUID();
    }

    @Override
    public void reset() {
        UUID seed = this.freshSeed();
        long mostSigBits = seed.getMostSignificantBits();
        long leastSigBits = seed.getLeastSignificantBits();
        // Stamp on version and variant. Makes it an illegal UUID (unless all the bits are zero!)
        BitsLong.pack(mostSigBits, 0L, 12, 16) ;
        BitsLong.pack(leastSigBits, 0L, 62, 64) ;

        seedBytes = new byte[128 / 8];
        Bytes.setLong(seed.getMostSignificantBits(), seedBytes, 0);
        Bytes.setLong(seed.getLeastSignificantBits(), seedBytes, 8);
        if ( cache != null )
            cache.clear();
    }

    @Override
    public Node alloc(String label) {
        Callable<Node> getter = ()->alloc(Bytes.string2bytes(label));
        Node n = cache.getOrFill(label, getter);
        return n;
    }

    @Override
    public Node create() {
        counter++;
        // Make illegal string bytes so can't clash with alloc(String).
        // It is different because it has a zero (illegal in a Java string) in it.
        counterBytes[0] = 0;
        counterBytes[1] = 0;
        Bytes.setLong(counter, counterBytes, 2);
        return alloc(counterBytes);
    }

    /** Given the per-run seed and label bytes, make a blank node. */
    private Node alloc(byte[] labelBytes) {
        byte[] input = new byte[seedBytes.length+labelBytes.length];
        System.arraycopy(seedBytes, 0, input, 0, seedBytes.length);
        System.arraycopy(labelBytes, 0, input, seedBytes.length, labelBytes.length);
        
        // Apache Common Codec or Guava. 
        // The 2 versions of the code below should produce the same hex strings.
        //
        // The main difference from our perspective is that the Guava version
        // returns a byte[]. Hashes are not "large numbers" - they are bit patterns --
        // but it does create and use internal Java objects.
        //
        // We need to be careful about byte order. The long[] returned by
        // MurmurHash3 (Apache Commons) needs to be stringified as "low bytes first"
        // which is the reverse of %d-formatting for a long which is 
        // "high byte first" (in a left-to-right writing system).
        //
        // For byte output compatibility with byte[] from Guava,
        // need to reverse the bytes of the longs so that it prints "low to high"
        // Java works in big-endian -- high bytes first.

        String hexString;
        if ( true ) {
            long[] x = MurmurHash3.hash128(input);
            // dev: String xs = String.format("%016x%016x", Long.reverseBytes(x[0]), Long.reverseBytes(x[1]));
            char[] chars = new char[32];
            longAsHexLC(x[0], chars, 0);
            longAsHexLC(x[1], chars, 16);
            hexString = new String(chars);
        } else {
            // Guava. Several objects created.
            // Using 104729 makes it agree with Apache Commons Codec value.
            byte[] bytes = Hashing.murmur3_128(104729).hashBytes(input).asBytes();
            hexString = Bytes.asHexLC(bytes);
        }
        return NodeFactory.createBlankNode(hexString);
    }

    /** Long to hex (lower case) chars with low byte first. */
    private void longAsHexLC(long value, char[] chars, int start) {
        // Avoiding generating intermediate strings from e.g. Bytes.asHexLC
        // Byte loop.
        // Bytes get encoded "high bits first". "AF" is value A*16+F
        for ( int idx = 0 ; idx < 8 ; idx++ ) {
            int i = idx * 8;
            int bValue = (int)((value >> i) & 0xFF);
            // Keep order of the byte - high nibble, low nibble.
            int hi = (bValue & 0xF0) >> 4;
            int lo = (bValue & 0x0F);
            char chHi = Chars.hexDigitsLC[hi];
            char chLo = Chars.hexDigitsLC[lo];
            chars[start + 2 * idx] = chHi;
            chars[start + 2 * idx + 1] = chLo;
        }
    }
}
