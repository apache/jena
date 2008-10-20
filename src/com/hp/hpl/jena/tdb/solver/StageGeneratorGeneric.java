/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Triple;
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

import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.solver.reorder.PatternTriple;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformationBase;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderWeighted;
import com.hp.hpl.jena.tdb.solver.stats.StatsMatcher;

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
            
            // XXX cache these!
            StatsMatcher matcher = new StatsMatcher() ;  
            // Reorder.
            for ( Triple t : NodeLib.tripleList(pattern) )
            {
                PatternTriple pt = new PatternTriple(t) ;
                
//                
//                long x = weight(stats, t) ;
//                System.out.println(x+" :: "+FmtUtils.stringForTriple(t)) ;
//                if ( x == -1 )
//                {
//                    // No estimate
//                }
//                
//                //matcher.addPattern(t) ;
            }
            ReorderTransformation rt = new ReorderWeighted(matcher) ;
            pattern = rt.reorder(pattern) ;
            System.out.println(pattern) ;
        }
        return executeInline(pattern, input, execCxt) ;
    }

//    private static long weight(GraphStatisticsHandler stats, Triple t)
//    {
//        // Make a weight for 
//        // Need to cope with Item.TERM
//        // 
//        
//        long S = stats.getStatistic(t.getSubject(), Node.ANY, Node.ANY) ;
//        long P = stats.getStatistic(Node.ANY, t.getPredicate(), Node.ANY) ;
//        long O = stats.getStatistic(Node.ANY, Node.ANY, t.getObject()) ;
//
//        if ( S == 0 || P == 0 || O == 0 )
//            return 0 ;
//        
//        if ( S > 0 ) ;
//
//        return -1 ;
//    }

    /** Use the graph's query handler */ 
    private QueryIterator executeQueryHandler(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        return QueryIterBlockTriplesQH.create(input, pattern, execCxt) ;
    }
    
    static class ReorderStatsHandler extends ReorderTransformationBase
    {
        
        private GraphStatisticsHandler stats ;

        ReorderStatsHandler(GraphStatisticsHandler stats) { this.stats = stats ; }
        
        @Override
        protected double weight(PatternTriple pt)
        {
            return 0 ;
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