/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.db.impl;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Bound;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.graph.query.Fixed;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.Query;


public class DBPattern  {
	int index;
	Element S;
	Element P;
	Element O;
	int Scost, Pcost, Ocost;
	boolean isStaged;
	boolean isConnected;  // pattern can be joined to previously staged pattern for this query.
	boolean isSingleSource;  // pattern has just one data source (specialized graph)
	boolean isStmt;  // pattern is over only asserted statement tables (no reified)
	boolean isReif;  // pattern is over only reified statement tables (no asserted)
	List source; // specialized graphs with triples for this pattern
	char subsumed;
	
	public DBPattern ( int i, Triple pattern, Mapping varMap ) {
		index = i;
		source = new ArrayList();
		isStaged = false;
		isConnected = false;
		isSingleSource = false;
		isStmt = isReif = false;
		S = nodeToElement(pattern.getSubject(), varMap);
		P = nodeToElement(pattern.getPredicate(), varMap);
		O = nodeToElement(pattern.getObject(), varMap);
		Scost = elementCost(S);
		Pcost = elementCost(P);
		Ocost = elementCost(O);
	}
	
	/**
		the code below is pretty much identical to that of
		graph.query.patternstagecompiler.compile.
	*/
	private Element nodeToElement( Node X, Mapping map )
		{
		if (X.equals( Query.ANY )) return Element.ANY;
		if (X.isVariable()) {
			if (map.hasBound(X))
				return new Bound (map.indexOf(X));
			else {
				freeVarCnt++;
				return new Free( X );
			}
		}
		return new Fixed( X );
		}

	
	public void sourceAdd ( SpecializedGraph sg, char sub ) {
		if ( source.isEmpty() ) {
			subsumed = sub;
			isSingleSource = true;
			if ( sg instanceof SpecializedGraphReifier_RDB ) isReif = true;
			else isStmt = true;
		} else {
			if ( subsumed != sub )
				throw new RDFRDBException("Specialized graphs incorrectly subsume pattern");			
			isSingleSource = false;
			if ( sg instanceof SpecializedGraphReifier_RDB ) isStmt = false;
			else isReif = false;
		}
		source.add(sg);
	}
	
	public boolean hasSource() { return !source.isEmpty(); }
	public boolean isSingleSource() { return isSingleSource; }
	public SpecializedGraph singleSource() { return (SpecializedGraph) source.get(0); }

	protected void getVars ( List varList, Mapping varMap ) {
		if (freeVarCnt > 0) {
			if (S instanceof Free)
				addVar(varList, (Free) S, varMap);
			if (P instanceof Free)
				addVar(varList, (Free) P, varMap);
			if (O instanceof Free)
				addVar(varList, (Free) O, varMap);
		}
	}
	
	private int findVar ( List varList, Node_Variable var ) {
		int i;
		for ( i=0; i<varList.size(); i++ ) {
			Node_Variable v = ((VarIndex) varList.get(i)).var;
			if ( var.equals(v) )
				return i;
		}
		return -1;		
	}

	private void addVar ( List varList, Free var, Mapping varMap ) {
		int i = findVar(varList,var.var());
		if ( i < 0 ) {
			i = varList.size();
			VarIndex vx;
			if ( var.isArg() ) {
				vx = new VarIndex (var.var(), var.getMapping(), i);
			} else {
				vx = new VarIndex (var.var(), i);
			}
			varList.add(vx);
		}
		var.setListing(i);
	}
	
	public boolean joinsWith ( DBPattern jsrc, List varList, boolean onlyStmt, boolean onlyReif ) {
		// currently, we can only join over the same table.
		// and, in general, we can't join if the pattern has a predicate variable.
		// but, if we are only querying asserted stmts and the pattern is
		// over asserted stmts, we can do the join.
		if ( jsrc.isSingleSource() && source.contains(jsrc.source.get(0)) && 
			( !(P instanceof Free) || (onlyStmt && isStmt) ) ) {
			// jsrc has same source. look for a join variables
			if ( (S instanceof Free) && (findVar(varList,((Free)S).var()) >= 0) )
					return true;
			if ( (O instanceof Free) && (findVar(varList,((Free)O).var()) >= 0) )
					return true;
			if ( onlyStmt && isStmt && (P instanceof Free) &&
					(findVar(varList,((Free)P).var()) >= 0) )
						return true;
		}
		return false;
	}
	
    
	/**
	 * Return the relative cost of evaluating the pattern with the current.
	 * @return the relative cost.
	 */
	
	public int cost ( Mapping varMap ) {
		if ( costInit ) {
			costInit = false;
			costCur = costCalc();
		} else if ( freeVarCnt > 0 ) {
			// only recompute cost if there's a chance it changed.
			if ( anyBound(varMap) ) {
				costCur = costCalc();
			}
		}
		return costCur;
	}
	
	static final int costMax = 100;
	static final int costMin = 1;
	int costCur;
	
	private boolean costInit = true;
	private int freeVarCnt = 0;
	
	protected boolean isArgCheck ( Free v, Mapping map ) {
		int ix;
		ix = map.lookUp(v.var());
		if ( ix >= 0 ) {
			v.setIsArg(ix);
			isConnected = true;
			freeVarCnt--;
			return true;
		} else
			return false;
	}

	protected boolean anyBound(Mapping map) {	
		boolean res = false;
		if ( S instanceof Free ) 
			if ( isArgCheck((Free)S,map) ) {
				Scost = elementCost(S);
				res = true;
			} 
		if ( P instanceof Free ) 
			if ( isArgCheck((Free)P,map) ) {
				Pcost = elementCost(P);
				res = true;
			} 
		if ( O instanceof Free ) 
			if ( isArgCheck((Free)O,map) ) {
				Ocost = elementCost(O);
				res = true;
			} 
		return res;
	}
	
	private int fixedCost = 0;
	private int boundCost = 0;
	private int unboundCost = 4;
	private int unboundPredFactor = 4;

	private int elementCost ( Element x ) {
		if ( x instanceof Fixed ) 
			return fixedCost;
		else if ( x instanceof Bound )
			return boundCost;
		else if ( (x instanceof Free) && ((Free)x).isArg() )
			return boundCost;
		else
			return unboundCost;
	}

	/*
	 * compute the "estimated cost" to evaluate the pattern. in fact,
	 * it is just a relative ranking that favors patterns with bound
	 * nodes (FIXED or bound variables) over unbound nodes (unbound
	 * variables and ANY).
	 * @return int The estimated cost in the range [costmin,costMax).
	 */
	 
	private int costCalc() {
		return Scost+Pcost+Ocost;
	}

}

/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
