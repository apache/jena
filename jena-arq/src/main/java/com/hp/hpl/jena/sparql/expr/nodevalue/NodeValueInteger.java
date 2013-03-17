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

import java.math.BigDecimal ;
import java.math.BigInteger ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;


public class NodeValueInteger extends NodeValue
{
    //long integer = Integer.MIN_VALUE ;
    // The performance impact of this seems to be very low
    // After all, much of the work is pattern matching. 
    BigInteger integer ;
    
    public NodeValueInteger(BigInteger i)         { super() ; integer = i ; }
    public NodeValueInteger(BigInteger i, Node n) { super(n) ; integer = i ; }
//   public NodeValueInteger(long i, Node n)       { super(n) ; integer = new BigInteger(Long.toString(i)) ; }
    public NodeValueInteger(long i)               { super() ; integer = new BigInteger(Long.toString(i)) ; }

    @Override
    public boolean isNumber() { return true ; }
    @Override
    public boolean isInteger() { return true ; }
    @Override
    public boolean isDecimal() { return true ; }
    @Override
    public boolean isFloat()  { return true ; }
    @Override
    public boolean isDouble() { return true ; }
    
    @Override
    public BigInteger  getInteger()   { return integer ; }
    @Override
    public double getDouble()  { return integer.doubleValue() ; }
    @Override
    public float  getFloat()   { return integer.floatValue() ; }
    @Override
    public BigDecimal getDecimal()  { return new BigDecimal(integer) ; }

    @Override
    protected Node makeNode()
    { return NodeFactory.createLiteral(integer.toString(), null, XSDDatatype.XSDinteger) ; }
    
    @Override
    public String asString() { return toString() ; }
    
    @Override
    public String toString()
    { 
        // Preserve lexical form
        if ( getNode() != null ) return super.asString() ;  // str()
        return integer.toString() ;
    }
    
    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }
}
