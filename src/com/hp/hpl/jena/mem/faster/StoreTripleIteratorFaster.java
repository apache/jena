/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: StoreTripleIteratorFaster.java,v 1.5 2008-01-02 12:09:58 andy_seaborne Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.mem.*;

/**
    @deprecated now StoreTRipleIterator can handle both kinds of
    NodeToTriplesMap.
    @author hedgehog
*/
public class StoreTripleIteratorFaster extends TrackingTripleIterator
    {
    protected NodeToTriplesMapBase X;
    protected NodeToTriplesMapBase A;
    protected NodeToTriplesMapBase B;
    protected Graph toNotify;
    
    public StoreTripleIteratorFaster
        ( Graph toNotify, Iterator it, 
        NodeToTriplesMapBase X, 
        NodeToTriplesMapBase A, 
        NodeToTriplesMapBase B )
        { 
        super( it ); 
        this.X = X;
        this.A = A; 
        this.B = B; 
        this.toNotify = toNotify;
        }

    public void remove()
        {
        super.remove();
        X.removedOneViaIterator();
        A.remove( current );
        B.remove( current );
        toNotify.getEventManager().notifyDeleteTriple( toNotify, current );
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