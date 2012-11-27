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

package com.hp.hpl.jena.sparql.engine;

import org.apache.jena.atlas.lib.Closeable ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.util.PrintSerializable ;

public interface Plan extends PrintSerializable, Closeable
{
    // General markers, especially structural elements
    public static final String startMarker = "(" ;
    public static final String finishMarker = ")" ;

    // Used for small things.
    public static final String startMarker2 = "(" ;
    public static final String finishMarker2 = ")" ;
    
    public Op getOp() ;
    public QueryIterator iterator() ;
}
