/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.nodevalue;

import java.math.BigDecimal;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.Utils;


public class NodeValueDecimal extends NodeValue
{
    BigDecimal decimal ;
    
    public NodeValueDecimal(BigDecimal d)         { decimal = d ; }
    public NodeValueDecimal(BigDecimal d, Node n) { super(n) ; decimal = d ; }
    
    @Override
    public boolean isNumber() { return true ; }
    @Override
    public boolean isDecimal() { return true ; }
    @Override
    public boolean isFloat()  { return true ; }
    @Override
    public boolean isDouble() { return true ; }
    
    @Override
    public BigDecimal getDecimal()  { return decimal ; }
    @Override
    public float getFloat()    { return decimal.floatValue() ; }
    @Override
    public double getDouble()  { return decimal.doubleValue() ; }

    @Override
    protected Node makeNode()
    { 
        int s = decimal.scale() ;
        return Node.createLiteral(Utils.stringForm(decimal), null, XSDDatatype.XSDdecimal) ;
    }
    
    @Override
    public String asString() { return toString() ; }
    
    @Override
    public String toString()
    { 
        // Preserve lexical form.
        if ( getNode() != null ) return super.asString() ;
        return Utils.stringForm(decimal) ;
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
