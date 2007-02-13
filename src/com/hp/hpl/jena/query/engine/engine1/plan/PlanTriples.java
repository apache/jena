/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1.plan;

import java.util.ListIterator;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.db.impl.DBQueryHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.mem.GraphMemBaseQueryHandler;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.core.ARQConstants;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.engine1.PlanElement;
import com.hp.hpl.jena.query.engine.engine1.PlanVisitor;
import com.hp.hpl.jena.query.engine.iterator.QueryIterBlockTripleAlt;
import com.hp.hpl.jena.query.engine.iterator.QueryIterBlockTriples;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.Symbol;
import com.hp.hpl.jena.query.util.Utils;


/**  
 * @author Andy Seaborne
 * @version $Id: PlanTriples.java,v 1.4 2007/02/06 18:20:24 andy_seaborne Exp $
 */

public class PlanTriples extends PlanElement0
{
    public static Symbol altMatcher = ARQConstants.allocSymbol("altmatcher") ;
    
    //private static Log log = LogFactory.getLog(PlanBasePattern.class) ;
    BasicPattern triples = null ;
    
    public static PlanElement make(Context context)
    { return new PlanTriples(context) ; }
        
    // Matcher control
    
    public static void enableAltMatcher()           { enableAltMatcher(true) ; }
    public static void disableAltMatcher()          { enableAltMatcher(false) ; }
    public static void enableAltMatcher(boolean b)  { ARQ.getContext().set(altMatcher, b) ; }

    // Decision process for which matcher to use.
    private static QueryIterator createMatcher(QueryIterator input,
                                               BasicPattern pattern , 
                                               ExecutionContext cxt)
    {
        QueryHandler qh = cxt.getActiveGraph().queryHandler() ;
        
        // Always use the pass-through triple matcher for databases
        if ( qh instanceof DBQueryHandler )
            return QueryIterBlockTriples.create(input, pattern, cxt) ;
        
        // If in-memory and allowing alt matching ...
        if ( qh instanceof GraphMemBaseQueryHandler &&
             cxt.getContext().isTrueOrUndef(PlanTriples.altMatcher) )
        {
            // The alt matcher avoids thread creation - makes a difference when called very heavily.
            return QueryIterBlockTripleAlt.create(input, pattern, cxt) ;
        }
        
        // When in doubt ... use the general pass-through to graph query handler matcher.
        return QueryIterBlockTriples.create(input, pattern, cxt) ;
        
    }
    public PlanTriples(Context context)
    {
        super(context) ;
        triples = new BasicPattern() ;
    }
    
    public void addTriple(Triple t) { triples.add(t) ; }
    
    public ListIterator triples() { return triples.iterator() ; }
    public BasicPattern getPattern() { return triples ; }
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        if ( input == null )
            LogFactory.getLog(this.getClass()).fatal("Null input to "+Utils.classShortName(this.getClass())) ;
        QueryIterator cIter = createMatcher(input, triples, execCxt) ;
        return cIter ;
    }

    public void visit(PlanVisitor visitor) { visitor.visit(this) ; }

    public PlanElement apply(Transform transform)  { return transform.transform(this) ; }

    public PlanElement copy()
    {
        PlanTriples pbt = new PlanTriples(getContext()) ;
        pbt.triples.addAll(this.triples) ;
        return pbt ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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