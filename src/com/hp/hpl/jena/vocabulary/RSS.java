/*
  (c) Copyright 2000, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: RSS.java,v 1.9 2005-02-21 12:21:35 andy_seaborne Exp $
*/

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
    The standard RSS vocavulary.
    @author  bwm + kers
    @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.9 $' Date='$Date: 2005-02-21 12:21:35 $'
*/
public class RSS extends Object {

    protected static final String uri = "http://purl.org/rss/1.0/";

    /** returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI()
        { return uri; }

    public static final Resource channel = ResourceFactory.createResource( uri + "channel" );
    public static final Resource item = ResourceFactory.createResource( uri + "item" );

    public static final Property description = ResourceFactory.createProperty( uri, "description" );
    public static final Property image = ResourceFactory.createProperty( uri, "image" );
    public static final Property items = ResourceFactory.createProperty( uri, "items" );
    public static final Property link = ResourceFactory.createProperty( uri, "link" );
    public static final Property name = ResourceFactory.createProperty( uri, "name" );
    public static final Property textinput = ResourceFactory.createProperty( uri, "textinput" );
    public static final Property title = ResourceFactory.createProperty( uri, "title" );
    public static final Property url = ResourceFactory.createProperty( uri, "url" );

}

/*
 *  (c) Copyright 2000, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 * RSS.java
 *
 * Created on 31 August 2000, 15:41
 */