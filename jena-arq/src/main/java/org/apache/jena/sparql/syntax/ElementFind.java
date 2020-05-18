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

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class ElementFind extends Element {
    private final Var  var;
    private final Triple triple;

    public ElementFind(Var v, Node node) {
        Objects.requireNonNull(node);
        if ( ! node.isNodeTriple() ) {
            throw new ARQException("Not a triple term: "+node); 
        }
        this.var = v;
        this.triple = Node_Triple.triple(node);
    }

    public ElementFind(Var v, Triple triple) {
        this.var = Objects.requireNonNull(v);
        this.triple = Objects.requireNonNull(triple);
    }

    public Var getVar() {
        return var;
    }

    public Triple getTriple() {
        return triple;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
        if ( !(el2 instanceof ElementFind) )
            return false;
        ElementFind f2 = (ElementFind)el2;
        if ( !this.getVar().equals(f2.getVar()) )
            return false;
        if ( !this.getTriple().equals(f2.getTriple()) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return var.hashCode() ^ triple.hashCode();
    }

    @Override
    public void visit(ElementVisitor v) {
        v.visit(this);
    }
}
