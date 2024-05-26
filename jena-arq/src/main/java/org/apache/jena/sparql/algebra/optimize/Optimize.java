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

package org.apache.jena.sparql.algebra.optimize;

import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.util.Context ;

/** Optimization of algebra expressions.
 * <p>
 *  New optimization processes can be installed via a global change:
 *  <pre>
 *    Optimize.setFactory((cxt)-&gt;new MyOptimizer(cxt)) ;</pre>
 *  or on a per-context basis:
 *  <pre>
 *    Optimize.RewriterFactory f = (cxt)-&gt;new MyOptimizer(cxt) ;
 *    context.set(ARQConstants.sysOptimizerFactory, f) ;</pre>
 */
public class Optimize
{
    /** Factory for the "Do nothing" optimizer. */
    // also known as :: (context) -> { return (op) -> op ; } ;
    public static RewriteFactory noOptimizationFactory = context -> op -> op ; // Right associative.

    /** Factory for the "minimal" optimizer. */
    public static RewriteFactory minimalOptimizationFactory = (context) -> new OptimizerMinimal(context);

    /** Factory for the standard optimization sequnece. */
    public static RewriteFactory stdOptimizationFactory = (context) -> new OptimizerStd(context);

    /**
     * Set this via {@link #setFactory} to a different factory implementation to have a
     * different general optimizer.
     */
    private static RewriteFactory factory = stdOptimizationFactory ;

    public static Op optimize(Op op, ExecutionContext execCxt) {
        return optimize(op, execCxt.getContext()) ;
    }

    /** Optimize based on all options */
    public static Op optimize(Op op, Context context) {
        Rewrite opt = decideOptimizer(context) ;
        return opt.rewrite(op) ;
    }

    /** Set the global optimizer factory to one that does nothing.
     * Applications probably want {@link #basicOptimizer}  */
    public static void noOptimizer() {
        setFactory(noOptimizationFactory) ;
    }

    /** Set the global optimizer factory to one that only does property functions and scoped variables.
     * @see #minimalOptimizationFactory
     */
    public static void basicOptimizer() {
        setFactory(minimalOptimizationFactory) ;
    }

    static private Rewrite decideOptimizer(Context context) {
        RewriteFactory f = (RewriteFactory)context.get(ARQConstants.sysOptimizerFactory) ;
        if ( f == null )
            f = factory ;
        if ( f == null )
            f = stdOptimizationFactory ;    // Only if default 'factory' gets lost.
        return f.create(context) ;
    }

    /** Globally set the factory for making optimizers */
    public static void setFactory(RewriteFactory aFactory)
    { factory = aFactory ; }

    /** Get the global factory for making optimizers */
    public static RewriteFactory getFactory()
    { return factory ; }

    /** Apply a {@link Transform} to an {@link Op} */
    public static Op apply(Transform transform, Op op) {
        Op op2 = Transformer.transformSkipService(transform, op) ;
        if ( op2 != op )
            return op2 ;
        return op ;
    }
}
