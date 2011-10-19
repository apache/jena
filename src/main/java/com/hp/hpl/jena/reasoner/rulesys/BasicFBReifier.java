/*
 	(c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: BasicFBReifier.java,v 1.1 2009-06-29 08:55:38 castagna Exp $
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


/*
    (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/