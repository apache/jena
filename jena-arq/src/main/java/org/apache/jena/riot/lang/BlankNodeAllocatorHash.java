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

package org.apache.jena.riot.lang ;

import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;
import java.util.UUID ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.cache.Getter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;

/**
 * Allocate bnode labels using a per-run seed and the label presented.
 * <p>
 * This is the most scalable, always legal allocator.
 * <p>
 * New allocators must be created per parser run, or .reset() called. These are
 * fed to a digest to give a bit string, (currently MD5, to get a 128bit bit
 * string) that is used to form a bNode AnonId of hex digits.
 * <p>
 * In addition there is a cache of label->node allocations, using the natural
 * tendency to locality in a database dump. (subject bNodes, bNodes in lists
 * and other data values structures like unit values).
 * <p>
 * Not thread safe.
 */

public class BlankNodeAllocatorHash implements BlankNodeAllocator {
    private static String       DigestAlgorithm = "MD5" ;
    private static int          CacheSize       = 1000 ;
    private MessageDigest       mDigest ;
    private byte[]              seedBytes ;
    // long+2 bytes to distinguish from UTF-8 bytes.
    private byte[]              counterBytes    = new byte[10] ; 
    private Cache<String, Node> cache ;
    private long                counter         = 0 ;

    public BlankNodeAllocatorHash() {
        reset() ;
        try {
            mDigest = MessageDigest.getInstance(DigestAlgorithm) ;
        } catch (NoSuchAlgorithmException e) {
            throw new InternalErrorException("failed to create message digest", e) ;
        }

        Getter<String, Node> getter = new Getter<String, Node>() {
            @Override
            public Node get(String key) {
                return alloc(key) ;
            }
        } ;
        Cache<String, Node> cache1 = CacheFactory.createCache(CacheSize) ;
        cache = CacheFactory.createCacheWithGetter(cache1, getter) ;
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
        seedBytes = new byte[128 / 8] ;
        Bytes.setLong(seed.getMostSignificantBits(), seedBytes, 0) ;
        Bytes.setLong(seed.getLeastSignificantBits(), seedBytes, 8) ;
    }

    @Override
    public Node alloc(String label) {
        return alloc(Bytes.string2bytes(label)) ;
    }

    @Override
    public Node create() {
        counter++ ;
        // Make illegal string bytes so can't clash with alloc(String)
        counterBytes[0] = 0 ;
        counterBytes[1] = 0 ;
        Bytes.setLong(counter, counterBytes, 2) ;
        return alloc(counterBytes) ;
    }

    private Node alloc(byte[] labelBytes) {
        mDigest.update(seedBytes) ;
        mDigest.update(labelBytes) ;
        byte[] bytes = mDigest.digest() ; // resets
        String hexString = Bytes.asHexLC(bytes) ;
        return NodeFactory.createAnon(new AnonId(hexString)) ;
    }
}
