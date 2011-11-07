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

package arq.examples.engine;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Example skeleton for a query engine.
 *  To just extend ARQ by custom basic graph pattern matching (a very common case)
 *  see the arq.examples.bgpmatching package.
 *  To take full control of query execution, use this example for catching the
 *  execution setup and see opexec for a customized OpExecutor for query execution.   
 */  

public class MyQueryEngine extends QueryEngineMain
{
    // Do nothing template for a query engine.  
    
    public MyQueryEngine(Query query, DatasetGraph dataset, Binding initial, Context context)
    {
        super(query, dataset, initial, context) ;
    }

    public MyQueryEngine(Query query, DatasetGraph dataset)
    { 
        // This will default to the global context with no initial settings 
        this(query, dataset, null, null) ;
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding initial, Context context)
    {
        // Extension point: access possible to all the parameters for execution.
        // Be careful to deal with initial bindings.
        Transform transform = new MyTransform() ;
        op = Transformer.transform(transform, op) ;
        return super.eval(op, dsg, initial, context) ;
    }
    
    @Override
    protected Op modifyOp(Op op)
    {
        // Extension point: possible place to alter the algebra expression.
        // Alternative to eval(). 
        op = super.modifyOp(op) ;
        // op = Algebra.toQuadForm(op) ;
        return op ;
    }
    
    // ---- Registration of the factory for this query engine class. 
    
    // Query engine factory.
    // Call MyQueryEngine.register() to add to the global query engine registry. 

    static QueryEngineFactory factory = new MyQueryEngineFactory() ;

    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }


    static class MyTransform extends TransformCopy
    {
        // Example, do nothing tranform. 
        @Override
        public Op transform(OpBGP opBGP)                { return opBGP ; }
    }

    static class MyQueryEngineFactory implements QueryEngineFactory
    {
        // Accept any dataset for query execution 
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return true ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding initial, Context context)
        {
            // Create a query engine instance.
            MyQueryEngine engine = new MyQueryEngine(query, dataset, initial, context) ;
            return engine.getPlan() ;
        }

        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context)
        {   // Refuse to accept algebra expressions directly.
            return false ;
        }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context)
        {   // Shodul notbe called because acceept/Op is false
            throw new ARQInternalErrorException("MyQueryEngine: factory calleddirectly with an algebra expression") ;
        }
    } 

}
