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

package org.apache.jena.shacl.engine.constraint;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.vocabulary.SHACL;

/** sh:minCount */
public class MinCount extends CardinalityConstraint {

    public MinCount(int minCardinality) {
        super(minCardinality, -1);
    }

    public int getMinCount() {
        return super.minCount;
    }
    
    @Override
    public Node getComponent() {
        return SHACL.MinCountConstraintComponent;
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    // Special syntax. Handled in ShapeOutputVisitor property shape. Ignore here.
    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        throw new InternalErrorException("Call to MinCount/compact syntax");
    }
    
    @Override
    public String toString() {
        return String.format("minCount[%s]", strInt(minCount));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( !super.equals(obj) )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        return true;
    }
}
