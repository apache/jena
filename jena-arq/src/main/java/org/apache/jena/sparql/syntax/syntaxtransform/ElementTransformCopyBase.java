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

package org.apache.jena.sparql.syntax.syntaxtransform ;

import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.PathBlock ;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.syntax.* ;

/** Create a copy if the Element(s) below has changed.
 * This is a common base class for writing recursive {@link ElementTransform}
 * in conjunction with being applied by {@link ElementTransformer}.
 */
public class ElementTransformCopyBase implements ElementTransform {
    // Note the use of == as object pointer equality.

    protected boolean alwaysCopy = false ;

    public ElementTransformCopyBase() {
        this(false);
    }

    public ElementTransformCopyBase(boolean alwaysCopy) {
        super();
        this.alwaysCopy = alwaysCopy;
    }

    @Override
    public Element transform(ElementTriplesBlock el) {
        if ( alwaysCopy ) {
            BasicPattern before = el.getPattern();
            BasicPattern copy = new BasicPattern();
            before.getList().forEach(copy::add);
            el = new ElementTriplesBlock(copy);
        }
        return el ;
    }

    @Override
    public Element transform(ElementPathBlock el) {
        if ( alwaysCopy ) {
            PathBlock before = el.getPattern();
            PathBlock copy = new PathBlock();
            before.getList().forEach(copy::add);
            el = new ElementPathBlock(copy);
        }
        return el ;
    }

    @Override
    public Triple transform(Triple triple) {
        return triple;
    }

    @Override
    public Quad transform(Quad quad) {
        return quad;
    }

    @Override
    public Element transform(ElementFilter el, Expr expr2) {
        if ( !alwaysCopy && el.getExpr() == expr2 )
            return el ;
        return new ElementFilter(expr2) ;
    }

    @Override
    public Element transform(ElementAssign el, Var v, Expr expr2) {
        if ( !alwaysCopy && el.getVar() == v && el.getExpr() == expr2 )
            return el ;
        return new ElementAssign(v, expr2) ;
    }

    @Override
    public Element transform(ElementBind el, Var v, Expr expr2) {
        if ( !alwaysCopy && el.getVar() == v && el.getExpr() == expr2 )
            return el ;
        return new ElementBind(v, expr2) ;
    }

    @Override
    public Element transform(ElementData el) {
         if( alwaysCopy ) {
             ElementData copy = new ElementData();
             el.getVars().forEach(copy::add);
             el.getTable().rows().forEachRemaining(copy::add);
             el = copy;
         }
        return el ;
    }

    @Override
    public Element transform(ElementUnion el, List<Element> elts) {
        if ( !alwaysCopy && el.getElements() == elts )
            return el ;
        ElementUnion el2 = new ElementUnion() ;
        el2.getElements().addAll(elts) ;
        return el2 ;
    }

    @Override
    public Element transform(ElementOptional el, Element elt1) {
        if ( !alwaysCopy && el.getOptionalElement() == elt1 )
            return el ;
        return new ElementOptional(elt1) ;
    }

    @Override
    public Element transform(ElementGroup el, List<Element> elts) {
        if ( !alwaysCopy && el.getElements() == elts )
            return el ;
        ElementGroup el2 = new ElementGroup() ;
        el2.getElements().addAll(elts) ;
        return el2 ;
    }

    @Override
    public Element transform(ElementDataset el, Element elt1) {
        if ( !alwaysCopy && el.getElement() == elt1 )
            return el ;
        return new ElementDataset(el.getDataset(), elt1) ;
    }

    @Override
    public Element transform(ElementNamedGraph el, Node gn, Element elt1) {
        if ( !alwaysCopy && el.getGraphNameNode() == gn && el.getElement() == elt1 )
            return el ;
        return new ElementNamedGraph(gn, elt1) ;
    }

    @Override
    public Element transform(ElementExists el, Element elt1) {
        if ( !alwaysCopy && el.getElement() == elt1 )
            return el ;
        return new ElementExists(elt1) ;
    }

    @Override
    public Element transform(ElementNotExists el, Element elt1) {
        if ( !alwaysCopy && el.getElement() == elt1 )
            return el ;
        return new ElementNotExists(elt1) ;
    }

    @Override
    public Element transform(ElementMinus el, Element elt1) {
        if ( !alwaysCopy && el.getMinusElement() == elt1 )
            return el ;
        return new ElementMinus(elt1) ;
    }

    @Override
    public Element transform(ElementService el, Node service, Element elt1) {
        if ( !alwaysCopy && el.getServiceNode() == service && el.getElement() == elt1 )
            return el ;
        return new ElementService(service, elt1, el.getSilent()) ;
    }

    @Override
    public Element transform(ElementSubQuery el, Query query) {
        if ( !alwaysCopy && el.getQuery() == query )
            return el ;
        return new ElementSubQuery(query) ;
    }
}
