/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.ref;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.algebra.AlgebraGenerator;
import com.hp.hpl.jena.query.algebra.Evaluator;
import com.hp.hpl.jena.query.algebra.OpSubstitute;
import com.hp.hpl.jena.query.algebra.Table;
import com.hp.hpl.jena.query.algebra.op.*;
import com.hp.hpl.jena.query.engine.*;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.engine1.EngineConfig;
import com.hp.hpl.jena.query.engine.engine1.QueryEngineUtils;
import com.hp.hpl.jena.query.syntax.Element;
import com.hp.hpl.jena.query.util.Context;

public class QueryEngineRef extends QueryEngineBase
{
    public static boolean verbose = false ;
    private static QueryEngineFactory factory = new QueryEngineFactory()
    {
        public boolean accept(Query query, Dataset dataset) 
        { return true ; }

        public QueryExecution create(Query query, Dataset dataset)
        {
            QueryEngineRef engine = new QueryEngineRef(query) ;
            engine.setDataset(dataset) ;
            return engine ;
        }
    } ;
    
    static public void register()
    {
        QueryEngineRegistry.addFactory(factory) ;
    }
    
    static public void unregister()
    {
        QueryEngineRegistry.removeFactory(factory) ;
    }
    
    public QueryEngineRef(Query q)
    {
        this(q, null) ;
    }

    private Op queryOp = null ;
    private Op patternOp = null ;
    
    public QueryEngineRef(Query q, Context context)
    {
        super(q, context) ;
    }
    
    protected Plan queryToPlan(Query query, Modifiers mods, Element pattern)
    {
        Op op = getOp() ;
        ExecutionContext execCxt = getExecContext() ;
        Evaluator eval = EvaluatorFactory.create(execCxt) ;
        Table table = op.eval(eval) ;
        QueryIterator qIter = table.iterator(execCxt) ;
        return new PlanOp(op, qIter) ;
    }

    protected Op createPatternOp()
    {
        if ( query.getQueryPattern() == null )
            return null ;
        Op op = AlgebraGenerator.compile(query.getQueryPattern(), getContext()) ;
        if ( startBinding != null )
        { 
            // Misses exposing the input bindings.
            Binding b = QueryEngineUtils.asBinding(startBinding) ;
            op = OpSubstitute.substitute(b, op) ;
        }
        return op ;
    }
    
    protected Op createOp()
    { 
        Op op = getPatternOp() ;
        
        Modifiers mods = new Modifiers(query) ;
        // Maybe move into the algebra compiler
        // ORDER BY
        if ( mods.orderConditions != null )
            op = new OpOrder(op, mods.orderConditions) ;
        
        // Project (ORDER may involve an unselected variable)
        // No projection => initial variables are exposed.
        // Needed for CONSTRUCT and initial bindings + SELECT *
        
        if ( mods.projectVars != null && ! query.isQueryResultStar())
        {
            // Don't project for QueryResultStar so initial bindings show through
            // in SELECT *
            if ( mods.projectVars.size() == 0 && query.isSelectType() )
                LogFactory.getLog(this.getClass()).warn("No project variables") ;
            if ( mods.projectVars.size() > 0 ) 
                op = new OpProject(op, mods.projectVars) ;
        }
        
        // DISTINCT
        if ( query.isDistinct() || getContext().isTrue(EngineConfig.autoDistinct) )
            op = new OpDistinct(op, mods.projectVars) ;
        
        // LIMIT/OFFSET
        if ( query.hasLimit() || query.hasOffset() )
            op = new OpSlice(op, mods.start, mods.length) ;
        
        return op ;
    }
    
    /** Public for debugging and inspection - not used for execution  */  
    public Op getOp()
    {
        if ( queryOp == null )
            queryOp = createOp() ; 
        return queryOp ;
    }
    
    public Op getPatternOp()
    {
        if ( patternOp == null )
            patternOp = createPatternOp() ;
        return patternOp ;
    }
}
/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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