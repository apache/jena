/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: RDFS.java,v 1.5 2003-06-23 12:59:03 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 *
 * @author  bwm, updated by kers/daniel/christopher
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.5 $' Date='$Date: 2003-06-23 12:59:03 $'
 */
public class RDFS {

    protected static final String uri="http://www.w3.org/2000/01/rdf-schema#";
    
    private static Model m = ModelFactory.createDefaultModel();
    
    public static final Resource Class     = m.createResource(uri+"Class");
    public static final Resource Datatype            = m.createResource(uri+"Datatype");
    public static final Resource ConstraintProperty  =  m.createResource(uri+"ConstraintProperty");
    public static final Resource Container           = m.createResource(uri+"Container");
    public static final Resource ContainerMembershipProperty
                                                     = m.createResource(uri+"ContainerMembershipProperty");
    public static final Resource ConstraintResource  = m.createResource(uri+"ConstraintResource");
    public static final Resource Literal             = m.createResource(uri+"Literal");
    public static final Resource Resource            = m.createResource(uri+"Resource");
    
    public static final Property comment             = m.createProperty(uri, "comment");
    public static final Property domain              = m.createProperty(uri, "domain");
    public static final Property label               = m.createProperty(uri, "label");
    public static final Property isDefinedBy         = m.createProperty(uri, "isDefinedBy");
    public static final Property range               = m.createProperty(uri, "range");
    public static final Property seeAlso             = m.createProperty(uri, "seeAlso");
    public static final Property subClassOf          = m.createProperty(uri, "subClassOf");
    public static final Property subPropertyOf       = m.createProperty(uri, "subPropertyOf");
    public static final Property member             = m.createProperty(uri, "member"); 

/** returns the URI for this schema
 * @return the URI for this schema
 */    
    public static String getURI() {
        return uri;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 *
 * RDFS.java
 *
 * Created on 28 July 2000, 18:13
 */