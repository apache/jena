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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.util.Utils ;


public class NodeValueFloat extends NodeValue
{
    float value = Float.NaN ;
    
    public NodeValueFloat(float f)         { super() ; value = f ; }
    public NodeValueFloat(float f, Node n) { super(n) ; value = f ; } 

    @Override
    public boolean isNumber() { return true ; }
    @Override
    public boolean isFloat() { return true ; }
    @Override
    public boolean isDouble() { return true ; }
    
    @Override
    public float getFloat()  { return value ; }
    @Override
    public double getDouble()  { return value ; }

    @Override
    protected Node makeNode()
    { return NodeFactory.createLiteral(Utils.stringForm(value), null, XSDDatatype.XSDfloat) ; }
    
    @Override
    public String asString() { return toString() ; }
    
    @Override
    public String toString()
    { 
        // Preserve lexical form
        if ( getNode() != null ) return super.asString() ;
        return Float.toString(value) ;
    }
    
    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }
}
