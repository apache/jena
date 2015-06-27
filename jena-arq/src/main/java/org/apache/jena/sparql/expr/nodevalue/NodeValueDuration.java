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

package org.apache.jena.sparql.expr.nodevalue;

import javax.xml.datatype.Duration ;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.expr.NodeValue ;

/** XSD Duration */ 

public class NodeValueDuration extends NodeValue
{
    Duration duration ; 
    
    public NodeValueDuration(Duration dt)
    { 
        duration = dt ;
    }
    
    public NodeValueDuration(Duration dt, Node n) { super(n) ; duration = dt ; }
    
    @Override
    public boolean isDuration() { return true ; }
    @Override
    public Duration getDuration()     { return duration ; }

    @Override
    protected Node makeNode()
    {
        String lex = duration.toString() ;
        return NodeFactory.createLiteral(lex, XSDDatatype.XSDduration) ;
    }
    
    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }
}
