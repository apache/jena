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

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Interface encapsulating a mapping from a name to a value. */

public interface Binding
{
    /** Iterate over all variables of this binding. */
    public Iterator<Var> vars() ;

    /** Test whether a variable is bound to some object */
    public boolean contains(Var var) ;

    /** Return the object bound to a variable, or null */
    public Node get(Var var) ;
    
    /** Number of (var, value) pairs. */
    public int size() ;

    /** Is this an empty binding?  No variables. */
    public boolean isEmpty() ;

}
