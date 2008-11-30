/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.engine;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.util.Context;

/** Example skeleton for a query engine.
 *  To just enxtend ARQ by custom basic graph pattern matching (a very common case)
 *  see the arq.examples.extend.bgp package 
 * 
 * @author Andy Seaborne
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

    public QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context)
    {
        // To extend: rewrite op with a Transform
        
        Transform transform = new MyTransform() ;
        op = Transformer.transform(transform, op) ;
        
        return super.eval(op, dsg, binding, context) ;
    }
    
    // ---- Registration of the factory for this query engine class. 
    
    // Query engine factory.
    // Call MyQueryEngine.register() to add to the global query engine registry. 

    static QueryEngineFactory factory = new MyQueryEngineFactory() ;

    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }
    
}

class MyTransform extends TransformCopy
{
    // Example, do nothing tranform. 
    public Op transform(OpBGP opBGP)                { return opBGP ; }
}

class MyQueryEngineFactory implements QueryEngineFactory
{
    // Accept any dataset for query execution 
    public boolean accept(Query query, DatasetGraph dataset, Context context) 
    { return true ; }

    public Plan create(Query query, DatasetGraph dataset, Binding initial, Context context)
    {
        // Create a query engine instance.
        MyQueryEngine engine = new MyQueryEngine(query, dataset, initial, context) ;
        return engine.getPlan() ;
    }

    public boolean accept(Op op, DatasetGraph dataset, Context context)
    {   // Refuse to accept algebra expressions directly.
        return false ;
    }

    public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context)
    {   // Shodul notbe called because acceept/Op is false
        throw new ARQInternalErrorException("MyQueryEngine: factory calleddirectly with an algebra expression") ;
    }
} 

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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