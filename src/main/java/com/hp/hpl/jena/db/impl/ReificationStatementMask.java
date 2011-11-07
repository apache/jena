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
