/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.nodevalue;

import java.util.Calendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.Utils;

/** XSD Date (which is unrelated to XSD dateTime in the datatype hierarchy) */ 

public class NodeValueDate extends NodeValue
{
    // For comparisons - add a time 00:00:00
    Calendar date ;
    
    public NodeValueDate(Calendar cal)
    { 
        date = (Calendar)cal.clone() ;
        // Force the time to 00:00:00 
        date.set(Calendar.HOUR, 0) ;
        date.set(Calendar.MINUTE, 0) ;
        date.set(Calendar.SECOND, 0) ;
        date.set(Calendar.MILLISECOND, 0) ;
    }
    public NodeValueDate(Calendar cal, Node n) { super(n) ; date = cal ; }
    
    //@Override
    public boolean isDate() { return true ; }
    //@Override
    public Calendar getDate()     { return date ; }
    
    //@Override
    protected Node makeNode()
    {
       String lex = Utils.calendarToXSDDateString(date) ;
       return Node.createLiteral(lex, null, XSDDatatype.XSDdate) ;
    }
    
    public void visit(NodeValueVisitor visitor) { visitor.visit(this) ; }

}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
