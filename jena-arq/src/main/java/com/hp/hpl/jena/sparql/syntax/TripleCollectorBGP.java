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

package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;

/** A triples-only TripleCollector. */

public class TripleCollectorBGP implements TripleCollectorMark
{
    BasicPattern bgp = new BasicPattern() ;
    
    public TripleCollectorBGP() {}
    
    public BasicPattern getBGP() { return bgp ; }
    
    @Override
    public void addTriple(Triple t) { bgp.add(t) ; }
    
    @Override
    public int mark() { return bgp.size() ; }
    
    @Override
    public void addTriple(int index, Triple t) { bgp.add(index, t) ; }
    
    @Override
    public void addTriplePath(TriplePath path)
    { throw new ARQException("Triples-only collector") ; }

    @Override
    public void addTriplePath(int index, TriplePath path)
    { throw new ARQException("Triples-only collector") ; }
}
