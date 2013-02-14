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

package com.hp.hpl.jena.sparql.graph;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler ;
import com.hp.hpl.jena.graph.impl.WrappedGraph ;

public class UnmodifiableGraph extends WrappedGraph
{
    public UnmodifiableGraph(Graph base)
    {
        super(base) ;
        bud = new SimpleBulkUpdateHandler(this) ;
    }
    
    /** Return base graph that this class protects.  Caveat emptor. */
    public Graph unwrap()   { return super.base ; }
    
    @Override
    public void performAdd(Triple triple)
    { throw new UnsupportedOperationException() ; }
    
    @Override
    public void performDelete(Triple triple)
    { throw new UnsupportedOperationException() ; }
}
