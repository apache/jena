/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;


/** An Iterator that takes a binding and executes 
 * a pattern via the Jena graph QueryHandler interface.
 */
 
// This is only used for RDB - see QueryIterBlockTriples for main BGP solver in ARQ 

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
        protected void requestCancel() { }
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
        BindingMap binding = BindingFactory.create(parent) ;
        
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
