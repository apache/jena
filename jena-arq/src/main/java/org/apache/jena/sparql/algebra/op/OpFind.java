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

package org.apache.jena.sparql.algebra.op;

import java.util.Objects;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.Iso;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpFind extends Op0 {

    private final Triple triple;
    private final Var    var;

    public OpFind(Triple triple, Var var) {
        super();
        this.triple = triple;
        this.var = var;
    }

    @Override
    public String getName() {
        return Tags.tagFind;
    }

    @Override
    public Op apply(Transform transform) {
        return transform.transform(this);
    }

    @Override
    public Op0 copy() {
        OpFind op = new OpFind(triple, var);
        return op;
    }

    @Override
    public void visit(OpVisitor opVisitor) {
        opVisitor.visit(this) ;
    }

    public Triple getTriple() {
        return triple; 
    }
    
    public Var getVar() {
        return var; 
    }
    
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( other == null )
            return false;
        if ( this == other )
            return true;
        if ( ! (other instanceof OpFind) ) 
            return false ;
        OpFind opFind = (OpFind)other;
        if ( ! Objects.equals(getVar(), opFind.getVar()) ) 
            return false;
        return Iso.tripleIso(getTriple(), opFind.getTriple(), labelMap) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(triple, var);
    }
}
