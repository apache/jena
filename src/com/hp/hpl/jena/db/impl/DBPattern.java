/*
 * Created on Aug 7, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
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
	boolean isStaged; // 
	List source; // specialized graphs with triples for this pattern
	char subsumed;
	
	public DBPattern ( int i, Triple pattern, Mapping varMap ) {
		index = i;
		source = new ArrayList();
		isStaged = false;
		S = nodeToElement(pattern.getSubject(), varMap);
		P = nodeToElement(pattern.getPredicate(), varMap);
		O = nodeToElement(pattern.getObject(), varMap);		
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
		if ( source.isEmpty() )
			subsumed = sub;
		else if ( subsumed != sub )
			throw new RDFRDBException("Specialized graphs incorrectly subsume pattern");			
		source.add(sg);
	}
	
	public boolean hasSource() { return !source.isEmpty(); }
	public boolean isSingleSource() { return source.size() == 1; }
	public SpecializedGraph singleSource() { return (SpecializedGraph) source.get(0); }

	protected void getFree(List freeVar) {
		if (freeVarCnt > 0) {
			if (S instanceof Free)
				addFree(freeVar, (Free) S);
			if (P instanceof Free)
				addFree(freeVar, (Free) P);
			if (O instanceof Free)
				addFree(freeVar, (Free) O);
		}
	}
	
	private int findFree ( List freeVar, Node_Variable var ) {
		int i;
		for ( i=0; i<freeVar.size(); i++ ) {
			if ( var.equals((Node_Variable) freeVar.get(i)) )
				return i;
		}
		return -1;		
	}

	private boolean isFree ( List freeVar, Node_Variable var ) {
		return findFree(freeVar,var) >= 0;		
	}
	
	private void addFree ( List freeVar, Free var ) {
		int i = findFree(freeVar,var.var());
		if ( i < 0 ) {
			i = freeVar.size();
			freeVar.add(var.var());
		}
		var.bind(i);
	}
	
	private boolean joinCheck ( List freeVar ) {
		if ( !(P instanceof Fixed) ) return false;
		if ( S instanceof Free ) return isFree(freeVar, ((Free)S).var());
		if ( O instanceof Free ) return isFree(freeVar, ((Free)O).var());
		return false;
	}

	public boolean joinsWith ( DBPattern jsrc, List resVar ) {
		if ( P instanceof Fixed ) {
			if ( jsrc.isSingleSource() && source.contains(jsrc.source.get(0)) )
				// jsrc has same source. look for a join variables
				return jsrc.joinCheck(resVar);
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
			if ( rebind(varMap) ) {
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
	
	protected boolean rebind( Mapping map ) {
		boolean anyBound = false;
		if ((S instanceof Free)&& map.hasBound(((Free)S).var())) {
				S = new Bound (map.indexOf(((Free)S).var()));
				anyBound = true;
				freeVarCnt--;
		}
		if ((P instanceof Free)&& map.hasBound(((Free)P).var())) {
				S = new Bound (map.indexOf(((Free)P).var()));
				anyBound = true;
				freeVarCnt--;
		}
		if ((O instanceof Free)&& map.hasBound(((Free)O).var())) {
				S = new Bound (map.indexOf(((Free)O).var()));
				anyBound = true;
				freeVarCnt--;
		}
		return anyBound;
	}
	
	
	private int boundCost = 0;
	private int unboundCost = 1;
	private int unboundPredFactor = 4;

	private int elementCost(Element x) {
		if ( (x instanceof Fixed) || (x instanceof Bound) )
			return boundCost;
		else
			return unboundCost;

	}

	/*
	 * compute the "estimated cost" to evaluate the pattern. in fact,
	 * it is just a relative ranking that favors patterns with bound
	 * nodes (FIXED or bound variables) over unbound nodes (unbound
	 * variables and ANY). also, patterns with FIXED predicates that
	 * are ranked lower (lower cost) than patterns with variable or
	 * ANY predicates (because fastpath currently does not support
	 * such patterns).
	 * @return int The estimated cost in the range [costmin,costMax).
	 */
	 
	private int costCalc() {
		int c = elementCost(S);
		c += ((elementCost(P) * unboundPredFactor));
		c += elementCost(O);
		return c;
	}

}