/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: DBQueryHandler.java,v 1.1 2003-08-11 02:41:52 wkw Exp $
*/

package com.hp.hpl.jena.db.impl;

/**
    A SimpleQueryHandler is a more-or-less straightforward implementation of QueryHandler
    suitable for use on graphs with no special query engines.
	@author kers
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.BindingQueryPlan;
import com.hp.hpl.jena.graph.query.Bound;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.graph.query.Fixed;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.PatternStage;
import com.hp.hpl.jena.graph.query.Pipe;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.graph.query.SimpleQueryPlan;
import com.hp.hpl.jena.graph.query.Stage;
import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

public class DBQueryHandler extends SimpleQueryHandler {
	/** the Graph this handler is working for */
	private GraphRDB graph;
	boolean queryFullReifStmt;  // if true, ignore partially reified statements
								// this is a join optimization for reification.

	/** make an instance, remember the graph */
	public DBQueryHandler(GraphRDB graph) {
		super(graph);
		this.graph = graph;
		queryFullReifStmt = false;
	}

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

		for (i = 0; i < ptn.length; i++) {
			pat = ptn[i];
			src = new DBPattern (i, pat, varMap);
			pat = ptn[i];
			Iterator it = graph.getSpecializedGraphs();
			while (it.hasNext()) {
				SpecializedGraph sg = (SpecializedGraph) it.next();
				char sub = sg.subsumes(pat);
				if ( sub == SpecializedGraph.noTriplesForPattern )
					continue;
				src.sourceAdd(sg, sub);
				if ( sub == SpecializedGraph.allTriplesForPattern ) {
					break;
				}
			}
			if ( !src.hasSource() )
				throw new RDFRDBException(
					"Pattern is not bound by any specialized graph: " + pat);
			source[i] = src;
		}

		int minCost;
		int cost;
		DBPattern minSrc, unstagedSrc = null;
		while (patternsToDo > 0) {
			// rank the patterns by cost
			minCost = DBPattern.costMax + 1;
			minSrc = null;
			for (i = 0; i < ptn.length; i++) {
				src = source[i];
				if (src.isStaged)
					continue;
				pat = ptn[i];
				cost = src.cost(varMap);
				if (cost < minCost) {
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
				List resVar = new ArrayList(); // list of Node_Variable
				List qryPat = new ArrayList(); // list of DBPattern
				qryPat.add(src);
				src.getFree(resVar);
				for ( i=0; i<ptn.length; i++ ) {
					if ( source[i].isStaged ) continue;
					if ( src.joinsWith(source[i],resVar) ) {
						qryPat.add(source[i]);
						patternsToDo--;
						source[i].getFree(resVar);
						source[i].isStaged = true;
					}
				}
				if ( qryPat.size() > 1 )	
					stages[stageCnt] =
						new DBQueryStage(graph, src.singleSource(), resVar, qryPat,
							varMap, queryFullReifStmt);
				else
					stages[stageCnt] = super.patternStage( varMap, constraints,
											new Triple[] { ptn[src.index] });		
			} else {
				stages[stageCnt] = super.patternStage( varMap, constraints,
										new Triple[] { ptn[src.index] });		
			}
			stageCnt++;
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
