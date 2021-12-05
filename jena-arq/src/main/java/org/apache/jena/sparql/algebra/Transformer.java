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

package org.apache.jena.sparql.algebra;

import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor ;
import org.apache.jena.sparql.algebra.walker.Walker ;
import org.apache.jena.sparql.expr.ExprTransform ;
import org.apache.jena.sparql.expr.ExprTransformCopy ;

/** A bottom-top application of a transformation of SPARQL algebra */
public class Transformer
{

    private static Transformer singleton = new Transformer();

    /** Get the current transformer */
    public static Transformer get() { return singleton; }

    /** Set the current transformer - use with care */
    public static void set(Transformer value) { Transformer.singleton = value; }

    /** Transform an algebra expression */
    public static Op transform(Transform transform, Op op)
    { return get().transformation(transform, op, null, null) ; }

    /** Transform an algebra expression and the expressions */
    public static Op transform(Transform transform, ExprTransform exprTransform, Op op)
    { return get().transformation(transform, exprTransform, op, null, null) ; }

    /**
     * Transformation with specific Transform and default ExprTransform (apply transform
     * inside pattern expressions like NOT EXISTS)
     */
    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        return get().transformation(transform, op, beforeVisitor, afterVisitor) ;
    }

    /** Transformation with specific Transform and ExprTransform applied */
    public static Op transform(Transform transform, ExprTransform exprTransform, Op op,
                               OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        return get().transformation(transform, exprTransform, op, beforeVisitor, afterVisitor) ;
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, Op op) {
        return transformSkipService(transform, null, op, null, null) ;
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, ExprTransform exprTransform, Op op) {
        return transformSkipService(transform, exprTransform, op, null, null) ;
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform opTransform, ExprTransform exprTransform, Op op,
                                          OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        if ( opTransform == null )
            opTransform = new TransformCopy() ;
        if ( exprTransform == null )
            exprTransform = new ExprTransformCopy() ;
        Transform transform2 = new TransformSkipService(opTransform) ;
        transform2 = opTransform ;
        ApplyTransformVisitor atv = new ApplyTransformVisitor(transform2, exprTransform, false, beforeVisitor, afterVisitor) ;
        return Walker.transformSkipService(op, atv, beforeVisitor, afterVisitor) ;
    }

    // To allow subclassing this class, we use a singleton pattern
    // and these protected methods.
    protected Op transformation(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        return transformation(transform, null, op, beforeVisitor, afterVisitor) ;
    }

    protected Op transformation(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        return transformation$(transform, exprTransform, op, beforeVisitor, afterVisitor) ;
    }

    private Op transformation$(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        return Walker.transform(op, transform, exprTransform, beforeVisitor, afterVisitor) ;
    }

    // --------------------------------
    // Safe: ignore transformation of OpService and return the original.
    // Still walks the sub-op of OpService unless combined with a walker that does not go
    // down SERVICE
    static class TransformSkipService extends TransformWrapper
    {
        public TransformSkipService(Transform transform)
        {
            super(transform) ;
        }

        @Override
        public Op transform(OpService opService, Op subOp)
        { return opService ; }
    }
}
