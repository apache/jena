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

import static org.apache.jena.shacl.compact.writer.CompactOut.compactUnquotedString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.path.Path;

/** sh:uniqueLang
 *
 * It is different because one validation call can generate multiple results.
 */
public class UniqueLangConstraint implements Constraint {

    private final boolean flag;
    public UniqueLangConstraint(boolean flag) {
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }

    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        // Node shape -> not allowed
        // XXX Make parse error.
        throw new ShaclException("sh:uniqueLang with no path");
    }

    @Override
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> pathNodes) {
        if ( ! flag )
            return;
        Set<String> results = new HashSet<>();
        Set<String> seen = new HashSet<>();
        for ( Node obj : pathNodes) {
            if ( Util.isLangString(obj) ) {
                String tag = obj.getLiteralLanguage().toLowerCase();
                // Valid?
                //LangTag.check(tag);
                if ( seen.contains(tag) && ! results.contains(tag)) {
                    String msg = toString()+" Duplicate langtag: "+obj.getLiteralLanguage();
                    vCxt.reportEntry(msg, shape, focusNode, path, null, this);
                    results.add(tag);
                }
                seen.add(tag);
            }
        }
    }

    @Override
    public Node getComponent() {
        return SHACL.UniqueLangConstraintComponent;
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compactUnquotedString(out, "uniqueLang", Boolean.toString(flag));
    }

    @Override
    public String toString() {
        return "UniqueLang["+flag+"]";
    }

    @Override
    public int hashCode() {
        return 17*Objects.hash(flag);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof UniqueLangConstraint) )
            return false;
        UniqueLangConstraint other = (UniqueLangConstraint)obj;
        return Objects.equals(flag, other.flag);
    }
}
