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

package org.apache.jena.graph;

import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.shared.impl.JenaParameters ;

/**
 * Functions related to blank nodes, and the implementation.
 */

public class BlankNodeId extends java.lang.Object {
    /**
     * Support for debugging ONLY: global BlankNodeId counter. The intial value is
     * just to make the output look prettier if it has lots (but not lots and
     * lots) of bnodes in it.
     */
    private static AtomicInteger idCount = new AtomicInteger(100000) ;

    /**
     * Allocate a fresh blank node label.
     * @see JenaParameters#disableBNodeUIDGeneration
     */
    public static String createFreshId() {
     if (JenaParameters.disableBNodeUIDGeneration)
            return "A" + idCount.getAndIncrement();
        else
            // Unique but uses entropy so creating large numbers quickly
            // can become a problem. See BlankNodeAllocator in jena-arq
            return java.util.UUID.randomUUID().toString();
    }
}
