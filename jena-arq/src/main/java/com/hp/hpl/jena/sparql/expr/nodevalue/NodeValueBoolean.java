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
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;


public class NodeValueBoolean extends NodeValue
{
    boolean bool = false ;
    
    public NodeValueBoolean(boolean b)         { super() ;  bool = b ; }
    public NodeValueBoolean(boolean b, Node n) { super(n) ; bool = b ; }

    @Override
    public boolean isBoolean()  { return true ; }

    @Override
    public boolean getBoolean() { return bool ; }

    @Override
    protected Node makeNode() 
    { return bool ? NodeConst.nodeTrue :  NodeConst.nodeFalse ; } 
    
    @Override
    public String asString() { return toString() ; }
    
    @Override
    public String toString()
    { return bool ? "true" : "false" ; }
    
    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }
}
