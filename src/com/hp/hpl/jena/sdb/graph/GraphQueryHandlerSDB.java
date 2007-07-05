/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class GraphQueryHandlerSDB implements QueryHandler
{
    public BindingQueryPlan prepareBindings( Query q, Node [] variables )
    {
        return null ;
    }
    

    public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] p )
    {
        return null ; 
    }

    public TreeQueryPlan prepareTree( Graph pattern )
    {
        return null ; 
    }

    // Special cases.
    
    public ExtendedIterator subjectsFor( Node p, Node o )
    {
        return null ; 
    }
    
    public ExtendedIterator predicatesFor( Node s, Node o )
    {
        return null ; 
    }
    
    public ExtendedIterator objectsFor( Node s, Node p )
    {
        return null ; 
    }

    public boolean containsNode( Node n )
    {
        return false ; 
    }
}

class TreeQueryPlanSDB implements TreeQueryPlan
{

    public Graph executeTree()
    {
        return null ;
    }
}

class StageSDB extends Stage
{
    @Override
    public Pipe deliver(Pipe arg0)
    {
        return null ;
    }
    
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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