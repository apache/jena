/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;


/**
 * @author Elias Torres (<a href="mailto:elias@torrez.us">elias@torrez.us</a>)
 */
public class JSONResults
{
    public static final int INDENT = 2 ;
        
    public static final String dfHead       = "head" ;
    public static final String dfVars       = "vars" ;
    public static final String dfLink       = "link" ;
    public static final String dfResults    = "results" ;
    public static final String dfBindings   = "bindings" ;
    public static final String dfBoolean    = "boolean" ;
    
//    public static final String dfOrdered    = "ordered" ;
//    public static final String dfDistinct   = "distinct" ;
    
    public static final String dfType       = "type" ;
    public static final String dfValue      = "value" ;
    public static final String dfDatatype   = "datatype" ;
    public static final String dfLang       = "xml:lang" ;
    
    public static final String dfBNode          = "bnode" ;
    public static final String dfURI            = "uri" ;
    public static final String dfLiteral        = "literal" ;
    public static final String dfTypedLiteral   = "typed-literal" ;  
    public static final String dfUnbound    = "unbound" ;
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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