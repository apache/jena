/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.nodevalue;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;


public class NodeValueInteger extends NodeValue
{
    //long integer = Integer.MIN_VALUE ;
    // The performance impact of this seems to be very low
    // After all, much of the work is pattern matching. 
    BigInteger integer ;
    
    public NodeValueInteger(BigInteger i)         { super() ; integer = i ; }
    public NodeValueInteger(BigInteger i, Node n) { super(n) ; integer = i ; }
    public NodeValueInteger(long i, Node n)       { super(n) ; integer = new BigInteger(Long.toString(i)) ; }
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
    { return Node.createLiteral(integer.toString(), null, XSDDatatype.XSDinteger) ; }
    
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

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
