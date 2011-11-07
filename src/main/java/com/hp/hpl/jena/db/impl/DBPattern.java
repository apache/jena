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

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Bound;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.graph.query.Fixed;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.shared.BrokenException;

public class DBPattern  
    {
    final Triple pattern;
    final Element S;
    final Element P;
    final Element O;
	
    private int Scost, Pcost, Ocost;
	
    private boolean isBusy;
    
	private boolean isConnected;  // pattern can be joined to previously staged pattern for this query.
	
    private boolean isStmt;  // pattern is over only asserted statement tables (no reified)
	private boolean isReif;  // pattern is over only reified statement tables (no asserted)
    
	private List<SpecializedGraph> sources; // specialized graphs with triples for this pattern
	
    private char subsumed;
	
	public DBPattern ( Triple pat, Mapping varMap ) {
		pattern = pat;
		sources = new ArrayList<SpecializedGraph>();
		isBusy = false;
		isConnected = false;
		isStmt = isReif = false;
		S = nodeToElement( pattern.getSubject(), varMap );
		P = nodeToElement( pattern.getPredicate(), varMap );
		O = nodeToElement( pattern.getObject(), varMap );
		Scost = elementCost(S);
		Pcost = elementCost(P);
		Ocost = elementCost(O);
	}

    public void setBusy()
        { // pro tem, in case the old `isStaged` actually still meant something
        if (isBusy) throw new BrokenException( "a DBPattern can be made busy at most once" );
        isBusy = true;
        }
    
    public boolean isConnected()
        { return isConnected; }
	/**
		this nodeToElement is pretty much identical to that of
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

	
	public void sourceAdd( SpecializedGraph sg, char sub )
        {
        if (sources.isEmpty())
            {
            subsumed = sub;
            if (sg instanceof SpecializedGraphReifier_RDB) isReif = true;
            else isStmt = true;
            }
        else
            {
            if (subsumed != sub) throw new RDFRDBException( "Specialized graphs incorrectly subsume pattern" );
            if (sg instanceof SpecializedGraphReifier_RDB) isStmt = false;
            else isReif = false;
            }
        sources.add( sg );
        }
	
	public boolean hasSource() 
        { return sources.size() > 0; }
    
    /**
        Answer true iff this pattern [currently] is associated with exactly one source.
    */
	public boolean isSingleSource() 
        { return sources.size() == 1; }
	
    public SpecializedGraph singleSource() { return sources.get(0); }

	protected void addFreeVars ( List<VarDesc> varList ) {
		if (freeVarCnt > 0) {
			if (S instanceof Free)
				addVar(varList, (Free) S);
			if (P instanceof Free)
				addVar(varList, (Free) P);
			if (O instanceof Free)
				addVar(varList, (Free) O);
		}
	}
	
	private int findVar ( List<VarDesc> varList, Node_Variable var ) {
		for (int i = 0; i < varList.size(); i += 1 ) {
			Node_Variable v = varList.get(i).var;
			if (var.equals( v )) return i;
		}
		return -1;		
	}

	private void addVar ( List<VarDesc> varList, Free var ) {
		int i = findVar(varList,var.var());
		if ( i < 0 ) {
			i = varList.size();
			VarDesc vx;
			if ( var.isArg() ) {
				vx = new VarDesc (var.var(), var.getMapping(), i);
			} else {
				vx = new VarDesc (var.var(), i);
			}
			varList.add(vx);
		}
		var.setListing(i);
	}

    /**
        currently, we can only join over the same table, and, in general, we 
        can't join if the pattern has a predicate variable -- but, if we are only 
        querying asserted stmts and the pattern is over asserted stmts, we can 
        do the join.
    */
	public boolean joinsWith
        ( DBPattern other, List<VarDesc> varList, boolean onlyStmt, boolean onlyReif, boolean implicitJoin )
        {
        boolean includesSource = other.isSingleSource() && sources.contains( other.sources.get( 0 ) );
        boolean newSourceTest = sources.containsAll( other.sources );
        // if (includesSource != newSourceTest) System.err.println( ">> old source test: " + includesSource + ", but new source test: " + newSourceTest );
        if (includesSource && (!(P instanceof Free) || (onlyStmt && isStmt)))
            { // other has same source. See if there's a join variable.
            return 
                appearsIn( S, varList ) 
                || appearsIn( O, varList )
                || (onlyStmt && isStmt && appearsIn( P, varList )) 
                || (implicitJoin && shareFixedSubject( other )) 
                ;
            }
        return false;
        }

    private boolean shareFixedSubject( DBPattern other )
        { // Yukk.
        boolean originalDefinition = 
            S instanceof Fixed
            && other.S instanceof Fixed
            && S.match( (Domain) null, other.S.asNodeMatch( (Domain) null ) )
            ;
        return 
            originalDefinition;
        }

    /**
     	Answer true iff <code>e</code> is a free variable that appears in
        <code>varList</code>.
    */
    private boolean appearsIn( Element e, List<VarDesc> varList )
        { return e instanceof Free && findVar( varList, ((Free) e).var() ) >= 0; }
	
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
		int ix = map.lookUp( v.var() );
		if ( ix >= 0 ) {
			v.setIsArg( ix );
			isConnected = true;
			freeVarCnt -= 1;
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
	// private int unboundPredFactor = 4;

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
