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

import java.util.Objects ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.util.FmtUtils ;

/** A NodeValue that is a lang tagged literal (rdf:langString).
 * A string + language tag which is not ""
 */
public class NodeValueLang extends NodeValue {
    // We could extends NodeValueString for the machinery
    // but it get confusing as then it is a NodeValueString 
    // but isString is false.
    
    private final String string ; 
    private final String lang;

    public NodeValueLang(String lex, String lang) {
        this.string = Objects.requireNonNull(lex) ;
        this.lang = Objects.requireNonNull(lang) ;
        if ( lang.isEmpty() )
            throw new IllegalArgumentException("lang is the empty string") ;
    }
    
    public NodeValueLang(Node n) {
        super(Objects.requireNonNull(n)) ;
        this.string = n.getLiteralLexicalForm() ;
        this.lang = n.getLiteralLanguage() ;
    }

    @Override
    public boolean isLangString() {
        return true;
    }
    
    @Override
    public String getString()   { return string ; }

    @Override
    public String getLang()     { return lang ; }
    
    @Override
    public String asString()    { return string ; }
    
    @Override
    protected Node makeNode()
    { return NodeFactory.createLiteral(string, lang) ; }
    
    @Override
    public String toString() { 
        if ( getNode() != null )
            return FmtUtils.stringForNode(getNode()) ;
        return "'"+getString()+"'@"+lang  ;
    }
    
    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }
}
