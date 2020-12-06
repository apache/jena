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

import static org.apache.jena.shacl.compact.writer.CompactOut.*;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.expr.RegexJava;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;

/** sh:pattern.
 * 
 * This applies to literals and URI through the use of SPARQL str(). 
 */
public class PatternConstraint extends ConstraintTerm {

    private final Pattern pattern;
    private final String patternString;
    private final String flagsStr;

    public PatternConstraint(String pattern, String flagsStr) {
        this.flagsStr = flagsStr;
        int flags = RegexJava.makeMask(flagsStr);
        if ( flagsStr != null && flagsStr.contains("q") )
            this.patternString = Pattern.quote(pattern);
        else
            this.patternString = pattern;
        this.pattern = Pattern.compile(pattern, flags);
    }

    public String getPattern() {
        return patternString;
    }

    public String getFlagsStr() {
        return flagsStr;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        if ( n.isBlank() ) {
            String msg = toString()+": Blank node: "+ShLib.displayStr(n);
            return new ReportItem(msg, n);
        }
        String str = NodeFunctions.str(n);
        boolean b = pattern.matcher(str).find();
        if ( b )
            return null;
        String msg = toString()+": Does not match: '"+str+"'";
        return new ReportItem(msg, n);
    }

    @Override
    public Node getComponent() {
        return SHACL.PatternConstraintComponent;
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        // XXX flagStr?
        compactQuotedString(out, "pattern", patternString);
        if ( flagsStr != null ) {
            out.print(" ");
            compactQuotedString(out, "flags", flagsStr);
        }
    }

    @Override
    public String toString() {
        if ( flagsStr != null )
            return "Pattern["+patternString+"("+flagsStr+")]";
        return "Pattern["+patternString+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof PatternConstraint) )
            return false;
        PatternConstraint other = (PatternConstraint)obj;
        return Objects.equals(patternString, other.patternString);
    }
}
