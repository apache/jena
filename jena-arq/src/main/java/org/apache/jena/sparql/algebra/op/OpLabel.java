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

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/** Do-nothing class that means that tags/labels/comments can be left in the algebra tree.
 * If serialized, toString called on the object, reparsing yields a string.
 *  Can have zero one sub ops. */

public class OpLabel extends Op1
{
    // Beware : while this is a Op1, it might have no sub operation.
    // (label "foo") and (label "foo" (other ...)) are legal.
    
    // Better: string+(object for internal use only)+op?
    public static Op create(Object label, Op op) { return new OpLabel(label, op); }
    
    private Object object;

    protected OpLabel(Object thing) { this(thing, null); }
    
    protected OpLabel(Object thing, Op op) {
        super(op);
        this.object = thing;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( !(other instanceof OpLabel) )
            return false;
        OpLabel opLabel = (OpLabel)other;
        if ( !Objects.equals(object, opLabel.object) )
            return false;

        return Objects.equals(getSubOp(), opLabel.getSubOp());
    }

    @Override
    public int hashCode() {
        int x = HashLabel;
        x ^= Lib.hashCodeObject(object, 0);
        x ^= Lib.hashCodeObject(getSubOp(), 0);
        return x;
    }

    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this); }

    public Object getObject() { return object; } 
    
    public boolean hasSubOp() { return getSubOp() != null; } 
    
    @Override
    public String getName() {
        return Tags.tagLabel;
    }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp); }

    @Override
    public Op1 copy(Op subOp) {
        return new OpLabel(object, subOp);
    }
}
