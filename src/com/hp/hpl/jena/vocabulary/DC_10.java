/*
 * (c) Copyright 2000-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFException;

/** Dublin Core version 1.0 vocabulary.
 */

public class DC_10 {
    
    protected static final String uri =
        "http://purl.org/dc/elements/1.0/";
    
/** returns the URI for this schema
 * @return the URI for this schema
 */    
    public static String getURI()
    {
        return uri;
    }
    

           static final String   ncontributor = "contributor";
    public static       Property contributor;  
           static final String   ncoverage = "coverage";
    public static       Property coverage;
           static final String   ncreator = "creator";
    public static       Property creator;  
           static final String   ndate = "date";
    public static       Property date;
           static final String   ndescription = "description";
    public static       Property description;  
           static final String   nformat = "format";
    public static       Property format; 
           static final String   nidentifier = "identifier";
    public static       Property identifier; 
           static final String   nlanguage = "language";
    public static       Property language;
           static final String   npublisher = "publisher";
    public static       Property publisher;  
           static final String   nrelation = "relation";
    public static       Property relation; 
           static final String   nrights = "rights";
    public static       Property rights;
           static final String   nsource = "source";
    public static       Property source;
           static final String   nsubject = "subject";
    public static       Property subject; 
           static final String   ntitle = "title";
    public static       Property title;
           static final String ntype = "type";
    public static       Property type;   

    
    static {
        try {
            contributor   = new PropertyImpl(uri, ncontributor);
            coverage      = new PropertyImpl(uri, ncoverage);
            creator       = new PropertyImpl(uri, ncreator);
            date          = new PropertyImpl(uri, ndate);
            description   = new PropertyImpl(uri, ndescription);
            format        = new PropertyImpl(uri, nformat);
            identifier    = new PropertyImpl(uri, nidentifier);
            language      = new PropertyImpl(uri, nlanguage);
            publisher     = new PropertyImpl(uri, npublisher);
            relation      = new PropertyImpl(uri, nrelation);
            rights        = new PropertyImpl(uri, nrights);
            source        = new PropertyImpl(uri, nsource);
            subject       = new PropertyImpl(uri, nsubject);
            title         = new PropertyImpl(uri, ntitle);
            type          = new PropertyImpl(uri, ntype);
        } catch (RDFException e) {
            ErrorHelper.logInternalError("RDF", 1, e);
        }
    }
}




/*
 *  (c) Copyright Hewlett-Packard Company 2000-2003 
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

