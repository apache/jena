/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function.library;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;
import com.hp.hpl.jena.sparql.util.ALog;

/** date(expression) => XSD dateTime 
 *  Attempt to convert an expression to an XSD dateTime.
 *  Supported conversions: Date as yyyy-mm-dd
 */ 

public class date extends FunctionBase1
{
    public date() {  }

    @Override
    public NodeValue exec(NodeValue v)
    {
        if ( ! v.isString() )
        {
            ALog.warn(this, "date: argument not a string: "+v) ;
            throw new ExprEvalException("date: argument not a string: "+v) ;
        }
        
        String lexicalForm = v.getString() ;
        
        // Quite picky about format
        if ( ! lexicalForm.matches("\\d{4}-\\d{2}-\\d{2}") )
        {
            ALog.warn(this, "date: argument not in date format: "+v) ;
            throw new ExprEvalException("date: argument not in date format: "+v) ;
        }
        
        lexicalForm=lexicalForm+"T00:00:00Z" ;
        
        NodeValue nv = NodeValue.makeNode(lexicalForm, XSDDatatype.XSDdateTime) ;
        return nv ;
    }
    
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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