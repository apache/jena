/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.engine.*;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.binding.BindingMap;
import com.hp.hpl.jena.query.engine.binding.BindingRoot;
import com.hp.hpl.jena.query.engine.engine1.compiler.QueryPatternCompiler;
import com.hp.hpl.jena.query.engine.engine1.plan.PlanDistinct;
import com.hp.hpl.jena.query.engine.engine1.plan.PlanLimitOffset;
import com.hp.hpl.jena.query.engine.engine1.plan.PlanOrderBy;
import com.hp.hpl.jena.query.engine.engine1.plan.PlanProject;
import com.hp.hpl.jena.query.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.query.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.query.syntax.Element;
import com.hp.hpl.jena.query.util.Context;

/**
 * @author     Andy Seaborne
 * @version    $Id: QueryEngine.java,v 1.95 2007/02/08 16:19:00 andy_seaborne Exp $
 */
 
public class QueryEngine extends QueryEngineBase
{
    private static Log log = LogFactory.getLog(QueryEngine.class) ;
    
    private PlanElement plan = null ;           // The whole query
    private PlanElement planPattern = null ;    // Just the pattern (convenient).
    
    /** Create a QueryEngine.  The preferred mechanism is through QueryEngineFactory */
    
    public QueryEngine(Query q)
    {
        this(q, null) ;
    }

    public QueryEngine(Query q, Context context)
    {
        super(q, context) ;
    }

    // ---- Plan-ness operations
    
    /** Get the Plan for the whole query (building it if it has not already been built)
     * 
     * @return Plan
     */
    public PlanElement getPlanElement() 
    {
        if ( plan == null )
            plan = buildPlan(getModifiers(), query.getQueryPattern()) ;
        return plan ;
    }
    
    /** Get the PlanElement for the start of the query pattern.
     *  Builds the whole plan if necessary.
     * 
     * @return PlanElement
     */
    public PlanElement getPlanPattern() 
    {
        if ( plan == null )
            getPlanElement() ;
        return planPattern ;    
    }
    


    // ---- Interface to QueryEngineBase
    
    protected final
    Plan queryToPlan(Query query, Modifiers modifiers, Element pattern)
    {
        if ( plan == null )
            plan = buildPlan(getModifiers(), query.getQueryPattern()) ;
        
        PlanElement pElt = getPlanElement() ;
        return new Plan1(pElt, this) ;
    }

    // ------------------------------------------------
    // Query Engine extension points.
    
    /** This operator is a hook for other query engines to reuse this framework but
     *  take responsibility for their own query pattern construction. 
     */
    protected PlanElement makePlanForQueryPattern(Context context, Element queryPatternElement)
    {
        // Choose the thing to make a plan of
        // This can be null - no WHERE clause.
        if ( queryPatternElement == null )
            return null ;
        return QueryPatternCompiler.makePlan(context, queryPatternElement) ;
    }
    
    /** Inspect, and possibily modify, the query plan and execution tree.
     * Called after plan creation getPlanForQueryPattern
     * 
     * @param context
     * @param planElt
     * @return PlanElement The plan element for the query pattern - often the PlanElement passed in
     */
    protected PlanElement queryPlanPatternHook(Context context, PlanElement planElt)
    { return planElt ; } 
    
    /** Inspect, and possibily modify, the query plan and execution tree.
     * Called after plan creation getPlanForQueryPattern
     * 
     * @param context
     * @param planElt
     * @return PlanElement  New root of the planning tree (often, the one passed in)
     */
    protected PlanElement queryPlanHook(Context context, PlanElement planElt)
    { return planElt ; } 
    
    // Build plan around the query pattern plan 
    
    private PlanElement buildPlan(Modifiers mods, Element pattern)
    {
        if ( plan != null )
            return plan ;
        
             // Remember the part of the plan that is specifically for the query pattern
        planPattern = makePlanForQueryPattern(getContext(), pattern) ;
        
        // Give subclasses a chance to run
        planPattern = queryPlanPatternHook(getContext(), planPattern) ;
        PlanElement planElt = planPattern ;
    
        // -- Modifiers
        
        // ORDER BY
        if ( mods.orderConditions != null )
            planElt = PlanOrderBy.make(getContext(), planElt, mods.orderConditions) ;
        
        // Project (ORDER may involve an unselected variable)
        // No projection => initial variables are exposed.
        // Needed for CONSTRUCT and initial bindings + SELECT *
        
        if ( mods.projectVars != null && ! query.isQueryResultStar())
        {
            // Don't project for QueryResultStar so initial bindings show through
            // in SELECT *
            if ( mods.projectVars.size() == 0 && query.isSelectType() )
                log.warn("No project variables") ;
            if ( mods.projectVars.size() > 0 ) 
                planElt = PlanProject.make(getContext(), planElt, mods.projectVars) ;
        }
        
        // DISTINCT
        if ( query.isDistinct() || getContext().isTrue(EngineConfig.autoDistinct) )
            planElt = PlanDistinct.make(getContext(), planElt, mods.projectVars) ;
        
        // LIMIT/OFFSET
        if ( query.hasLimit() || query.hasOffset() )
            planElt = PlanLimitOffset.make(getContext(), planElt, mods.start, mods.length) ;
    
        plan = planElt ;
        plan = queryPlanHook(getContext(), plan) ;
        return plan ;
    }

    // Turn a plan for the whole query into a results iterator.
    QueryIterator planToIterator(PlanElement pElt)
    {
        QueryIterator qIter = null ;
        try {
            init() ;
            if ( ! queryExecutionInitialised )
                throw new ARQInternalErrorException("Query execution not initialized") ;

            Binding rootBinding = buildInitialBinding() ;
            QueryIterator initialIter = new QueryIterSingleton(rootBinding, getExecContext()) ;
            
            // Any WHERE clause ?
            if ( pElt == null )
            {
                if ( startBinding != null )
                    return initialIter ;
                else
                    return new QueryIterNullIterator(getExecContext()) ;
            }

            qIter = pElt.build(initialIter, getExecContext()) ;
            return qIter ;
        } catch (RuntimeException ex) {
            if ( qIter != null )
                qIter.close();
            throw ex ;
        }
    }

    private Binding buildInitialBinding()
    {
        Binding rootBinding = makeRootBinding() ;
        
        if ( startBinding == null )
            return rootBinding ;

        Binding b = new BindingMap(rootBinding) ;
        QueryEngineUtils.addToBinding(b, startBinding) ;
        return b ;
    }
    
    private static Binding makeRootBinding()
    {
        Binding rootBinding = BindingRoot.create() ;
//        Calendar cal = new GregorianCalendar() ;
//        String lex = Utils.calendarToXSDDateTimeString(cal) ;
//        Node n = Node.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
//        rootBinding.add(ARQConstants.varCurrentTime, n) ;
        return rootBinding ;
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
