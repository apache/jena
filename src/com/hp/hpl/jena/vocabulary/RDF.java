/*
 *  (c)     Copyright Hewlett-Packard Company 2000, 2001, 2002. All rights reserved.
 * [See end of file]
 *  $Id: RDF.java,v 1.5 2003-06-23 11:01:37 chris-dollin Exp $
*/


package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 *
 * @author  bwm; updated by kers/daniel/christopher
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.5 $' Date='$Date: 2003-06-23 11:01:37 $'
 */
public class RDF{


    protected static final String uri =
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

/** returns the URI for this schema
 * @return the URI for this schema
 */
    public static String getURI()
    {
        return uri;
    }
    
    /*
         would use a Model as a resource/property factory, but ... createDefaultModel
         needs Reifier.Standard which needs Reifier which needs RDF.type; oops,
         circularity and sudden death. 
         TODO break circularity somehow. 
    */

    public static       Property li(int i) {
    	// System.err.println( "constructing RDF.li(" + i + ")" );
        return new PropertyImpl(uri, "_"+Integer.toString(i));}

    public static       Resource Alt = new ResourceImpl(uri+"Alt");
    public static       Resource Bag = new ResourceImpl(uri+"Bag");
    public static       Resource Property = new ResourceImpl(uri+"Property");
    public static       Resource Seq = new ResourceImpl(uri+"Seq");
    public static       Resource Statement = new ResourceImpl(uri+"Statement");
    public static       Resource List = new ResourceImpl(uri+"List");
    public static       Resource nil = new ResourceImpl(uri+"nil");

    public static       Property first = new PropertyImpl(uri, "first");
    public static       Property rest = new PropertyImpl(uri, "rest");
    public static       Property subject = new PropertyImpl(uri, "subject");
    public static       Property predicate = new PropertyImpl(uri, "predicate");
    public static       Property object = new PropertyImpl(uri, "object");
    public static       Property type = new PropertyImpl(uri, "type");
    public static       Property value = new PropertyImpl(uri, "value");

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
