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

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;

/** sh:languageIn */
public class StrLanguageIn extends ConstraintTerm {

    private final List<String> langs;
    
    public StrLanguageIn(List<String> langs) {
        this.langs = langs;
    }

    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        if ( ! n.isLiteral() )
            return new ReportItem(toString()+": Not a literal",n);
        String langTag = n.getLiteralLanguage();
        if ( langTag == null || langTag.isEmpty() ) {
            if ( vCxt.isStrict() ) {
                if ( Util.isSimpleString(n) )
                    return new ReportItem(toString()+": xsd:string (no language tag)",n);
                else
                    return new ReportItem(toString()+": No language tag",n);
            }
            if ( ! Util.isSimpleString(n) )
                return new ReportItem(toString()+": Not an rdf:langString or xsd:string",n);
            // Allow "" to mean xsd:string "none".
            if ( langs.contains(langTag) )
                return null;
            return new ReportItem(toString()+": No matching language tag",n);
        }

        for ( String langPattern : langs ) {
            boolean b = NodeFunctions.langMatches(langTag, langPattern);
            if ( b )
                return null;
        }
        String msg = toString()+": No matching language tag "+langTag;
        return new ReportItem(msg,n);
    }

    @Override
    public Node getComponent() {
        return SHACL.LanguageInConstraintComponent;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        langs.forEach(lang->{
            if ( lang.isEmpty() )
                sj.add("<plain>");
            else
                sj.add(lang);
        });
        
        
        return "LanguageIn["+sj.toString()+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(langs);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof StrLanguageIn) )
            return false;
        StrLanguageIn other = (StrLanguageIn)obj;
        return Objects.equals(langs, other.langs);
    }

}
