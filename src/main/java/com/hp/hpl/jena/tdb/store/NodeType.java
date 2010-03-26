/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.Named;

import com.hp.hpl.jena.tdb.TDBException;

public enum NodeType implements Named
{
    // Do not allocate id 0
//  NULL
//  {
//  @Override public XSDDatatype getDatatype()  { return null ; }
//  @Override public int getTypeId()            { return 0 ; }
//  @Override public String getName()           { return "null" ; }
//  } ,

    BNODE
    {
        @Override public XSDDatatype getDatatype()  { return null ; }
        @Override public int getTypeId()            { return 1 ; }
        @Override public String getName()           { return "BNode" ; }
    } ,

    URI
    {
        @Override public XSDDatatype getDatatype()  { return null ; }
        @Override public int getTypeId()            { return 2 ; }
        @Override public String getName()           { return "URI" ; }
    } ,
    
    LITERAL
    {
        @Override public XSDDatatype getDatatype()  { return null ; }
        @Override public int getTypeId()            { return 3 ; }
        @Override public String getName()           { return "Literal" ; }
    } ,

//    STRING
//    {
//        @Override public XSDDatatype getDatatype()  { return null ; }
//        @Override public int getTypeId()            { return 4 ; }
//        @Override public String getName()           { return "String" ; }
//    } ,
//
//    XSDSTRING
//    {
//        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDstring ; }
//        @Override public int getTypeId()            { return 5 ; }
//        @Override public String getName()           { return "XSD String" ; }
//    } ,
//
//    INTEGER
//    {
//        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDinteger ; }
//        @Override public int getTypeId()            { return 6 ; }
//        @Override public String getName()           { return "Integer" ; }
//    } ,
//
//    DOUBLE
//    { 
//        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDdouble ; }
//        @Override public int getTypeId()            { return 7 ; }
//        @Override public String getName()           { return "Double" ; }
//    } ,
//
//    DATETIME
//    { 
//        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDdateTime ; }
//        @Override public int getTypeId()            { return 8 ; }
//        @Override public String getName()         { return "Datetime" ; }
//    } ,
    
//  G_YEAR
//  { 
//      @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDgYear ; }
//      @Override public int getTypeId()            { return 9 ; }
//      @Override public String getName()         { return "gYear" ; }
//  } ,
    
//  G_YEARMONTH
//  { 
//      @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDgYearMonth ; }
//      @Override public int getTypeId()            { return 10 ; }
//      @Override public String getName()         { return "gYearMonth" ; }
//  } ,
        
//  G_MONTH
//  { 
//      @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDgMonth ; }
//      @Override public int getTypeId()            { return 11 ; }
//      @Override public String getName()         { return "gMonth" ; }
//  } ,
    
//  G_MONTHDAY
//  { 
//      @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDgMonthDay ; }
//      @Override public int getTypeId()            { return 12 ; }
//      @Override public String getName()         { return "gMonthDay" ; }
//  } ,
    
//  G_DAY
//  { 
//      @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDgDay ; }
//      @Override public int getTypeId()            { return 13 ; }
//      @Override public String getName()         { return "gDay" ; }
//  } ,
    
            
//
//    VAR
//    { 
//        @Override public XSDDatatype getDatatype()  { return null ; }
//        @Override public int getTypeId()            { return 40; }
//        @Override public String getName()           { return "var" ; }
//    } ,
//
    OTHER
    { 
        @Override public XSDDatatype getDatatype()  { return null ; }
        @Override public int getTypeId()            { return 50 ; }
        @Override public String getName()         { return "Other" ; }
    } ,

//  UNKNOWN
//  { 
//  public XSDDatatype getDatatype()  { return null ; }
//  public int getTypeId()            { return 99 ; }
//  public String printName()         { return "Unknown" ; }
//  } ,

    ;

    abstract public int getTypeId() ;
    abstract public XSDDatatype getDatatype() ;
    abstract public String getName() ;
    @Override public String toString() { return getName() ; }

    static public NodeType lookup(Node n)
    {
        if ( n.isURI() ) return URI ;
        if ( n.isBlank() ) return BNODE ;
        if ( n.isLiteral() )
        {
            return LITERAL ; 
//            if ( n.getLiteralDatatypeURI() == null )
//                // String - plain literal
//                return STRING ;
//            if ( n.getLiteralDatatype() == XSDDatatype.XSDstring )
//                return XSDSTRING ;
//            if ( n.getLiteralDatatype() == XSDDatatype.XSDinteger )
//                return INTEGER ;
//            if ( n.getLiteralDatatype() == XSDDatatype.XSDdouble )
//                return DOUBLE ;
//            if ( n.getLiteralDatatype() == XSDDatatype.XSDdateTime )
//                return DATETIME ;
        }
        return OTHER ;
    }

    static public NodeType lookup(int type)
    {
        // Is there a better to ensure all cases are covered?
        if ( type == BNODE.getTypeId() )      return BNODE ;
        if ( type == URI.getTypeId() )        return URI ;
        if ( type == LITERAL.getTypeId() )    return LITERAL ;
//        if ( type == STRING.getTypeId() )     return STRING ;
//        if ( type == XSDSTRING.getTypeId() )  return XSDSTRING ;
//        if ( type == INTEGER.getTypeId() )    return INTEGER ;
//        if ( type == DOUBLE.getTypeId() )     return DOUBLE ;
//        if ( type == DATETIME.getTypeId() )   return DATETIME ;
//        if ( type == VAR.getTypeId() )        return VAR ;
//        if ( type == OTHER.getTypeId() )      return OTHER ;

//      LogFactory.getLog(ValueType.class).warn("Seen an unrecognized type") ;
//      return UNKNOWN ; 
        throw new TDBException("Unknown type ("+type+")") ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
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