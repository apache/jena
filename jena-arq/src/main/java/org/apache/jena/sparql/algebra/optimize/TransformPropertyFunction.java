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

import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.*;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpTriple ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.util.Context ;

/** Rewrite to replace a property function property with the call to the property function implementation */
public class TransformPropertyFunction extends TransformCopy {
    private final Context context;
    private final boolean doingPropertyFunctions;
    private final PropertyFunctionRegistry registry;

    /**
     * Apply the property function transformation.
     * <p>
     * The {@code context} provides the place to find the property function registry.
     * A custom one can be supplied using
     * {@link ARQConstants#registryPropertyFunctions}
     * <p>
     * See {@link PropertyFunctionRegistry#chooseRegistry} for the full decision
     * process.
     * <p>
     * If {@link ARQ#enablePropertyFunctions} is false, then property functions are
     * not enabled and remain as plain triples. For example, this is set false by "strict mode"
     */
    public static Op transform(Op op, Context context) {
        Transform t = new TransformPropertyFunction(context);
        return Transformer.transform(t, op);
    }

    public TransformPropertyFunction(Context context) {
        this.context = context;
        registry = PropertyFunctionRegistry.chooseRegistry(context);
        doingPropertyFunctions = ( registry != null ) && context.isTrueOrUndef(ARQ.enablePropertyFunctions);
    }

    @Override
    public Op transform(OpTriple opTriple) {
        if ( !doingPropertyFunctions )
            return opTriple;

        Op x = transform(opTriple.asBGP());
        if ( !(x instanceof OpBGP) )
            return x;

        if ( opTriple.equivalent((OpBGP)x) )
            return opTriple;
        return x;
    }

    @Override
    public Op transform(OpBGP opBGP) {
        if ( !doingPropertyFunctions )
            return opBGP;
        return PropertyFunctionGenerator.buildPropertyFunctions(registry, opBGP, context);
    }

    // Property function processing is done before quad conversion
    // To change that, implement:

//    @Override
//    public Op transform(OpQuad opQuad) {
//        if ( ! doingPropertyFunctions )
//            return super.transform(opQuad);
//        ...
//
//    }
//
//    @Override
//    public Op transform(OpQuadPattern opQuadPattern) {
//        if ( ! doingPropertyFunctions )
//            return super.transform(opQuadPattern);
//        ...
//    }
}

