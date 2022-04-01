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

package org.apache.jena.shex.expressions;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.util.NodeUtils;

public class ValueSetRange {
    public ValueSetItem item;
    public ArrayList<ValueSetItem> exclusions;
    public ValueSetRange(String iriStr, String lang, Node literal, boolean isStem) {
        // [shex] collapse. Subclass?
        this.item = new ValueSetItem(iriStr, lang, literal, isStem);
        this.exclusions = new ArrayList<>();
    }

    public String type() {
        if( item.iriStr != null) return "IRI";
        if( item.langStr != null) return "Language";
        if( item.literal != null) return "Literal";
        return "unknown";
    }

    public ValueSetItem item() {
        return item;
    }

    public boolean included(Node data) {
        return contains(item, data);
    }

    public boolean excluded(Node data) {
        for ( ValueSetItem ex : exclusions ) {
            if ( contains(ex, data) )
                return true;
        }
        return false;
    }

    public int numExclusions() {
        return exclusions.size();
    }

    public void exclusions(Consumer<ValueSetItem> action) {
        exclusions.forEach(action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exclusions, item);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ValueSetRange other = (ValueSetRange)obj;
        return Objects.equals(exclusions, other.exclusions) && Objects.equals(item, other.item);
    }

    // Spot the visitor pattern.

    private static boolean contains(ValueSetItem item, Node node) {
        if ( ! item.isStem )
            return matchExact(item, node);
        else
            return matchStem(item, node);
    }

    private static boolean matchExact(ValueSetItem item, Node node) {
        if ( item.langStr != null) {
            if ( ! node.isLiteral() )
                return false;
            String lang = node.getLiteralLanguage();
            return item.langStr.equalsIgnoreCase(lang);
        }
        if ( item.literal != null)
            return item.literal.equals(node);
        if ( item.iriStr != null)  {
            if ( ! node.isURI() )
                return false;
            return item.iriStr.equals(node.getURI());
        }
        // All null. DOT case.
        return true;
    }

    private static boolean matchStem(ValueSetItem item, Node node) {
        if ( item.langStr != null) {
            if ( ! NodeUtils.hasLang(node) )
                return false;
            String lang = node.getLiteralLanguage();
            String pattern = ( item.langStr.isEmpty() ) ? "*" : item.langStr;
            return NodeFunctions.langMatches(lang, pattern);
        }
        if ( item.literal != null) {
            if ( ! node.isLiteral() )
                return false;
            // [shex] Check for not strings - what is supposed to happen?
            String sData = node.getLiteralLexicalForm();
            String sSchema = NodeFunctions.str(item.literal);
            return sData.startsWith(sSchema);
        }
        if ( item.iriStr != null)  {
            if ( ! node.isURI() )
                return false;
            return node.getURI().startsWith(item.iriStr);
        }
        return false;
    }
}