/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: ReificationStatementMask.java,v 1.3 2008-01-02 12:08:23 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Mutable statement-component masks for SpecializedGraphReifier.
    (Extracted from same).
*/
class ReificationStatementMask {
	
		protected int mask;
			
		public static final int HasSubj = 1;
		public static final int HasPred = 2;
		public static final int HasObj = 4;
		public static final int HasType = 8;
		public static final int HasSPOT = 15;
		public static final int IsStmt = 16;
		public static final int HasNada = 0;
	
		public boolean hasSubj () { return (mask & HasSubj) == HasSubj; }
		public boolean hasPred () { return (mask & HasPred) == HasPred; }
		public boolean hasObj () { return (mask & HasObj) == HasObj; }
		public boolean hasType () { return (mask & HasType) == HasType; }
		public boolean hasSPOT () { return (mask & HasSPOT) == HasSPOT; }
		public boolean isStmt () { return (mask & IsStmt) == IsStmt; }
		public boolean hasNada () { return mask == HasNada; }
		public boolean hasOneBit () { return ( (mask == HasSubj) ||
			(mask == HasPred) || (mask == HasObj) || ( mask == HasType) );
		}
			
		// note: have SPOT does not imply a reification since
		// 1) there may be multiple fragments for prop, obj
		// 2) the fragments may be in multiple tuples
	
		ReificationStatementMask ( Triple t ) {
			mask = HasNada;
			Node p = t.getPredicate();
			if ( p != null ) {
				if ( p.equals(RDF.Nodes.subject) ) mask = HasSubj;
				else if ( p.equals(RDF.Nodes.predicate) ) mask = HasPred; 
				else if ( p.equals(RDF.Nodes.object) ) mask = HasObj; 
				else if ( p.equals(RDF.Nodes.type) ) {
						Node o = t.getObject();
						if ( o.equals(RDF.Nodes.Statement) ) mask = HasType;
				}
			}			
		}
	
		ReificationStatementMask () { mask = HasNada; }
	
		public void setMerge ( ReificationStatementMask m ) {
			mask |= m.mask;	
		}
			
		public void setHasType () {
			mask |= HasType;	
		}
		
		public void setMask ( boolean hasSubj, boolean hasProp, boolean hasObj, boolean hasType ) {
			if ( hasSubj ) mask |= HasSubj;
			if ( hasProp) mask |= HasPred;
			if ( hasObj) mask |= HasObj;
			if ( hasType ) mask |= HasType;	
		}

	
		public void setHasSubj () {
			mask |= HasSubj;	
		}
		
		public void setHasPred () {
			mask |= HasPred;	
		}

		public void setHasObj () {
			mask |= HasObj;	
		}
		
		public void setIsStmt () {
			mask |= IsStmt;	
		}
	
		public boolean hasIntersect ( ReificationStatementMask m ) {
			return (mask & m.mask) != 0;	
		}
	
		public boolean equals ( ReificationStatementMask m ) {
			return mask == m.mask;	
		}

}

/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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