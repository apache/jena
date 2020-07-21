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

import static org.apache.jena.shacl.lib.ShLib.displayStr;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.compact.writer.CompactWriter;
import org.apache.jena.shacl.compact.writer.ShaclNotCompactException;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.validation.ValidationProc;
import org.apache.jena.shacl.vocabulary.SHACL;

/** sh:not */
public class ShNot extends ConstraintOp1 {

    public ShNot(Shape other) {
        super(other);
    }

    @Override
    public Node getComponent() {
        return SHACL.NotConstraintComponent;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Graph data, Node node) {
        ValidationContext vCxt2 = ValidationContext.create(vCxt);
        ValidationProc.execValidateShape(vCxt2, data, other, node);
        boolean innerConforms = vCxt2.generateReport().conforms();
        if ( ! innerConforms )
            return null;
        String msg = "Not["+other+"] at focusNode "+displayStr(node);
        return new ReportItem(msg, node);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        // "other" must be a node shape with no property shapes and one constraint.
        Constraint constraint  = CompactWriter.getCompactPrintable(other);
        if ( constraint == null )
            throw new ShaclNotCompactException("sh:not("+other+")");
        out.print("! ");
        constraint.printCompact(out, nodeFmt);
    }

    @Override
    public String toString() {
        return "Not";
    }
}
