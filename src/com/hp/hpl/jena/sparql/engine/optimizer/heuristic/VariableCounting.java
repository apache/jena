/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.heuristic;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternJoin;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicBasicPattern;

/**
 * This heuristic implements a simple variable counting technique to estimate
 * the execution cost of edges and nodes of graphs (i.e. BasicPatterns).
 * If a node of a triple pattern is an unbound variable, the cost 1.0 is 
 * assigned to the node. Else, the node is bound and the cost 0.0 is
 * assigned. Futher, S/P/O nodes are weighted. The subject is weighted
 * with 3, the predicate with 1 and the object with 2. The weight considers
 * that subjects are generally more selective than predicates. Thus,
 * the pattern ?s :p ?o should have a heigher cost than the 
 * pattern :s ?p ?o.
 * 
 * This heuristic is executed if more sophisticated techniques cannot be 
 * used because of the lack of indexes.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class VariableCounting extends HeuristicBasicPattern
{	
	private static Log log = LogFactory.getLog(VariableCounting.class) ;
	
	/**
	 * This method returns the cost for the triple based
	 * on the number and type of variables defined in the triple.
	 * 
	 * @param triple1
	 * @return Double
	 */
	public double getCost(Triple triple1) 
	{
		double cost = 1d ;
		int MAX_COST = 8 ;
		
		if (triple1.getSubject().isVariable())
			cost += 4 ;
		
		if (triple1.getPredicate().isVariable())
			cost += 1 ;
		
		if (triple1.getObject().isVariable())
			cost += 2 ;
		
		return cost / MAX_COST ;
	}
	
	/**
	 * The method returns the cost for the join between
	 * two triples for this heuristic, since there is no 
	 * joined triple pattern concept for variable counting.
	 * The more joins a two triple pattern define, the less
	 * ist the execution cost, since they are more selective.
	 * Further a join over SO or OS is for instance less
	 * selective as a join over SS. Exotic joins like
	 * SP, PS, PP, PO, OP are considered high selective.
	 * This is probably very domain dependent, as most
	 * domains rarely define such exotic join types. 
	 * Moreover, unbound joins are calculated to be 
	 * less selective than bound joins, e.g. for
	 * (1) ?s1 ?p1 ?o1 . ?s1 ?p2 ?o2
	 * (2) :s1 ?p1 ?o1 . :s1 ?p2 ?o2
	 * (2) is more selective. This method may be fine tuned
	 * for specific domains.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return Double
	 */
	public double getCost(Triple triple1, Triple triple2)
	{
		if (! BasicPatternJoin.isJoined(triple1, triple2))
		{
			log.error("The triples are not joined, no cost estimation allowed (Double.MAX_VALUE returned): " + triple1 + " " + triple2) ;
			
			return Double.MAX_VALUE ;
		}
	
		double cost = 32d ;
		int MAX_COST = 32 ;
		List joins = BasicPatternJoin.specificTypes(triple1, triple2) ; // List<String>
		
		for (Iterator iter = joins.iterator(); iter.hasNext(); )
		{
			String type = (String)iter.next() ;
			
			if (type.equals(BasicPatternJoin.uSS))
				cost -= 2 ;
			else if (type.equals(BasicPatternJoin.uSP))
				cost -= 3 ;
			else if (type.equals(BasicPatternJoin.uSO))
				cost -= 1 ;
			else if (type.equals(BasicPatternJoin.uPS))
				cost -= 3 ;
			else if (type.equals(BasicPatternJoin.uPP))
				cost -= 3 ;
			else if (type.equals(BasicPatternJoin.uPO))
				cost -= 3 ;
			else if (type.equals(BasicPatternJoin.uOS))
				cost -= 1 ;
			else if (type.equals(BasicPatternJoin.uOP))
				cost -= 3 ;
			else if (type.equals(BasicPatternJoin.uOO))
				cost -= 1 ;
			else if (type.equals(BasicPatternJoin.bSS))
				cost -= 2 * 2 ;
			else if (type.equals(BasicPatternJoin.bSP))
				cost -= 3 * 2 ;
			else if (type.equals(BasicPatternJoin.bSO))
				cost -= 1 * 2 ;
			else if (type.equals(BasicPatternJoin.bPS))
				cost -= 3 * 2 ;
			else if (type.equals(BasicPatternJoin.bPP))
				cost -= 0 ;
			else if (type.equals(BasicPatternJoin.bPO))
				cost -= 3 * 2 ;
			else if (type.equals(BasicPatternJoin.bOS))
				cost -= 1 * 2 ;
			else if (type.equals(BasicPatternJoin.bOP))
				cost -= 3 * 2 ;
			else if (type.equals(BasicPatternJoin.bOO))
				cost -= 1 * 2 ;
		}
		
		return cost / MAX_COST * getCost(triple1) * getCost(triple2) ;
	}
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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