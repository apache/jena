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

package com.hp.hpl.jena.tdb.store.nodetable ;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;

/** Encode/decode for Nodes into bytes */
public interface Nodec {
    /**
     * Calculate the maximum number of bytes needed for a Node. This needs to be
     * an overestimate and is used to ensure there is space in the bytebuffer
     * passed to encode.
     */
    public int maxSize(Node node) ;

    /**
     * Encode the node into the byte buffer, starting at the given offset. The
     * ByteBuffer will have position/limit around the space used on return,
     * <b>without a length code<b>.
     * 
     * @param node Node to encode.
     * @param bb ByteBuffer
     * @param pmap Optional prefix mapping. Can be null.
     * @return Length of byte buffer used for the whole encoding.
     */
    public int encode(Node node, ByteBuffer bb, PrefixMapping pmap) ;

    /**
     * Decode the node from the byte buffer. The ByteBuffer position should be
     * the start of the encoding (no binary length for example)
     * 
     * @param bb ByteBuffer
     * @param pmap Optional prefix mapping. Can be null.
     * @return the decoded Node.
     */
    public Node decode(ByteBuffer bb, PrefixMapping pmap) ;
}
