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

package com.hp.hpl.jena.sparql.engine.optimizer.reorder;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;


/** A mutable triple pattern */
public final class PatternTriple
{
    public Item subject ;
    public Item predicate ;
    public Item object ;
    
    public static PatternTriple parse(Item pt)
    { 
        ItemList list = pt.getList();
        return new PatternTriple(list.get(0), list.get(1), list.get(2)) ; 
    }
    
    public PatternTriple(Item s, Item p, Item o)
    {
        set(normalize(s), normalize(p), normalize(o)) ;
    }
    
    private void set(Item s, Item p, Item o) 
    {
        subject =    s ;
        predicate =  p ;
        object =     o ;
    }
    
    public PatternTriple(Node s, Node p, Node o)
    {
        set(normalize(s),
            normalize(p),
            normalize(o)) ;
    }
    
    public PatternTriple(Triple triple)
    {
        this(triple.getSubject(),
             triple.getPredicate(),
             triple.getObject()) ;
    }
    
    @Override
    public String toString()
    { return subject+" "+predicate+" "+object ; }
    
    private static Item normalize(Item x)
    { return x != null ? x : PatternElements.ANY ; }
    
    private static Item normalize(Node x)
    { return x != null ? Item.createNode(x) : PatternElements.ANY ; }
}
