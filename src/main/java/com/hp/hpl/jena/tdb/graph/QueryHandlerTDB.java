/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;

import com.hp.hpl.jena.tdb.store.GraphTDB;

public class QueryHandlerTDB extends SimpleQueryHandler implements QueryHandler
{

    public QueryHandlerTDB(GraphTDB graph)
    {
        super(graph) ;
    }

    // ----------------------
    @Override
    public boolean containsNode(Node n)
    {
        return super.containsNode(n) ;
    }

    @Override
    public ExtendedIterator<Node> subjectsFor(Node p, Node o)
    {
        return super.subjectsFor(p, o) ;
    }

    @Override
    public ExtendedIterator<Node> predicatesFor(Node s, Node o)
    {
        return super.predicatesFor(s, o) ;
    }

    @Override
    public ExtendedIterator<Node> objectsFor(Node s, Node p)
    {
        return super.objectsFor(s, p) ;
    }
    // ----------------------

//    @Override
//    public Stage patternStage(Mapping map, ExpressionSet constraints, Triple[] p)
//    {
//        return null ;
//    }
//
//    @Override
//    public BindingQueryPlan prepareBindings(Query q, Node[] variables)
//    {
//        return null ;
//    }
//
//    @Override
//    public TreeQueryPlan prepareTree(Graph pattern)
//    {
//        return null ;
//    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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