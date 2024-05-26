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

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.Iso;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/** Property functions (or any OpBGP replacement)
 *  Execution will be per-engine specific */
public class OpPropFunc extends Op1
{
    // c.f. OpProcedure which is similar except for the handling of arguments.
    // Safer to have two (Ops are mainly abstract syntax, not executional).
    private Node uri;
    private PropFuncArg subjectArgs;
    private PropFuncArg objectArgs;

    public OpPropFunc(Node uri, PropFuncArg args1 , PropFuncArg args2, Op op)
    {
        super(op);
        this.uri = uri;
        this.subjectArgs = args1;
        this.objectArgs = args2;
    }
    
    public PropFuncArg getSubjectArgs() {
        return subjectArgs;
    } 
    
    public PropFuncArg getObjectArgs() {
        return objectArgs;
    } 
    
    @Override
    public Op apply(Transform transform, Op subOp) {
        return transform.transform(this, subOp);
    }

    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this); }

    public Node getProperty() { return uri; }
    
    @Override
    public String getName() {
        return Tags.tagPropFunc;
    }

    @Override
    public Op1 copy(Op op) {
        return new OpPropFunc(uri, subjectArgs, objectArgs, op);
    }

    @Override
    public int hashCode() {
        return uri.hashCode() ^ getSubOp().hashCode();
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( ! ( other instanceof OpPropFunc ) ) 
            return false;
        OpPropFunc procFunc = (OpPropFunc)other;
        if ( ! this.getProperty().equals(procFunc.getProperty()) )
            return false;
        
        PropFuncArg s1 = getSubjectArgs();
        PropFuncArg s2 = procFunc.getSubjectArgs();
        s1.equals(s2);
        
        if ( ! isomorphic(getSubjectArgs(), procFunc.getSubjectArgs(), labelMap) )
            return false;
        if ( ! isomorphic(getObjectArgs(), procFunc.getObjectArgs(), labelMap) )
            return false;
        return getSubOp().equalTo(procFunc.getSubOp(), labelMap);
    }

    private static boolean isomorphic(PropFuncArg pfa1, PropFuncArg pfa2, NodeIsomorphismMap labelMap) {
        if ( pfa1 == null && pfa2 == null )
            return true;
        if ( pfa1 == null ) return false;
        if ( pfa2 == null ) return false;
        
        if ( pfa1.isList() && pfa2.isList() ) {
            List<Node> list1 = pfa1.getArgList();
            List<Node> list2 = pfa2.getArgList();
            return Iso.isomorphicNodes(list1, list2, labelMap);
        }
        if ( pfa1.isNode() && pfa2.isNode() )
            return Iso.nodeIso(pfa1.getArg(), pfa2.getArg(), labelMap);
        return false;
    }
}
