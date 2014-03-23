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

package com.hp.hpl.jena.sparql.expr.nodevalue;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;


public class NodeValueString extends NodeValue
{
    // A plain string, no language tag or xsd:string.
    
    private String string ; 
    
    public NodeValueString(String str)         { string = str ; } 
    public NodeValueString(String str, Node n) { super(n) ; string = str ; }
    
    @Override
    public boolean isString() { return true ; }
    
    @Override
    public String getString() { return string ; }

    @Override
    public String asString() { return string ; }
    
    @Override
    public String toString()
    { 
        if ( getNode() != null )
        {
            // Can be a plain string or an xsd:string.
            return FmtUtils.stringForNode(getNode()) ;
        }
        return '"'+string+'"'  ;
    }
    
    @Override
    protected Node makeNode()
    { return NodeFactory.createLiteral(string) ; }
    
    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }
}
