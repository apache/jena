/**
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

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingProject ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;


public class QueryIterProject2 extends QueryIterRepeatApply
{
    List<Var> projectionVars ;
    private final OpProject opProject ;
    private final OpExecutor engine ;

    public QueryIterProject2(OpProject opProject, QueryIterator input, OpExecutor engine, ExecutionContext execCxt)
    {
        super(input, execCxt) ;
        this.opProject = opProject ;
        this.engine = engine ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        QueryIterator qIter = engine.executeOp(opProject.getSubOp(), QueryIterSingleton.create(binding, getExecContext())) ;
        
        qIter = new ProjectEnsureBinding(qIter, 
                                         new ProjectEnsureBindingConverter(binding, opProject.getVars()), 
                                         getExecContext()) ;
        // Project - ensure binding vars are visible. 
        return qIter ;
    }
    
    static class ProjectEnsureBinding extends QueryIterConvert
    {

        public ProjectEnsureBinding(QueryIterator iter, Converter c, ExecutionContext context)
        {
            super(iter, c, context) ;
        }
    }
    
    static class ProjectEnsureBindingConverter implements QueryIterConvert.Converter
    {

        private final Binding outerBinding ;
        private final List<Var> projectionVars ;

        public ProjectEnsureBindingConverter(Binding outerBinding, List<Var> vars)
        {
            this.outerBinding = outerBinding ;
            this.projectionVars = vars ;
        }

        @Override
        public Binding convert(Binding bind)
        {
            Binding b = new BindingProject(projectionVars, bind, outerBinding) ;
            return b ;
        }
        
    }

}
