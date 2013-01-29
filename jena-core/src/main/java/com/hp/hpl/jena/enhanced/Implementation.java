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

package com.hp.hpl.jena.enhanced;
import com.hp.hpl.jena.graph.*;

/**
 * <p>
 * Interface defining a generic factory interface for generating enhanced nodes
 * from normal graph nodes. Implementation classes should have a public final 
 * member variable called factory of this type.
 * </p>
 */
public abstract class Implementation {

     /** 
      * Create a new EnhNode wrapping a Node in the context of an EnhGraph
      * @param node The node to be wrapped
      * @param eg The graph containing the node
      * @return A new enhanced node which wraps node but presents the interface(s)
      *         that this factory encapsulates.
      */
     public abstract EnhNode wrap( Node node,EnhGraph eg );
     
     /**
        true iff wrapping (node, eg) would succeed.
        @param node the node to test for suitability
        @param eg the enhanced graph the node appears in
        @return true iff the node can represent our type in that graph
     */
     public abstract boolean canWrap( Node node, EnhGraph eg );
     
}
