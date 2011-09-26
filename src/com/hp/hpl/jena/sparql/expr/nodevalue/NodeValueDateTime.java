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

package com.hp.hpl.jena.sparql.expr.nodevalue;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

/** XSD dateTime (which is unrelated to XSD date in the datatype hierarchy) */ 

public class NodeValueDateTime extends NodeValue
{
    XSDDateTime dateTime ;
    
    public NodeValueDateTime(XSDDateTime xdt) { dateTime = xdt ; }
    public NodeValueDateTime(XSDDateTime xdt, Node n) { super(n) ; dateTime = xdt ; }
    
    @Override
    public boolean isDateTime() { return true ; }
    @Override
    public XSDDateTime getDateTime() { return dateTime ; }
    
    @Override
    protected Node makeNode()
    {
        String lex = dateTime.toString() ;
        return Node.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
    }
    
    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }
}
