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

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;

/**
 *
 * @author  bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:21:40 $'
 */
public class RDFS {
    
    // next available error code = 2

    protected static    String uri="http://www.w3.org/2000/01/rdf-schema#";
    
           static final String   nClass    = "Class";
    public static       Resource Class     = null;
           static final String   nConstraintProperty = "ConstraintProperty";
    public static       Resource ConstraintProperty  = null;
           static final String   nContainer          = "Container";
    public static       Resource Container           = null;
           static final String   
                        nContainerMembershipProperty =
                                                 "ContainerMembershipProperty";
    public static       Resource ContainerMembershipProperty
                                                     = null;
           static final String   nConstraintResource = "ConstraintResource";
    public static       Resource ConstraintResource  = null;
           static final String   nLiteral            = "Literal";
    public static       Resource Literal             = null;
           static final String   nResource           = "Resource";
    public static       Resource Resource            = null;
    
           static final String   ncomment            = "comment";
    public static       Property comment             = null;
           static final String   ndomain             = "domain";
    public static       Property domain              = null;
           static final String   nlabel              = "label";
    public static       Property label               = null;
           static final String   nisDefinedBy        = "isDefinedBy";
    public static       Property isDefinedBy         = null;
           static final  String   nrange              = "range";
    public static       Property range               = null;
           static final String   nseeAlso            = "seeAlso";
    public static       Property seeAlso             = null;
           static final String   nsubClassOf         = "subClassOf";
    public static       Property subClassOf          = null;
           static final String   nsubPropertyOf      = "subPropertyOf";
    public static       Property subPropertyOf       = null;
    
    static {
        try {
            Class = new ResourceImpl(uri+nClass);
            ConstraintProperty = new ResourceImpl(uri+nConstraintProperty);
            Container = new ResourceImpl(nContainer);
            ContainerMembershipProperty = 
                             new ResourceImpl(uri+nContainerMembershipProperty);
            ConstraintResource = new ResourceImpl(uri+nConstraintResource);
            Literal = new ResourceImpl(uri+nLiteral);
            Resource = new ResourceImpl(uri+nResource);
            
            comment = new PropertyImpl(uri, ncomment);
            domain = new PropertyImpl(uri, ndomain);
            label = new PropertyImpl(uri, nlabel);
            isDefinedBy = new PropertyImpl(uri, nisDefinedBy);
            range = new PropertyImpl(uri, nrange);
            seeAlso = new PropertyImpl(uri, nseeAlso);
            subClassOf = new PropertyImpl(uri, nsubClassOf);
            subPropertyOf = new PropertyImpl(uri, nsubPropertyOf);
        } catch (Exception e) {
            ErrorHelper.logInternalError("RDFS", 1, e);
        }
    }
                   

/** returns the URI for this schema
 * @return the URI for this schema
 */    
    public static String getURI() {
        return uri;
    }
}