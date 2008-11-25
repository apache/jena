/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;

public class BulkUpdateHandlerTDB extends SimpleBulkUpdateHandler
{

    /**
     * @param graph
     */
    public BulkUpdateHandlerTDB(GraphWithPerform graph)
    {
        super(graph) ;
    }
    
//  @Override
//  public void removeAll()
//  {}

    
//
//    @Override
//    public void add(Triple[] triples)
//    {}
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void add(List triples)
//    {}
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void add(Iterator it)
//    {}
//
//    @Override
//    public void add(Graph g)
//    {}
//
//    @Override
//    public void add(Graph g, boolean withReifications)
//    {}
//
//    @Override
//    public void delete(Triple[] triples)
//    {}
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void delete(List triples)
//    {}
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void delete(Iterator it)
//    {}
//
//    @Override
//    public void delete(Graph g)
//    {}
//
//    @Override
//    public void delete(Graph g, boolean withReifications)
//    {}
//
//    @Override
//    public void remove(Node s, Node p, Node o)
//    {}
//
//    @Override
//    public void removeAll()
//    {}

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */