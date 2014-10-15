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

package com.hp.hpl.jena.sdb.layout2;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.Named;
import com.hp.hpl.jena.sdb.SDBException;

public enum ValueType implements Named
{
    /* The ID order matters: SPARQL says:
    1.  (Lowest) no value assigned to the variable or expression in this solution.
    2. Blank nodes
    3. IRIs
    4. RDF literals
    5. A plain literal is lower than an RDF literal with type xsd:string of the same lexical form.
    */

    // Do not allocate id 0 (which is the return for NULL in JDBC)
//    NULL
//    {
//        @Override public XSDDatatype getDatatype()  { return null ; }
//        @Override public int getTypeId()            { return 0 ; }
//        @Override public String getName()           { return "null" ; }
//    } ,
    
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
    
    STRING
    {
        @Override public XSDDatatype getDatatype()  { return null ; }
        @Override public int getTypeId()            { return 3 ; }
        @Override public String getName()           { return "String" ; }
    } ,
    
    XSDSTRING
    {
        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDstring ; }
        @Override public int getTypeId()            { return 4 ; }
        @Override public String getName()           { return "XSD String" ; }
    } ,
    
    INTEGER
    {
        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDinteger ; }
        @Override public int getTypeId()            { return 5 ; }
        @Override public String getName()           { return "Integer" ; }
    } ,
    
    DOUBLE
    { 
        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDdouble ; }
        @Override public int getTypeId()            { return 6 ; }
        @Override public String getName()           { return "Double" ; }
    } ,
    
    DATETIME
    { 
        @Override public XSDDatatype getDatatype()  { return XSDDatatype.XSDdateTime ; }
        @Override public int getTypeId()            { return 7 ; }
        @Override public String getName()         { return "Datetime" ; }
    } ,
    
    VAR
    { 
        @Override public XSDDatatype getDatatype()  { return null ; }
        @Override public int getTypeId()            { return 8 ; }
        @Override public String getName()           { return "var" ; }
    } ,
    
    OTHER
    { 
        @Override public XSDDatatype getDatatype()  { return null ; }
        @Override public int getTypeId()            { return 50 ; }
        @Override public String getName()         { return "Other" ; }
    } ,

//    UNKNOWN
//    { 
//        public XSDDatatype getDatatype()  { return null ; }
//        public int getTypeId()            { return 99 ; }
//        public String printName()         { return "Unknown" ; }
//    } ,
    
    ;
    
    abstract public int getTypeId() ;
    abstract public XSDDatatype getDatatype() ;
    @Override
    abstract public String getName() ;
    @Override public String toString() { return getName() ; }
    
    static public ValueType lookup(Node n)
    {
        if ( n.isURI() ) return URI ;
        if ( n.isBlank() ) return BNODE ;
        if ( n.isLiteral() )
        {
            if ( n.getLiteralDatatypeURI() == null )
                // String - plain literal
                return STRING ;
            if ( n.getLiteralDatatype() == XSDDatatype.XSDstring )
                return XSDSTRING ;
            if ( n.getLiteralDatatype() == XSDDatatype.XSDinteger )
                return INTEGER ;
            if ( n.getLiteralDatatype() == XSDDatatype.XSDdouble )
                return DOUBLE ;
            if ( n.getLiteralDatatype() == XSDDatatype.XSDdateTime )
                return DATETIME ;
        }
        return OTHER ;
    }
    
    static public ValueType lookup(int type)
    {
        // Is there a better to ensure all cases are covered?
        if ( type == BNODE.getTypeId() )      return BNODE ;
        if ( type == URI.getTypeId() )        return URI ;
        if ( type == STRING.getTypeId() )     return STRING ;
        if ( type == XSDSTRING.getTypeId() )  return XSDSTRING ;
        if ( type == INTEGER.getTypeId() )    return INTEGER ;
        if ( type == DOUBLE.getTypeId() )     return DOUBLE ;
        if ( type == DATETIME.getTypeId() )   return DATETIME ;
        if ( type == VAR.getTypeId() )        return VAR ;
        if ( type == OTHER.getTypeId() )      return OTHER ;
        
//        LoggerFactory.getLogger(ValueType.class).warn("Seen an unrecognized type") ;
//        return UNKNOWN ; 
        throw new SDBException("Unknown type ("+type+")") ;
    }
    
}
