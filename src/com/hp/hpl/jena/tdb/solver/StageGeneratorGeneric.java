/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.TERM;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriples;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriplesQH;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.tdb.solver.reorder.PatternTriple;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderFixed;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformationBase;

/** Generic - always works - StageGenerator */
public class StageGeneratorGeneric implements StageGenerator
{
    public static Symbol altMatcher = ARQConstants.allocSymbol("altmatcher") ;

    public QueryIterator execute(BasicPattern pattern, 
                                 QueryIterator input,
                                 ExecutionContext execCxt)
    {
        if ( input == null )
            ALog.fatal(this, "Null input to "+Utils.classShortName(this.getClass())) ;

        Graph graph = execCxt.getActiveGraph() ; 

        // Known Jena graph types.

        if ( graph instanceof GraphMemFaster )      // Newer Graph-in-memory; default graph type in Jena
            return executeInlineStats(pattern, input, execCxt) ;

        if ( graph instanceof GraphRDB )
            return executeQueryHandler(pattern, input, execCxt) ;

        if ( graph instanceof GraphMem )            // Old Graph-in-memory
            return executeInlineStats(pattern, input, execCxt) ;

        // When in doubt ... use the general pass-through to graph query handler matcher.
        // Includes union graphs, InfGraphs and many other unusual kinds. 
        return executeQueryHandler(pattern, input, execCxt) ;
    }

    /** Use the inline, iterator based BGP matcher which works over find() */ 
    private QueryIterator executeInline(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        if ( execCxt.getContext().isTrueOrUndef(altMatcher) )
            return QueryIterBlockTriples.create(input, pattern, execCxt) ;
        else
            return executeQueryHandler(pattern, input, execCxt) ;
    }

    // This is going to get me into trouble ....
    //private static Map<Graph, StatsMatcher>   
    
    /** Use the inline, iterator based BGP matcher after some reorganisation for statistics */ 
    private QueryIterator executeInlineStats(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        Graph graph = execCxt.getActiveGraph() ;
        GraphStatisticsHandler stats = graph.getStatisticsHandler() ;
        
        if ( stats != null )
        {
            System.out.println(pattern) ;
            ReorderStatsHandler reorder = new ReorderStatsHandler(graph, stats) ;
            pattern = reorder.reorder(pattern) ;
            System.out.println(pattern) ;
        }
        return executeInline(pattern, input, execCxt) ;
    }

    /** Use the graph's query handler */ 
    private QueryIterator executeQueryHandler(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        return QueryIterBlockTriplesQH.create(input, pattern, execCxt) ;
    }
    
    static class ReorderStatsHandler extends ReorderTransformationBase
    {
        static ReorderFixed fixed = new ReorderFixed() ;
        
        // Guesses at the selectivity of fixed, but unknown, values.
        // Choose these for large graphs because bad guesses don't harm small graphs.  
        
        final long TERM_S ;
        final long TERM_TYPE ;
        final long TERM_P ;
        final long TERM_O ;
        final long N ;
        
        private GraphStatisticsHandler stats ;

        ReorderStatsHandler(Graph graph, GraphStatisticsHandler stats)
        {
            this.stats = stats ;
            N = graph.size() ;
            // Note: when these are too badly wrong, the app can supply a statistics file. 
            TERM_S = 10 ;       // Wild guess: "An average subject has 10 properties".
            TERM_P = N/10 ;     // Wild guess: "An average vocabulary has 10 properties"
            TERM_O = 20 ;       // Wild guess: "An average object is in 20 triples".
            TERM_TYPE = N/10 ;  // Wild guess: "An average class has in 1/10 of the resources."
        }
        
        @Override
        protected double weight(PatternTriple pt)
        {
            double x = fixed.weight(pt) ;
            // If there are two fixed terms, use the fixed weighting, all of which are quite small.
            // This choose a less optimal triple but the worse choice is still a very selective choice.
            // One case is IFPs: the multi term choice for PO is not 1. 
            if ( x < ReorderFixed.MultiTermMax )
            {
                // Rescale it from the fixed numbers of  ReorderFixed
                //x = ReorderFixed.MultiTermSampleSize * x / N ;
            }
            else
                x = weight1(pt) ;
            
            System.out.printf("** %s: --> %s\n", pt, x) ;
            return x ;
        }
        
        
        private double weight1(PatternTriple pt)
        {
            // One or zero fixed terms.
            
            long S = -1 ;
            long P = -1 ;
            long O = -1 ;
            
            // Include guesses for SP, OP, typeClass
            
            if ( pt.subject.isNode() )
                S = stats.getStatistic(pt.subject.getNode(), Node.ANY, Node.ANY) ;
            else if ( TERM.equals(pt.subject) )
                S = TERM_S ;
            
            // rdf:type.
            if ( pt.predicate.isNode() )
                P = stats.getStatistic(Node.ANY, pt.predicate.getNode(), Node.ANY) ;
            else if ( TERM.equals(pt.predicate) )
                P = TERM_P ;
            
            if ( pt.object.isNode() )
                O = stats.getStatistic(Node.ANY, Node.ANY, pt.object.getNode()) ;
            else if ( TERM.equals(pt.object) )
                O = TERM_O ;

            if ( S == 0 || P == 0 || O == 0 )
                // Can't match.
                return 0 ;
            
            // Find min positive
            double x = -1 ;
            if ( S > 0 ) x = S ;
            if ( P > 0 && P < x ) x = P ;
            if ( O > 0 && O < x ) x = O ;
            System.out.printf("** [%d, %d, %d]\n", S, P ,O) ;

            return x ;
        }
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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