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

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    A PatternCompiler is some class that knows how to handle fixed Node values,
    binding and bound occurences of variables, and wildcards.
*/

public interface PatternCompiler
    {
    /**
        Method called to deliver a compiled Element constructed from a constant Node.
    */
    public Element fixed( Node value );
    
    /**
        Method called to deliver a compiled element from a bound occurance of a 
        variable Node allocated at a given index position.
    */
    public Element bound( Node n, int index );
    
    /**
        Method called to deliver a compiled element from a binding occurance of a
        variable Node allocated at a given index position.
    */
    public Element bind( Node n, int index );
    
    /**
        Method called to deliver a compiled element from a wildcard ANY.
    */
    public Element any();
    }
