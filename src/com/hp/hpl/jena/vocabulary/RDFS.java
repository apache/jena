/*
  (c) Copyright 2000, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: RDFS.java,v 1.8 2003-07-18 10:32:30 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;

/**
    RDFS vocabulary items
    @author  bwm, updated by kers/daniel/christopher
    @version $Id: RDFS.java,v 1.8 2003-07-18 10:32:30 chris-dollin Exp $
 */
public class RDFS {

    protected static final String uri="http://www.w3.org/2000/01/rdf-schema#";

    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource Class = resource( "Class");
    public static final Resource Datatype = resource( "Datatype");
    public static final Resource ConstraintProperty  =  resource( "ConstraintProperty");
    public static final Resource Container  = resource( "Container");
    public static final Resource ContainerMembershipProperty
                                                     = resource( "ContainerMembershipProperty");
    public static final Resource ConstraintResource  = resource( "ConstraintResource");
    public static final Resource Literal = resource( "Literal");
    public static final Resource Resource = resource( "Resource");

    public static final Property comment = property( "comment");
    public static final Property domain = property( "domain");
    public static final Property label = property( "label");
    public static final Property isDefinedBy = property( "isDefinedBy");
    public static final Property range = property( "range");
    public static final Property seeAlso = property( "seeAlso");
    public static final Property subClassOf  = property( "subClassOf");
    public static final Property subPropertyOf  = property( "subPropertyOf");
    public static final Property member  = property( "member");

    public static class Nodes
        {
        public static final Node Class = RDFS.Class.getNode();
        public static final Node Datatype = RDFS.Datatype.getNode();
        public static final Node ConstraintProperty  = RDFS. ConstraintProperty.getNode();
        public static final Node Container  = RDFS.Container.getNode();
        public static final Node ContainerMembershipProperty
                                                         = RDFS.ContainerMembershipProperty.getNode();
        public static final Node Literal = RDFS.Literal.getNode();
        public static final Node Resource = RDFS.Resource.getNode();
        public static final Node comment = RDFS.comment.getNode();
        public static final Node domain = RDFS.domain.getNode();
        public static final Node label = RDFS.label.getNode();
        public static final Node isDefinedBy = RDFS.isDefinedBy.getNode();
        public static final Node range = RDFS.range.getNode();
        public static final Node seeAlso = RDFS.seeAlso.getNode();
        public static final Node subClassOf  = RDFS.subClassOf.getNode();
        public static final Node subPropertyOf  = RDFS.subPropertyOf.getNode();
        public static final Node member  = RDFS.member.getNode();
        }

    /**
        returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI() {
        return uri;
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
 *
 * RDFS.java
 *
 * Created on 28 July 2000, 18:13
 */
