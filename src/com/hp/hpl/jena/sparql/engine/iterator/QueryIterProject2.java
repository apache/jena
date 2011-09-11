/*
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
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

        public Binding convert(Binding bind)
        {
            Binding b = new BindingProject(projectionVars, bind, outerBinding) ;
            return b ;
        }
        
    }

}

/*
 * (c) Copyright 2010 Talis Systems Ltd
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