/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: DBQueryHandler.java,v 1.6 2003-08-25 02:17:47 wkw Exp $
*/

package com.hp.hpl.jena.db.impl;

/**
    An extension of SimpleQueryHandler for database-graphs.
	@author wkw et al
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.JenaException;

import java.util.*;

public class DBQueryHandler extends SimpleQueryHandler {
	/** the Graph this handler is working for */
	private GraphRDB graph;
	boolean queryOnlyStmt;  // if true, query only asserted stmt (ignore reification)
	boolean queryOnlyReif;  // if true, query only reified stmt (ignore asserted)
	boolean queryFullReif;  // if true, ignore partially reified statements
	private boolean doFastpath;  // if true, enable fastpath optimization

	/** make an instance, remember the graph */
	public DBQueryHandler ( GraphRDB graph ) {
		super(graph);
		this.graph = graph;
		if ( graph.reificationBehavior() == GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING ) {
			queryFullReif = queryOnlyReif = queryOnlyStmt = false;
		} else {
			queryFullReif = queryOnlyReif = false;
			queryOnlyStmt = true;
		}
		doFastpath = true;
	}

	public void setDoFastpath ( boolean val ) { doFastpath = val; }
	public boolean getDoFastpath () { return doFastpath; }

	public Stage patternStage(
		Mapping varMap,
		Graph constraints,
		Triple[] ptn) {
		final Stage[] stages = new Stage[ptn.length];
		int stageCnt = 0;
		final Integer numStages;
		DBPattern[] source = new DBPattern[ptn.length];

		// find the specialized graphs for the patterns 	
		int i;
		Triple pat;
		int patternsToDo = ptn.length;
		DBPattern src;
		int reifBehavior = graph.reificationBehavior();
		
		if ((patternsToDo == 1) || (doFastpath == false)) {
			// fastpath fastpath; assumes it's faster to do a find for single pattern queries		
			for(i=0;i<patternsToDo;i++)
				stages[stageCnt++] =
					super.patternStage( varMap, constraints, new Triple[] { ptn[i] });
		} else {
			for (i = 0; i < ptn.length; i++) {
				pat = ptn[i];
				src = new DBPattern(i, pat, varMap);
				pat = ptn[i];
				Iterator it = graph.getSpecializedGraphs();
				while (it.hasNext()) {
					SpecializedGraph sg = (SpecializedGraph) it.next();
					char sub = sg.subsumes(pat,reifBehavior);
					if (sub == SpecializedGraph.noTriplesForPattern)
						continue;
					src.sourceAdd(sg, sub);
					if (sub == SpecializedGraph.allTriplesForPattern) {
						break;
					}
				}
				if (!src.hasSource())
					throw new RDFRDBException(
						"Pattern is not bound by any specialized graph: "
							+ pat);
				source[i] = src;
			}

			int minCost, minConnCost;
			int cost;
			DBPattern minSrc, unstagedSrc = null;
			boolean isConnected = false;
			// find the minimum cost ... but always choose a connected
			// pattern over a disconnected pattern (to avoid cross-products)
			while (patternsToDo > 0) {
				// rank the patterns by cost
				minCost = minConnCost = DBPattern.costMax;
				isConnected = false;
				minSrc = null;
				for (i = 0; i < ptn.length; i++) {
					src = source[i];
					if (src.isStaged)
						continue;
					pat = ptn[i];
					cost = src.cost(varMap);
					if (isConnected || src.isConnected) {
						if (src.isConnected && (cost < minConnCost)) {
							minSrc = src;
							minConnCost = cost;
							isConnected = true;
						}
					} else if (cost < minCost) {
						minCost = cost;
						minSrc = src;
					} else
						unstagedSrc = src;
				}

				// if disconnected query; take a pattern at random and create a new stage
				src = (minSrc == null) ? unstagedSrc : minSrc;
				src.isStaged = true;
				patternsToDo--;

				// now we have a pattern for the next stage.
				// fastpath is only supported for patterns over one table.
				boolean doQuery = src.isSingleSource();
				if (doQuery) {
					// see if other patterns can join with it.
					List varList = new ArrayList(); // list of VarIndex
					List qryPat = new ArrayList(); // list of DBPattern
					qryPat.add(src);
					src.getVars(varList, varMap);
					boolean didJoin = false;
					do {
						didJoin = false;
						for (i = 0; i < ptn.length; i++) {
							if (source[i].isStaged)
								continue;
							if (source[i].joinsWith(src,varList,queryOnlyStmt,queryOnlyReif)) {
								qryPat.add(source[i]);
								patternsToDo--;
								source[i].getVars(varList, varMap);
								source[i].isStaged = true;
								didJoin = true;
							}
						}
					} while ( didJoin && (patternsToDo > 0));
					if (qryPat.size() > 1) {
						// add result vars of query to varmap
						for(i=0;i<varList.size();i++) {
							VarIndex vx = (VarIndex) varList.get(i);
							if ( vx.isArgVar == false )
								vx.bindToVarMap(varMap);						
						}
						stages[stageCnt] =
							new DBQueryStage(graph,src.singleSource(),varList,qryPat);
					} else
						stages[stageCnt] =
							super.patternStage(varMap,constraints, new Triple[]{ptn[src.index]});
				} else {
					stages[stageCnt] =
						super.patternStage(varMap,constraints,new Triple[]{ptn[src.index]});
				}
				stageCnt++;
			}
		}
		numStages = new Integer(stageCnt);

		return new Stage() {
			public Stage connectFrom(Stage s) {
				for (int i = 0; i < numStages.intValue(); i += 1) {
					stages[i].connectFrom(s);
					s = stages[i];
				}
				return super.connectFrom(s);
			}
			public Pipe deliver(Pipe L) {
				return stages[numStages.intValue() - 1].deliver(L);
			}
		};
	}
	
	// getters/setters for query handler options

	public void setQueryOnlyAsserted ( boolean opt ) {
		if ( (opt == true) && (queryOnlyReif==true) )
			throw new JenaException("QueryOnlyAsserted and QueryOnlyReif cannot both be true");
		queryOnlyStmt = opt;
	}

	public boolean getQueryOnlyAsserted() {
		return queryOnlyStmt;
	}

	public void setQueryOnlyReified ( boolean opt ) {
		if ( graph.reificationBehavior() != GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING )
			throw new JenaException("Reified statements cannot be queried for this model's reification style");
		if ( (opt == true) && (queryOnlyReif==true) )
			throw new JenaException("QueryOnlyAsserted and QueryOnlyReif cannot both be true");
		queryOnlyReif = true;
		throw new JenaException("QueryOnlyReified is not yet supported");
	}

	public boolean getQueryOnlyReified() {
		return queryOnlyReif;
	}

	public void setQueryFullReified ( boolean opt ) {
		if ( graph.reificationBehavior() != GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING )
			throw new JenaException("Reified statements cannot be queried for this model's reification style");
		queryFullReif = true;
	}

	public boolean getQueryFullReified() {
		return queryFullReif;
	}

}

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
