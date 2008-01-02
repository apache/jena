/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: FasterTripleStore.java,v 1.23 2008-01-02 12:09:58 andy_seaborne Exp $
*/
package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.*;

public class FasterTripleStore extends GraphTripleStoreBase implements TripleStore
    {    
    public FasterTripleStore( Graph parent )
        { 
        super( parent,
            new NodeToTriplesMapFaster( Field.getSubject, Field.getPredicate, Field.getObject ),
            new NodeToTriplesMapFaster( Field.getPredicate, Field.getObject, Field.getSubject ),
            new NodeToTriplesMapFaster( Field.getObject, Field.getSubject, Field.getPredicate )
                ); 
        }
    
    public NodeToTriplesMapFaster getSubjects()
        { return (NodeToTriplesMapFaster) subjects; }

    public NodeToTriplesMapFaster getPredicates()
        { return (NodeToTriplesMapFaster) predicates; }
    
    protected NodeToTriplesMapFaster getObjects()
        { return (NodeToTriplesMapFaster) objects; }
    
    public Applyer createApplyer( ProcessedTriple pt )
        {
        if (pt.hasNoVariables())
            return containsApplyer( pt );
        if (pt.S instanceof QueryNode.Fixed) 
            return getSubjects().createFixedSApplyer( pt );
        if (pt.O instanceof QueryNode.Fixed) 
            return getObjects().createFixedOApplyer( pt );
        if (pt.S instanceof QueryNode.Bound) 
            return getSubjects().createBoundSApplyer( pt );
        if (pt.O instanceof QueryNode.Bound) 
            return getObjects().createBoundOApplyer( pt );
        return varSvarOApplyer( pt );
        }

    protected Applyer containsApplyer( final ProcessedTriple pt )
        { 
        return new Applyer()
            {
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                Triple t = new Triple( pt.S.finder( d ), pt.P.finder( d ), pt.O.finder( d ) );
                if (objects.containsBySameValueAs( t )) next.run( d );
                }    
            };
        }

    protected Applyer varSvarOApplyer( final QueryTriple pt )
        { 
        return new Applyer()
            {
            protected final QueryNode p = pt.P;
        
            public Iterator find( Domain d )
                {
                Node P = p.finder( d );
                if (P.isConcrete())
                    return predicates.iterator( P, Node.ANY, Node.ANY );
                else
                    return subjects.iterateAll();
                }
    
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                Iterator it = find( d );
                while (it.hasNext())
                    if (m.match( d, (Triple) it.next() )) 
                         next.run( d );
                }
            };
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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