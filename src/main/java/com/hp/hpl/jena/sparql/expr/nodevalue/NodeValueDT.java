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

import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdate ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdateTime ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgDay ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgMonth ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgMonthDay ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgYear ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgYearMonth ;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDtime ;

import javax.xml.datatype.XMLGregorianCalendar ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

public class NodeValueDT extends NodeValue
{
    final private XMLGregorianCalendar datetime ;
    
    public NodeValueDT(String lex, Node n)
    {
        super(n) ;
        // Java bug : Java6 gMonth with a timezone of Z causes IllegalArgumnentException
        if ( XSDgMonth.equals(getNode().getLiteralDatatype()) )
        {
            if ( lex.endsWith("Z") )
            {
                lex = lex.substring(0, lex.length()-1) ;
                datetime = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(lex) ;
                datetime.setTimezone(0) ;
                return ;
            }
        }
        datetime = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(lex) ;
    }

    // Look at datatype.
    // By doing this here, the check of valid lexical form must have been done to create the node.
    
   
    @Override
    public boolean isDateTime()     { return XSDdateTime.equals(getNode().getLiteralDatatype()) ; }
    @Override
    public boolean isDate()         { return XSDdate.equals(getNode().getLiteralDatatype()) ; }
    @Override
    public boolean isTime()         { return XSDtime.equals(getNode().getLiteralDatatype()) ; }
    @Override
    public boolean isGYear()        { return XSDgYear.equals(getNode().getLiteralDatatype()) ; }
    @Override
    public boolean isGYearMonth()   { return XSDgYearMonth.equals(getNode().getLiteralDatatype()) ; }
    @Override
    public boolean isGMonth()       { return XSDgMonth.equals(getNode().getLiteralDatatype()) ; }
    @Override
    public boolean isGMonthDay()    { return XSDgMonthDay.equals(getNode().getLiteralDatatype()) ; }
    @Override
    public boolean isGDay()         { return XSDgDay.equals(getNode().getLiteralDatatype()) ; }
    
    @Override
    public XMLGregorianCalendar getDateTime()    { return (XMLGregorianCalendar)datetime.clone() ; }

    
    @Override
    protected Node makeNode()
    {
        return null ;
    }

    @Override
    public void visit(NodeValueVisitor visitor)
    { visitor.visit(this) ; }
}

