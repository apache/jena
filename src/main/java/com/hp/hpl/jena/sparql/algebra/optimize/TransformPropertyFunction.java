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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.PropertyFunctionGenerator ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Rewrite to replace a property function property with the call to the property function implementation */
public class TransformPropertyFunction extends TransformCopy
{
    private final Context context ;
    private final boolean doingMagicProperties ;
    private final PropertyFunctionRegistry registry ;
    
    public TransformPropertyFunction(Context context)
    {
        this.context = context ;  
        doingMagicProperties = context.isTrue(ARQ.enablePropertyFunctions) ;
        registry = PropertyFunctionRegistry.chooseRegistry(context) ;
    }
    
    @Override
    public Op transform(OpTriple opTriple)
    {
        if ( ! doingMagicProperties )
            return opTriple ;
        
        Op x =  transform(opTriple.asBGP()) ;
        if ( ! ( x instanceof OpBGP ) )
            return x ;

        if ( opTriple.equivalent((OpBGP)x) )
            return opTriple ;
        return x ;
        
    }
    
    @Override
    public Op transform(OpBGP opBGP)
    {
        if ( ! doingMagicProperties )
            return opBGP ;
        
        return PropertyFunctionGenerator.buildPropertyFunctions(registry, opBGP, context) ;
    }
    
    // Normally, property functionprocessing is done before quad conversion
    // we could convert back to OpGraph and so handle quads 
    
    // For the moment, leave in old mode.
    
//    @Override
//    public Op transform(OpQuad opQuad)
//    {
//        if ( ! doingMagicProperties )
//            return super.transform(opQuad) ; ;
//        check(opQuad.getQuad().getPredicate()) ;
//        return super.transform(opQuad) ;
//    }
//    
//    private void check(Node p)
//    {
//        if ( p.isURI() )
//        {
//            if ( registry.manages(p.getURI()) )
//                Log.warn(this,  "Property function in quad: "+p) ;
//        }
//    }
//    
//    @Override
//    public Op transform(OpQuadPattern opQuadPattern)
//    {
//        if ( ! doingMagicProperties )
//            return super.transform(opQuadPattern) ; ;
//        
//        for ( Triple t : opQuadPattern.getBasicPattern().getList() )
//            check(t.getPredicate()) ;
//        
//        return super.transform(opQuadPattern) ;
//    }

}

