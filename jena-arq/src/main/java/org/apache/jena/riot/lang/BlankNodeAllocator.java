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

import org.apache.jena.graph.Node;

/** Interface to allocators for blank nodes. */
public interface BlankNodeAllocator
{
    /** Allocate based on a non-null label.
     * Calling this twice, with the same label will generate equivalent nodes
     * but they may not be identical (i.e they are .equals but may not be ==) 
     */
    public Node alloc(String label);
    
    /** Create a fresh blank node, different from anything generated so far.
     *  Will not clash with a node allocated by {@link #alloc}
     */
    public Node create();
    
    /** Reset allocation state - calls to {@link #alloc} or {@link #create} */    
    public void reset();
}

