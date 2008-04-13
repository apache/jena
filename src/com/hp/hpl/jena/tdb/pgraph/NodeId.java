/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.tdb.Const;

import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;

import lib.Bytes;

final
public class NodeId
{
    public static final NodeId NodeDoesNotExist = new NodeId(-8) ;
    public static final NodeId NodeIdAny = new NodeId(-9) ;
    
    public static final NodeId n0 = new NodeId(0) ; 
    public static final NodeId n1 = new NodeId(1) ; 
    public static final NodeId n2 = new NodeId(2) ; 

    public static final NodeId n3 = new NodeId(3) ; 
    public static final NodeId n4 = new NodeId(4) ; 
    public static final NodeId n5 = new NodeId(5) ; 

    
    // NB If there is any sort of cahce with a NodeId in it, then there is an object created
    // by boxing anyway (unless swap to using Trove with it's hardcoded int/long implementation)
    // Therefore the cost of a NodeId is not as great as it might be.
    // Could recycle them (but the value field wil not be final) 
    
    public static final int SIZE = Const.SizeOfLong ;
    final long value ;
    
    public static NodeId create(long value) { return new NodeId(value) ; }
    
    // Chance for a cache? (Small Java objects are really not that expensive these days.)
    public static NodeId create(byte[] b, int idx)
    {
        return new NodeId(b, idx) ;
    }
    
    public static NodeId create(ByteBuffer b, int idx)
    {
        return new NodeId(b, idx) ;
    }
    
    private NodeId(byte[] b, int idx)
    {
        value = Bytes.getLong(b, idx) ;
    }
    
    private NodeId(ByteBuffer b, int idx)
    {
        value = b.getLong(idx) ;
    }
    
    
    private NodeId(long v) { value = v ;}
    
    public void toByteBuffer(ByteBuffer b, int idx) { b.putLong(idx, value) ; }
    
    public void toBytes(byte[] b, int idx) { Bytes.setLong(value, b, idx) ; }
 
    public boolean isDirect() { return false ; }
    
    // Masked?
    public long getId() { return value ; }
    
    @Override
    public int hashCode() { return (int)value ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( !(other instanceof NodeId ) ) return false ;
        return value == ((NodeId)other).value ;
    }
    
    @Override
    public String toString()
    { 
        //if ( value == NodeDoesNotExist ) return "[DoesNotExist]" ;
        if ( this == NodeIdAny ) return "[Any]" ;
        return "["+value+"]" ; }
    
    // ---- Encoding special - inlines.
    
    
    /** Encode a node as an inline literal.  Return null if it can't be done */
    
    protected NodeId inline(Node node)
    {
        if ( ! node.isLiteral() ) return null ;
        String lex = node.getLiteralLexicalForm() ;
        LiteralLabel lit = node.getLiteral() ;
        
        RDFDatatype dt = node.getLiteralDatatype() ;
        
        // xsd:decimal
        // xsd:integer, xsd:int and lots of others.
        
        // Decimal is a valid supertype of integer but we handle integers and decimals differently.
        if ( node.getLiteralDatatype().equals(XSDDatatype.XSDdecimal) )
        {
            // XSD Decimal
        }
        else
        {
            if ( XSDDatatype.XSDinteger.isValidLiteral(lit) )
            {
                long v = ((Number)lit.getValue()).longValue() ;
                if ( Math.abs(v) < 0x007FFFFF )
                {
                    // Convert to a 56 bit number (sign!)
                    v = v & 0x00FFFFFF ; 
                    // Type = 1 
                    BitsLong.
                    
                    return new NodeId(v | 0x01000000) ;
                }
            }
        }
        if ( XSDDatatype.XSDdateTime.isValidLiteral(lit) )
        {}
        if ( XSDDatatype.XSDdate.isValidLiteral(lit) )
        {}
        
        
        return null ;
    }
    
    
    //public reset(long value) { this.value = value ; }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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