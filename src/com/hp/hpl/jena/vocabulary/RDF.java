/*
 *  (c)     Copyright Hewlett-Packard Company 2000, 2001, 2002
 *   All rights reserved.
 * [See end of file]
 *  $Id: RDF.java,v 1.1.1.1 2002-12-19 19:21:39 bwm Exp $
 */


package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;


import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFException;

/**
 *
 * @author  bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:21:39 $'
 */
public class RDF{
    
    // next free error code = 2
    
    protected static final String uri =
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
/** returns the URI for this schema
 * @return the URI for this schema
 */    
    public static String getURI()
    {
        return uri;
    }
    
    public static       Property li(int i) throws RDFException {
    	// System.err.println( "constructing RDF.li(" + i + ")" );
        return new PropertyImpl(uri, "_"+Integer.toString(i));}
    
    
           static final String   nAlt = "Alt";
    public static       Resource Alt = null;    
           static final String   nBag = "Bag";
    public static       Resource Bag = null;    
           static final String   nProperty = "Property";
    public static       Resource Property;
           static final String   nSeq = "Seq";
    public static       Resource Seq = null;
           static final String   nStatement = "Statement";
    public static       Resource Statement = null;
           static final String   nList = "List";
    public static       Resource List = null;
           static final String   nnil = "nil";
    public static       Resource nil = null;
    

           static final String   nfirst = "first";
    public static       Property first;
           static final String   nrest = "rest";
    public static       Property rest;
           static final String   nsubject = "subject";
    public static       Property subject; 
           static final String   npredicate = "predicate";
    public static       Property predicate;
           static final String   nobject = "object";
    public static       Property object; 
           static final String ntype = "type";
    public static       Property type;
           static final String   nvalue = "value";
    public static       Property value;
   

    
    static {
        try {
            Alt         = new ResourceImpl(uri+nAlt);
            Bag         = new ResourceImpl(uri+nBag);
            Property    = new ResourceImpl(uri + nProperty);
            Seq         = new ResourceImpl(uri+nSeq);
            Statement   = new ResourceImpl(uri+nStatement);
            List        = new ResourceImpl(uri+nList);
            nil         = new ResourceImpl(uri+nnil);
            type        = new PropertyImpl(uri, ntype);
            rest        = new PropertyImpl(uri, nrest);
            first       = new PropertyImpl(uri, nfirst);
            subject     = new PropertyImpl(uri, nsubject);
            predicate   = new PropertyImpl(uri, npredicate);
            object      = new PropertyImpl(uri, nobject);
            value       = new PropertyImpl(uri, nvalue);

        } catch (RDFException e) {
            ErrorHelper.logInternalError("RDF", 1, e);
        }
    }
    
}

/*
 *  (c)   Copyright Hewlett-Packard Company 2000, 2001, 2002
 *   All rights reserved.
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
 *
 * RDF.java
 *
 * Created on 28 July 2000, 18:12
 */