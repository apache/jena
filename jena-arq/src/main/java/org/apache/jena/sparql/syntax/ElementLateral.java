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

package org.apache.jena.sparql.syntax;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/** LATERAL */

public class ElementLateral extends Element
{
    private final Element right ;

    public ElementLateral(Element right) {
        this(right, null, false);
    }

    private ElementLateral(Element right, List<Var> vars, boolean seenStar) {
        this.right = right ;
    }

    public Element getLateralElement()  { return right ; }

    @Override
    public int hashCode() {
        int hash = Element.HashLateral ;
        hash = hash ^ getLateralElement().hashCode() ;
        return hash ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
        if ( el2 == null ) return false ;
        if ( ! ( el2 instanceof ElementLateral ) )
            return false ;
        ElementLateral other = (ElementLateral)el2 ;
        return getLateralElement().equalTo(other.getLateralElement(), isoMap);
    }

    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
