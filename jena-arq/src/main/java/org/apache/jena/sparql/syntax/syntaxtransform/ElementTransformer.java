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

package org.apache.jena.sparql.syntax.syntaxtransform;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.syntax.*;

/** A bottom-up application of a transformation of SPARQL syntax Elements. 
 * {@linkplain QueryTransformOps#transform} provides the mechanism
 * to apply to a {@linkplain Query}.
 * @see ElementTransformCopyBase
 * @see UpdateTransformOps#transform
 */
public class ElementTransformer {
    private static ElementTransformer singleton = new ElementTransformer();

    /** Get the current transformer */
    public static ElementTransformer get() {
        return singleton;
    }

    /** Set the current transformer - use with care */
    public static void set(ElementTransformer value) {
        ElementTransformer.singleton = value;
    }

    /** Transform an algebra expression */
    public static Element transform(Element element, ElementTransform transform) {
        return transform(element, transform, null, null, null);
    }

    /** Transformation with specific ElementTransform and ExprTransform */
    public static Element transform(Element element, ElementTransform transform, ExprTransform exprTransform) {
        return get().transformation(element, transform, exprTransform, null, null);
    }

    public static Element transform(Element element, ElementTransform transform, ExprTransform exprTransform,
                                    ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        return get().transformation(element, transform, exprTransform, beforeVisitor, afterVisitor);
    }

    // To allow subclassing this class, we use a singleton pattern
    // and these protected methods.
    protected Element transformation(Element element, ElementTransform transform, ExprTransform exprTransform,
                                     ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        ApplyElementTransformVisitor v = new ApplyElementTransformVisitor(transform, exprTransform);
        return transformation(v, element, beforeVisitor, afterVisitor);
    }

    protected Element transformation(ApplyElementTransformVisitor transformApply, Element element,
                                     ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        if ( element == null ) {
            Log.warn(this, "Attempt to transform a null element - ignored");
            return element;
        }
        return applyTransformation(transformApply, element, beforeVisitor, afterVisitor);
    }

    /** The primitive operation to apply a transformation to an Op */
    protected Element applyTransformation(ApplyElementTransformVisitor transformApply, Element element,
                                          ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        ElementWalker.walk(element, transformApply, beforeVisitor, afterVisitor);
        Element r = transformApply.result();
        return r;
    }

    protected ElementTransformer() {}
}
