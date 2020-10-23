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

package org.apache.jena.sparql.engine.ref;

import org.apache.jena.query.Query ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.algebra.optimize.TransformPropertyFunction ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.* ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.util.Context ;

/** "Reference" query engine - this simply executes the algebra expression as-is
 *  using a simple (non-scalable) execution strategy that follows the definition
 *  of SPARQL as closely as possible. The reference query engine does provide the
 *  algebra extensions. 
 */
public class QueryEngineRef extends QueryEngineBase
{
    public QueryEngineRef(Op op, DatasetGraph dataset, Context context)
    { this(op, dataset, BindingFactory.root(), context) ; }
    
    public QueryEngineRef(Op op, DatasetGraph dataset, Binding input, Context context)
    { super(op, dataset, input, context) ; }

    protected QueryEngineRef(Query query, DatasetGraph dataset,
                             Binding input, Context context)
    {
        super(query, dataset, input, context) ;
    }
    
    @Override
    protected Op modifyOp(Op op)
    {
        // Just property functions
        Transform t1 = new TransformPropertyFunction(context) ;
        op = Transformer.transform(t1, op) ;
        // Nested extends. Needed for bnode01.rq
//        Transform t2 = new TransformExtendCombine();
//        op = Transformer.transform(t2, op) ;
        return op ;
    }
    
    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context)
    {
        if ( binding.vars().hasNext() )
            op = Substitute.substitute(op, binding) ;

        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, QC.getFactory(context)) ;
        Evaluator eval = EvaluatorFactory.create(execCxt) ;
        Table table = Eval.eval(eval, op) ;
        QueryIterator qIter = table.iterator(execCxt) ;
        return QueryIteratorCheck.check(qIter, execCxt) ;
    }
    
    static public QueryEngineFactory getFactory()   { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }
    
    private static QueryEngineFactory factory = new QueryEngineFactory()
    {
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineRef engine = new QueryEngineRef(query, dataset, binding, context) ;
            return engine.getPlan() ;
        }
        
        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineRef engine = new QueryEngineRef(op, dataset, binding, context) ;
            return engine.getPlan() ;
        }

    } ;
}
