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

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.syntax.Element ;


/** A "function" that executes over a pattern */
 
public abstract class ExprFunctionOp extends ExprFunction
{
    private final Op op ;
    private Op opRun = null ;
    private final Element element ;
    
    protected ExprFunctionOp(String fName, Element el, Op op)
    {
        super(fName) ;
        this.op = op ;
        this.element = el ;
    }

    @Override
    public Expr getArg(int i)
    {
        return null ;
    }
    
    @Override
    public boolean isGraphPattern()     { return true ; }
    @Override
    public Op getGraphPattern()         { return op ; }

    public Element getElement()         { return element ; }
    
    @Override
    public int numArgs() { return 0 ; }
    
    // ---- Evaluation
    
    @Override
    public final NodeValue eval(Binding binding, FunctionEnv env)
    {
        // Substitute?
        // Apply optimize transforms after substitution?
//        if ( opRun == null )
//        {
//            opRun = op ;
//            if ( env.getContext().isTrueOrUndef(ARQ.propertyFunctions) )
//                opRun = Optimize.apply("Property Functions", new TransformPropertyFunction(env.getContext()), opRun) ;
//        }
        
        ExecutionContext execCxt = new ExecutionContext(env.getContext(),
                                                        env.getActiveGraph(),
                                                        env.getDataset(),
                                                        QC.getFactory(env.getContext())
                                                        ) ;
        QueryIterator qIter1 = QueryIterSingleton.create(binding, execCxt) ;
        QueryIterator qIter = QC.execute(op, qIter1, execCxt) ;
        // Wrap with something to check for closed iterators.
        qIter = QueryIteratorCheck.check(qIter, execCxt) ;
        // Call the per-operation functionality.
        NodeValue v = eval(binding, qIter, env) ;
        qIter.close() ;
        return v ;
    }
    
    protected abstract NodeValue eval(Binding binding, QueryIterator iter, FunctionEnv env) ;
    
    public abstract ExprFunctionOp copy(ExprList args, Op x) ;
    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform, ExprList args, Op x) { return transform.transform(this, args, x) ; }
}
