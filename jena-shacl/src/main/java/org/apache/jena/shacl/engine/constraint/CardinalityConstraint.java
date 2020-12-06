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

import java.util.Objects;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;

public abstract class CardinalityConstraint extends ConstraintEntity {

    protected final int minCount;
    protected final int maxCount;

    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        // Node shape with cardinality. Can this usefully be checked for in the parser?
        throw new ShaclParseException("Cardinality constraint on a node shape");
    }

    // -1 => no test
    protected CardinalityConstraint(int minCardinality, int maxCardinality) {
        this.minCount = minCardinality;
        this.maxCount = maxCardinality;
    }

    protected static String strInt(int x) {
        return x < 0 ? "_" : Integer.toString(x);
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Set<Node> nodes) {
        int count = nodes.size();
        if ( minCount >= 0 && count < minCount ) {
            String msg = toString()+": Invalid cardinality: expected min "+minCount+": Got count = "+count;
            return new ReportItem(msg);
        }
        if ( maxCount >= 0 && count > maxCount ) {
            String msg = toString()+": Invalid cardinality: expected max "+maxCount+": Got count = "+count;
            return new ReportItem(msg);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxCount, minCount);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( ! this.getClass().equals(obj.getClass()) )
        //if ( !(obj instanceof CardinalityConstraint) )
            return false;
        CardinalityConstraint other = (CardinalityConstraint)obj;
        return maxCount == other.maxCount && minCount == other.minCount;
    }
}
