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

package com.hp.hpl.jena.sparql.engine.iterator ;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.* ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;

/**
 * Execute a projection in the middle of an execution. This requires the outcome
 * of each stage of execution (substitution evaluation assumed) to be merged with
 * the input binding. Bindings should already be compatible, otherwise this code
 * should not be used.
 */
public class QueryIterProjectMerge extends QueryIterRepeatApply {
    private final OpProject  opProject ;
    private final OpExecutor engine ;

    public QueryIterProjectMerge(OpProject opProject, QueryIterator input, OpExecutor engine, ExecutionContext execCxt) {
        super(input, execCxt) ;
        this.opProject = opProject ;
        this.engine = engine ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding) {
        QueryIterator qIter = engine.executeOp(opProject.getSubOp(),
                                               QueryIterSingleton.create(binding, getExecContext())) ;
        qIter = new QueryIterConvert(qIter,
                                     new ProjectEnsureBindingConverter(binding, opProject.getVars()),
                                     getExecContext()) ;
        return qIter ;
    }

    /** Ensure binding from outer binding are present when projecting */  
    static class ProjectEnsureBindingConverter implements QueryIterConvert.Converter {

        private final Binding   outerBinding ;
        private final List<Var> projectionVars ;

        public ProjectEnsureBindingConverter(Binding outerBinding, List<Var> vars) {
            this.outerBinding = outerBinding ;
            this.projectionVars = vars ;
        }

        @Override
        public Binding convert(Binding bind) {
            return ensure(projectionVars, outerBinding, bind) ;
            // Effectively, this is:
            //bind = new BindingProject(projectionVars, bind) ;
            // return BindingUtils.merge(outerBinding, bind) ;
        }
        
        /** Merge two bindings, the outer and inner, projecting the inner with the give variables.
         *  This is what happens in substitutIon execution, with a inner select-project.  
         */
        private static Binding ensure(List<Var> vars, Binding outer, Binding inner) {
            // A specialised BindingUtils.merge that does project as well.
            // Reduce small object churn.
            BindingMap b2 = BindingFactory.create(outer) ;
            Iterator<Var> vIter = (vars != null) ? vars.iterator() : inner.vars() ;
            // Add any variables from the RHS
            for ( ; vIter.hasNext() ; ) {
                Var v = vIter.next() ;
                Node n2 = inner.get(v) ;
                if ( n2 == null )
                    continue ;
                if ( ! b2.contains(v) )
                    b2.add(v, inner.get(v)) ;
                else {
                    // Checking!
                    Node n1 = outer.get(v) ;
                    if ( ! n1.equals(n2) )
                        Log.warn(BindingUtils.class,  "merge: Mismatch : "+n1+" != "+n2);
                }
            }
            return b2 ;
        }
    }
}
