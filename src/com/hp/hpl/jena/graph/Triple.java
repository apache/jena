/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Triple.java,v 1.1.1.1 2002-12-19 19:13:34 bwm Exp $
*/

package com.hp.hpl.jena.graph;

/**
 * @author Jeremy Carroll
<br>
    Chris removed the mask methods, which were used nowhere.
 */
final public class Triple {
	private final Node subj, pred, obj;
	public Triple(Node s, Node p, Node o) {
//        if (s == null) throw new UnsupportedOperationException( "subject cannot be null" );
//        if (p == null) throw new UnsupportedOperationException( "predicate cannot be null" );
//        if (o == null) throw new UnsupportedOperationException( "object cannot be null" );
		subj = s;
		pred = p;
		obj = o;
	}
	
	public String toString() {
		return subj + " @" + pred + " " + obj;
	}
	public Node getSubject() {
		return subj;
	}
	public Node getPredicate() {
		return pred;
	}
	public Node getObject() {
		return obj;
	}

    /** 
        triples only equal other triples with the same components. 
    <br>
        internals: avoids grubbing around in the insides of the other triple.  
    */
    
	public boolean equals(Object o) 
        { return o instanceof Triple && ((Triple) o).sameAs( subj, pred, obj ); }
    
    /** 
        component-wise equality, might choose to make public.
    */    
    private boolean sameAs( Node s, Node p, Node o )
        { return subj.equals( s ) && pred.equals( p ) && obj.equals( o ); }
        
    public int hashCode() {
    	return (subj.hashCode() >> 1) ^ pred.hashCode() ^ (obj.hashCode() << 1);
    }
}

/*
    (c) Copyright Hewlett-Packard Company 2002
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
