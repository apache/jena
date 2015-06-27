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

package org.apache.jena.sparql.engine.binding;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Var ;

/** Special purpose binding for nothing. Surprisingly useful.
 */

public class Binding0 extends BindingBase
{
    /* package */ Binding0() { super(null) ; }
    /* package */ Binding0(Binding parent) { super(parent) ; }

    /** Iterate over all the names of variables.
     */
    @Override
    public Iterator<Var> vars1() { return Iter.nullIterator() ; }

    @Override
    protected int size1() { return 0 ; }
    
    @Override
    protected boolean isEmpty1() { return true ; }
    
    @Override
    public boolean contains1(Var var) { return false ; }
    
    @Override
    public Node get1(Var var) { return null ; }
}
