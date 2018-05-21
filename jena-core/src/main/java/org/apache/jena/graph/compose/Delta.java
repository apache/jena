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

package org.apache.jena.graph.compose ;

import org.apache.jena.graph.Capabilities ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphPlain ;
import org.apache.jena.graph.impl.SimpleEventManager ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * Graph operation for wrapping a base graph and leaving it unchanged while
 * recording all the attempted updates for later access.
 * 
 * The behavior of this class is not well defined if triples are added to or
 * removed from the base graph, the additions graph, or the deletions graph
 * while this graph is in use.
 */

public class Delta extends CompositionBase implements Graph
{
    private Graph base ;
    private Graph additions ;
    private Graph deletions ;

    public Delta(Graph base)
    {
        super() ;
        this.base = GraphPlain.plain(base);
        this.additions = GraphPlain.plain();
        this.deletions = GraphPlain.plain();
    }

    @Override
    public Capabilities getCapabilities() {
        // Not stricly accurate.
        return base.getCapabilities();
    }

    /**
     * Answer the graph of all triples added.
     */
    public Graph getAdditions()
    {
        return additions ;
    }

    /**
     * Answer the graph of all triples removed.
     */
    public Graph getDeletions()
    {
        return deletions ;
    }

    /**
     * Add the triple to the graph, ie add it to the additions, remove it from
     * the removals.
     */
    @Override
    public void performAdd(Triple t)
    {
        if (!base.contains(t))
            additions.add(t) ;
        deletions.delete(t) ;
    }

    /**
     * Remove the triple, ie, remove it from the adds, add it to the removals.
     */
    @Override
    public void performDelete(Triple t)
    {
        additions.delete(t) ;
        if (base.contains(t))
            deletions.add(t) ;
    }

    /**
     * Find all the base triples matching tm, exclude the ones that are deleted,
     * add the ones that have been added.
     */
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple t)
    {
        ExtendedIterator<Triple> iterator = base.find(t).filterDrop(ifIn(GraphUtil.findAll(deletions))).andThen(additions.find(t)) ;
        return SimpleEventManager.notifyingRemove( this, iterator ) ;
    }

    @Override
    public void close()
    {
        super.close() ;
        base.close() ;
        additions.close() ;
        deletions.close() ;
    }

    @Override
    public int graphBaseSize()
    {
        return base.size() + additions.size() - deletions.size() ;
    }
}
