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

package org.apache.jena.shex;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.expressions.ShapeExpression;
import org.apache.jena.shex.sys.SysShex;
import org.apache.jena.shex.sys.ValidationContext;

/** A labelled ShEx shape. */
public class ShexShape {
    private final Node label;
    private ShapeExpression shExpression;

    // [shex] Future : builder.
    public ShexShape(Node label, ShapeExpression shExpression) {
        this.label = label;
        this.shExpression = shExpression;
    }

    public Node getLabel() {
        return label;
    }

    public ShapeExpression getShapeExpression() {
        return shExpression;
    }

    public boolean satisfies(ValidationContext vCxt, Node data) {
        vCxt.startValidate(this, data);
        try {
            return shExpression.satisfies(vCxt, data) &&
                    shExpression.testShapeExprSemanticActions(vCxt, data);
        } finally {
            vCxt.finishValidate(this, data);
        }
    }

    public void print(IndentedWriter iOut, NodeFormatter nFmt) {
        iOut.printf("Shape: ");
        if ( SysShex.startNode.equals(getLabel()) )
            iOut.print("START");
        else
            nFmt.format(iOut, getLabel());
        iOut.println();
        iOut.incIndent();
        // ShapeExpressionAND:
        // Consolidate adjacent TripleConstraints.
        getShapeExpression().print(iOut, nFmt);
        iOut.decIndent();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((shExpression == null) ? 0 : shExpression.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ShexShape other = (ShexShape)obj;
        if ( label == null ) {
            if ( other.label != null )
                return false;
        } else if ( !label.equals(other.label) )
            return false;
        if ( shExpression == null ) {
            if ( other.shExpression != null )
                return false;
        } else if ( !shExpression.equals(other.shExpression) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ShexShape [label="+label+" expr="+shExpression+"]";
    }
}
