/**
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

package org.apache.jena.sparql.expr.nodevalue;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.FmtUtils;

/**
 * A NodeValue that is a lang tagged literal with base direction (rdf:dirLangString).
 * A string + language tag which is not "" + base direction which is "ltr" or "rtl".
 */
public class NodeValueLangDir extends NodeValue {
    private final String string;
    private final String lang;
    private final TextDirection textDir;

    public NodeValueLangDir(String lex, String lang, String textDirStr) {
        this.string = Objects.requireNonNull(lex);
        this.lang = Objects.requireNonNull(lang);
        if ( lang.isEmpty() )
            throw new IllegalArgumentException("lang is the empty string");
        Objects.requireNonNull(textDirStr);
        this.textDir = TextDirection.createOrNull(textDirStr);
        if ( textDir == null )
            throw new IllegalArgumentException("base direction is not valid : '"+textDirStr+"'");
    }

    public NodeValueLangDir(String lex, String lang, TextDirection textDir) {
        this.string = Objects.requireNonNull(lex);
        this.lang = Objects.requireNonNull(lang);
        if ( lang.isEmpty() )
            throw new IllegalArgumentException("lang is the empty string");
        this.textDir = Objects.requireNonNull(textDir);
    }

    public NodeValueLangDir(Node n) {
        super(Objects.requireNonNull(n));
        this.string = n.getLiteralLexicalForm();
        this.lang = n.getLiteralLanguage();
        this.textDir = n.getLiteralBaseDirection();
    }

    @Override
    public boolean isLangString() {
        return true;
    }

    @Override
    public String getString()   { return string; }

    @Override
    public String getLang()     { return lang; }

    @Override
    public String getLangDir()  { return textDir.direction(); }

    @Override
    public String asString()    { return string; }

    @Override
    protected Node makeNode()
    { return NodeFactory.createLiteralDirLang(string, lang, textDir); }

    @Override
    public String toString() {
        if ( getNode() != null )
            return FmtUtils.stringForNode(getNode());
        return "'"+getString()+"'@"+lang+"--"+textDir.direction() ;
    }

    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this); }
}
