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

package org.apache.jena.sys;

import java.io.Serializable;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/** The injection points for the Node and Triple {@link Serializable} process. 
 * This class is public to allow system initialization to inject
 * handler functions for  {@link Node} and {@link Triple}.
 * See also {@code Quad}. 
 */
public class Serializer {
    
    /*package*/ static Function<Node, Object> nodeWriteReplaceFunction = null;
    /*package*/ static Function<Triple, Object> tripleWriteReplaceFunction = null;
    
    /** Set the node serializer replacement function.
     * This is a function called by {@code Node.writeReplace} during the {@link Serializable} process.
     * The return is an object used in place of {@link Node} for the serialization.  
     * 
     * <PRE>
     * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
     * </PRE><p>
     * The returned object must provide
     * <PRE>
     * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
     * </PRE><p>  
     * where "Object" is a {@link Node}.
     * 
     * @see java.io.Serializable
     */
    public static void setNodeSerializer(Function<Node, Object> writeReplaceFunction) {
        nodeWriteReplaceFunction = writeReplaceFunction;
    }
    
    /** Return the current node serializer replacement function. */
    public static Function<Node, Object> getNodeSerializer() {
        return nodeWriteReplaceFunction;
    }

    /** Set the triple serializer replacement function.
     * This is a function called by {@code Triple.writeReplace} during the {@link Serializable} process.
     * The return is an object used in place of {@link Triple} for the serialization.  
     * 
     * <PRE>
     * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
     * </PRE><p>
     * The returned object must provide
     * <PRE>
     * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
     * </PRE><p>  
     * where "Object" is a {@link Triple}.
     * 
     * @see java.io.Serializable
     */
    public static void setTripleSerializer(Function<Triple, Object> writeReplaceFunction) {
        tripleWriteReplaceFunction = writeReplaceFunction;
    }

    /** Return the current triple serializer replacement function. */
    public static Function<Triple, Object> getTripleSerializer() {
        return tripleWriteReplaceFunction;
    }
}
