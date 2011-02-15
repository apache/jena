/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimoprhics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.query.BindingQueryPlan ;
import com.hp.hpl.jena.graph.query.Domain ;
import com.hp.hpl.jena.graph.query.QueryHandler ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;


/** An Iterator that takes a binding and executes 
 * a pattern via the Jena graph QueryHandler interface.
 */
 
public class QueryIterBlockTriplesQH extends QueryIterRepeatApply
{
    protected BasicPattern pattern ;

    public static QueryIterator create( QueryIterator input,
                                        BasicPattern pattern , 
                                        ExecutionContext cxt)
    {
        return new QueryIterBlockTriplesQH(input, pattern, cxt) ;
    }
    
    protected QueryIterBlockTriplesQH( QueryIterator input,
                                       BasicPattern pattern , 
                                       ExecutionContext cxt)
    {
        super(input, cxt) ;
        this.pattern = pattern ;
    }

    @Override
    public QueryIterator nextStage(Binding binding)
    {
        return new StagePattern(binding, pattern, getExecContext()) ;
    }

    static class StagePattern extends QueryIter
    {
        ClosableIterator<Domain> graphIter ;
        Binding binding ;
        //DatasetGraph data ;
        Var[] projectionVars ;

        // Could get pattern, constraints and data from parent if this were not static.
        // But non-static inner class that inherit from an external class can
        // be confusing.  Unnecessary complication.
        
        public StagePattern(Binding binding,
                            BasicPattern pattern, 
                            ExecutionContext qCxt)
        {
            super(qCxt) ;
            this.binding = binding ;
            
            QueryHandler qh = qCxt.getActiveGraph().queryHandler() ;
            com.hp.hpl.jena.graph.query.Query graphQuery = new com.hp.hpl.jena.graph.query.Query() ;
            
            //System.out.println("StageBasePattern: "+pattern) ;
            
            Set<Var> vars = new HashSet<Var>() ;
            compilePattern(graphQuery, pattern.getList(), binding, vars) ;
            projectionVars = projectionVars(vars) ; 
            // **** No constraints done here currently
            //QueryEngineUtils.compileConstraints(graphQuery, constraints) ;
            
            // Start our next iterator.
            BindingQueryPlan plan = qh.prepareBindings(graphQuery, projectionVars);
            graphIter = plan.executeBindings() ;
            if ( graphIter == null )
                Log.warn(this, "Graph Iterator is null") ;
        }

        @Override
        protected boolean hasNextBinding()
        {
            boolean isMore = graphIter.hasNext() ;
            return isMore ;
        }

        @Override
        protected Binding moveToNextBinding()
        {
          Domain d = graphIter.next() ;
          Binding b = graphResultsToBinding(binding, d, projectionVars) ;
          return b ;
        }

        @Override
        protected void closeIterator()
        {
            if ( ! isFinished() )
            {
                if ( graphIter != null )
                    graphIter.close() ;
                graphIter = null ;
            }
        }
        
        @Override
        protected void requestCancel() { closeIterator() ; }

    }
    
    private static void compilePattern(com.hp.hpl.jena.graph.query.Query graphQuery,
                                      List<Triple> pattern, Binding presets, Set<Var> vars)
    {
        if ( pattern == null )
            return ;
        for (Iterator<Triple>iter = pattern.listIterator(); iter.hasNext();)
        {
            Triple t = iter.next();
            t = Substitute.substitute(t, presets) ;
            if ( vars != null )
            {
                if ( t.getSubject().isVariable() )
                    vars.add(Var.alloc(t.getSubject())) ;
                if ( t.getPredicate().isVariable() )
                    vars.add(Var.alloc(t.getPredicate())) ;
                if ( t.getObject().isVariable() )
                    vars.add(Var.alloc(t.getObject())) ;
            }
            graphQuery.addMatch(t);
        }
    }
    
    private static void compileConstraints(com.hp.hpl.jena.graph.query.Query graphQuery, List<?> constraints)
    {
        Log.warn(QueryIterBlockTriplesQH.class, "Call to compileConstraints for Jena Expressions") ;
    }
    
    public static Var[] projectionVars(Set<Var> vars)
    {
        Var[] result = new Var[vars.size()] ;
    
        int i = 0 ; 
        for ( Iterator<Var> iter = vars.iterator() ; iter.hasNext() ; )
        {
            Var n = iter.next() ;
            result[i] = n ;
            i++ ;
        }
        return result ;
    }
    
    private static Binding graphResultsToBinding(Binding parent, Domain d, Var[] projectionVars)
    {
        // Copy out
        Binding binding = new BindingMap(parent) ;
        
        for ( int i = 0 ; i < projectionVars.length ; i++ )
        {
            Var var = projectionVars[i] ;
            
            Node n = d.get(i) ;
            if ( n == null )
                // There was no variable of this name.
                continue ;
            binding.add(var, n) ;
        }
        return binding ;
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(Utils.className(this)) ;
        out.println() ;
        out.incIndent() ;
        FmtUtils.formatPattern(out, pattern, sCxt) ;
        out.decIndent() ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimoprhics Ltd.
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
