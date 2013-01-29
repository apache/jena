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

package com.hp.hpl.jena.graph.compose ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.util.iterator.* ;

/**
 * Graph operation for wrapping a base graph and leaving it unchanged while
 * recording all the attempted updates for later access.
 */

@Deprecated
public class Delta extends Dyadic implements Graph
{
    private Graph base ;

    public Delta(Graph base)
    {
        super(Factory.createGraphMem(), Factory.createGraphMem()) ;
        this.base = base ;
    }

    /**
     * Answer the graph of all triples added
     */
    public Graph getAdditions()
    {
        return L ;
    }

    /**
     * Answer the graph of all triples removed
     */
    public Graph getDeletions()
    {
        return R ;
    }

    /**
     * Add the triple to the graph, ie add it to the additions, remove it from
     * the removals.
     */
    @Override
    public void performAdd(Triple t)
    {
        if (!base.contains(t)) 
            L.add(t) ;
        R.delete(t) ;
    }

    /**
     * Remove the triple, ie, remove it from the adds, add it to the removals.
     */
    @Override
    public void performDelete(Triple t)
    {
        L.delete(t) ;
        if (base.contains(t)) 
            R.add(t) ;
    }

    /**
     * Find all the base triples matching tm, exclude the ones that are deleted,
     * add the ones that have been added.
     */
    @Override
    protected ExtendedIterator<Triple> _graphBaseFind(TripleMatch tm)
    {
        return base.find(tm).filterDrop(ifIn(GraphUtil.findAll(R))).andThen(L.find(tm)) ;
    }

    @Override
    public void close()
    {
        super.close() ;
        base.close() ;
    }

    @Override
    public int graphBaseSize()
    {
        return base.size() + L.size() - R.size() ;
    }
}
