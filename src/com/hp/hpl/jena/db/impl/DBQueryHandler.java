/*
  (c) Copyright 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: DBQueryHandler.java,v 1.21 2006-11-29 09:50:15 chris-dollin Exp $
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
	private boolean doImplicitJoin;  // if true, optimize (pushdown) implicit joins
	// e.g., "(ur1 pred1 ?v1) (uri1 pred2 ?v2)" is an implicit join on uri1

	/** make an instance, remember the graph */
	public DBQueryHandler( GraphRDB graph )
        {
        super( graph );
        this.graph = graph;
        if (graph.reificationBehavior() == GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING)
            {
            queryFullReif = queryOnlyReif = queryOnlyStmt = false;
            }
        else
            {
            queryFullReif = queryOnlyReif = false;
            queryOnlyStmt = true;
            }
        doFastpath = true;
        }

	public void setDoFastpath ( boolean val ) 
        { doFastpath = val; }
    
	public boolean getDoFastpath () 
        { return doFastpath; }
	
    public void setDoImplicitJoin ( boolean val ) 
        { doImplicitJoin = val; }

    /**
        Answer a Stage that covers the given triple patterns. This may be a
        default stage if pastpath is disabled, or a composite database stage
        if fastpath is enabled. If we're lucky there will be a single database
        stage if the entire triple pattern can be translated into a single SQL
        statement.
    */
	public Stage patternStage( Mapping varMap, ExpressionSet constraints, Triple[] givenTriples ) 
        {
        return avoidFastpath( constraints, givenTriples )
            ? super.patternStage( varMap, constraints, givenTriples )
            : patternStageWithFullpath( varMap, constraints, givenTriples )
            ;
        }

    /**
     	Answer true iff we should avoid the fastpath code and instead fall back
        on the default implementation in SimpleQueryHandler. Cases:
        <ul>
            <li>doFastPath is false [obviously]
            <li>givenTriples is empty [establishes a useful invariant]
            <li>there's just one givenTriple and the constrains are not Complex
        </ul>
    */
    private boolean avoidFastpath( ExpressionSet constraints, Triple[] givenTriples )
        {
        return 
            doFastpath == false
            || givenTriples.length == 0
            || (givenTriples.length == 1 && !constraints.isComplex())
            ;
        }

    /**
        <code>givenTriples</code> is not empty.
    */
    private Stage patternStageWithFullpath( Mapping varMap, ExpressionSet constraints, Triple[] givenTriples )
        {
        int stageCount = 0;
        int i;
        final Stage[] stages = new Stage[givenTriples.length];
        List patternsToDo = new ArrayList();
        for (i = 0; i < givenTriples.length; i++) patternsToDo.add( new Integer( i ) );
        DBPattern[] source = createDBPatterns( varMap, givenTriples );
    //
        while (patternsToDo.size() > 0)
            {
            DBPattern src = findCheapPattern( varMap, patternsToDo, source );

            // now we have a pattern for the next stage.
            List varList = new ArrayList(); // list of VarDesc
            ExpressionSet evalCons = new ExpressionSet(); // constraints
                                                            // to eval
            List queryPatterns = new ArrayList(); // list of DBPattern
            queryPatterns.add( src );
            boolean doQuery = false;
            // fastpath is only supported for patterns over one table.
            if (src.isSingleSource())
                {
                boolean didJoin = false;
                // see if other patterns can join with it.
                src.addFreeVars( varList );
                boolean foundJoin;
                do
                    {
                    foundJoin = false;
                    for (i = 0; i < patternsToDo.size(); i++)
                        {
                        DBPattern unstaged = source[((Integer) patternsToDo.get( i )).intValue()];
                        if (unstaged.joinsWith( src, varList, queryOnlyStmt, queryOnlyReif, doImplicitJoin ))
                            {
                            queryPatterns.add( unstaged );
                            patternsToDo.remove( i );
                            unstaged.addFreeVars( varList );
                            unstaged.isStaged = true;
                            foundJoin = didJoin = true;
                            }
                        }
                    }
                while (foundJoin && patternsToDo.size() > 0);
                // push down query if (1) there is a join OR if
                // (2) there is no join but there is a constraint to 
                // eval on a single pattern.
                // see if any constraints can be pushed down
                if (didJoin) 
                    doQuery = true;
                else
                    {
                    for (i = 0; i < varList.size(); i++)
                        {
                        VarDesc vx = (VarDesc) varList.get( i );
                        // see if any constraints on a result var.
                        // if so, push down constraint.
                        /*/ UNCOMMENT THE LINES BELOW TO ENABLE CONSTRAINT EVALUATION WITHIN THE DB. */
                        if ((vx.isArgVar == false) && findConstraints( constraints, evalCons, vx )) 
                            doQuery = true;
                        /* UNCOMMENT THE LINES ABOVE TO ENABLE CONSTRAINT EVALUATION WITHIN THE DB. */
                        }
                    }
                if (doQuery)
                    {
                    // add result vars to reslist for query
                    for (i = 0; i < varList.size(); i++)
                        {
                        VarDesc vx = (VarDesc) varList.get( i );
                        if (vx.isArgVar == false) vx.bindToVarMap( varMap );
                        }
                    }

                }
            else if (!src.hasSource()) 
                doQuery = true;
            // hack to handle the case when no graphs match the pattern
            Stage newStage = doQuery
                ? new DBQueryStage( graph,  src.hasSource() ? src.singleSource() : null,  varList, queryPatterns, evalCons )
                : super.patternStage( varMap, constraints, new Triple[] { src.pattern } )
                ;
            stages[stageCount++] = newStage;
            }
        return stageCount == 1 ? stages[0] : new StageSequence( stageCount, stages );
        }

    /**
         find the minimum cost pattern ... but always choose a connected
         pattern over a disconnected pattern (to avoid cross-products).
         There will always be a minimal cost pattern (because <code>sources</code>
         is never empty).
    */
    private DBPattern findCheapPattern( Mapping varMap, List patternsToDo, DBPattern[] sources )
        {
        DBPattern cheapSource = null;
        boolean haveConnectedPattern = false;
        int minCost = DBPattern.costMax, minConnCost = DBPattern.costMax;
        int selectedPatternIndex = -1;
        for (int i = 0; i < patternsToDo.size(); i++)
            {
            DBPattern unstaged = sources[((Integer) patternsToDo.get( i )).intValue()];
            int cost = unstaged.cost( varMap );
            if (unstaged.isConnected)
                {
                if (cost < minConnCost)
                    {
                    cheapSource = unstaged;
                    minConnCost = cost;
                    haveConnectedPattern = true;
                    selectedPatternIndex = i;
                    }
                }
            else if (haveConnectedPattern == false && cost < minCost)
                {
                minCost = cost;
                cheapSource = unstaged;
                selectedPatternIndex = i;
                }
            }
        if (cheapSource == null) 
            throw new JenaException( "impossible: no cheapest pattern among sources" );
        cheapSource.isStaged = true;
        patternsToDo.remove( selectedPatternIndex );
        return cheapSource;
        }

    /**
        Answer an array of database pattern objects, each associated with
        the specialised graphs that might contain the triples it's trying to
        match.
    */
    private DBPattern[] createDBPatterns( Mapping varMap, Triple[] givenTriples )
        {
        DBPattern[] source = new DBPattern[givenTriples.length];
        int reifBehavior = graph.reificationBehavior();
        for (int i = 0; i < givenTriples.length; i++)
            {
            DBPattern src = new DBPattern( givenTriples[i], varMap );
            associateWithSources( src, givenTriples[i], reifBehavior );
            source[i] = src;
            }
        return source;
        }

    /**
        Associate the database pattern <code>src</code> with the sources
        which might contain triples it would match.
    */
    private void associateWithSources( DBPattern src, Triple pat, int reifBehavior )
        {
        Iterator it = graph.getSpecializedGraphs();
        while (it.hasNext())
            {
            SpecializedGraph sg = (SpecializedGraph) it.next();
            char sub = sg.subsumes( pat, reifBehavior );
            if (sub != SpecializedGraph.noTriplesForPattern) src.sourceAdd( sg, sub );
            if (sub == SpecializedGraph.allTriplesForPattern) break;
            }
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
        // System.err.println( ">> setQueryOnlyReified: style of graph is " + graph.getReifier().getStyle() );
		if ( graph.reificationBehavior() != GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING )
			throw new JenaException( "Reified statements cannot be queried for this model's reification style: " + graph.getReifier().getStyle() );
		if ( (opt == true) && (queryOnlyStmt==true) )
			throw new JenaException("QueryOnlyAsserted and QueryOnlyReif cannot both be true");
		queryOnlyReif = opt;
	}

	public boolean getQueryOnlyReified() {
		return queryOnlyReif;
	}

	public void setQueryFullReified ( boolean opt ) {
		if ( graph.reificationBehavior() != GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING )
			throw new JenaException("Reified statements cannot be queried for this model's reification style");
		queryFullReif = opt;
	}

	public boolean getQueryFullReified() {
		return queryFullReif;
	}


	private boolean findConstraints ( ExpressionSet constraints, ExpressionSet evalCons, VarDesc vx ) {
		boolean res = false;
		Iterator it = constraints.iterator();
		Expression e;
		while (it.hasNext()) {
			e = (Expression) it.next();
			if (e.isApply() && e.argCount() == 2) {
				Expression l = e.getArg(0);
				if ( l.isVariable() && vx.var.getName().equals(l.getName()) ) {
					String f = e.getFun();
					if ( dbPartiallyHandlesExpression( f ) ) {
						evalCons.add(e);
						// for now, constraints must be reevaluated outside the
						// db engine since the db engine may not fully evaluate
						// the constraint.
						// it.remove();
						res = true;
					}
				}
			}
		}
		return res;			
	}

    /**
        Answer true if the database makes at least a partial attempt to evaluate
        this constraint.
   */
    private boolean dbPartiallyHandlesExpression( String f )
        {
        return f.equals(ExpressionFunctionURIs.J_startsWith) ||
             f.equals(ExpressionFunctionURIs.J_startsWithInsensitive) ||
             f.equals(ExpressionFunctionURIs.J_contains) ||
             f.equals(ExpressionFunctionURIs.J_containsInsensitive) ||
             f.equals(ExpressionFunctionURIs.J_EndsWith) ||
             f.equals(ExpressionFunctionURIs.J_endsWithInsensitive);
        }
    
    protected static final class StageSequence extends Stage
        {
        private final int numStages;
    
        private final Stage[] stages;
    
        protected StageSequence( int numStages, Stage[] stages )
            {
            this.numStages = numStages;
            this.stages = stages;
            }
    
        public Stage connectFrom( Stage s )
            {
            for (int i = 0; i < numStages; i += 1)
                {
                stages[i].connectFrom( s );
                s = stages[i];
                }
            return super.connectFrom( s );
            }
    
        public Pipe deliver( Pipe L )
            { return stages[numStages - 1].deliver( L );  }
        }
}

/*
    (c) Copyright 2002, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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
