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

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.validation.ValidationProc;
import org.apache.jena.shacl.vocabulary.SHACL;

/** sh:xone */
public class ShXone extends ConstraintOpN {

    public ShXone(List<Shape> others) {
        super(others);
    }

    @Override
    public Node getComponent() {
        return SHACL.XoneConstraintComponent;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Graph data, Node node) {
        int c = 0;
        for ( Shape sh : others ) {
            ValidationContext vCxt2 = ValidationContext.create(vCxt);
            ValidationProc.execValidateShape(vCxt2, data, sh, node);
            boolean innerConforms = vCxt2.generateReport().conforms();
            if ( innerConforms ) {
                c++;
                // Choice: count all vs break as soon as error detected
//                if ( c > 1 )
//                    break;
            }
        }
        if ( c == 1 )
            return null;
        String msg = toString()+" has "+c+" conforming shapes at focusNode "+displayStr(node);
        return new ReportItem(msg, node);
    }

    @Override
    public String toString() {
        return "Xone";
    }
}
