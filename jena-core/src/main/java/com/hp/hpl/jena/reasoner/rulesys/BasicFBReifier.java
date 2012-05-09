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

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class BasicFBReifier implements Reifier
    {
    protected final GetReifier deductions;
    protected final Graph parent;
    protected final Reifier base;
    
    public BasicFBReifier( BasicForwardRuleInfGraph parent, Reifier base, GetReifier deductions, ReificationStyle style )
        {
        this.deductions = deductions;
        this.parent = parent;
        this.base = base;
        }
    
    interface GetReifier
        { Reifier getReifier(); }

    @Override
    public ExtendedIterator<Node> allNodes()
        { return base.allNodes().andThen( deductions.getReifier().allNodes() ); }

    @Override
    public ExtendedIterator<Node> allNodes( Triple t )
        { return base.allNodes( t ).andThen( deductions.getReifier().allNodes() );  }

    @Override
    public void close()
        { base.close(); }

    @Override
    public ExtendedIterator<Triple> find( TripleMatch m )
        { return base.find( m ).andThen( deductions.getReifier().find( m ) ); }

    @Override
    public ExtendedIterator<Triple> findEither( TripleMatch m, boolean showHidden )
        { 
        return 
            base.findEither(  m, showHidden )
            .andThen( deductions.getReifier().findEither(  m, showHidden ) ); 
        }

    @Override
    public ExtendedIterator<Triple> findExposed( TripleMatch m )
        { return base.findExposed( m ).andThen( deductions.getReifier().findExposed( m ) );  }

    @Override
    public Graph getParentGraph()
        { return parent; }

    @Override
    public ReificationStyle getStyle()
        { return base.getStyle(); }

    @Override
    public boolean handledAdd( Triple t )
        { return base.handledAdd( t ); }

    @Override
    public boolean handledRemove( Triple t )
        { return base.handledRemove( t ); }

    @Override
    public boolean hasTriple( Node n )
        { return base.hasTriple( n ) || deductions.getReifier().hasTriple( n ); }

    @Override
    public boolean hasTriple( Triple t )
        { return base.hasTriple( t ) || deductions.getReifier().hasTriple( t ); }

    @Override
    public Node reifyAs( Node n, Triple t )
        { return base.reifyAs( n, t ); }

    @Override
    public void remove( Node n, Triple t )
        { base.remove( n, t ); }

    @Override
    public void remove( Triple t )
        { base.remove(  t  ); }

    @Override
    public int size()
        { return deductions.getReifier().size(); }

    @Override
    public Triple getTriple( Node n )
        {
        Triple a = base.getTriple( n );
        Triple b = deductions.getReifier().getTriple( n );
        if (a != null && b != null) throw new JenaException( "TODO: have multiple answers for getTrple, viz " + a + " and " + b );
        return a == null ? b : a;
        }
    }
