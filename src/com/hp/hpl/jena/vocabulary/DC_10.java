/*
 * (c) Copyright 2000, 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

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

    private static Property cp( String ln )
        { return ResourceFactory.createProperty( uri, ln ); }

    public static final Property contributor = cp( "contributor" );
    public static final Property coverage = cp( "coverage" );
    public static final Property creator = cp( "creator" );
    public static final Property date = cp( "date" );
    public static final Property description = cp( "description" );
    public static final Property format = cp( "format" );
    public static final Property identifier = cp( "identifier" );
    public static final Property language = cp( "language" );
    public static final Property publisher = cp( "publisher" );
    public static final Property relation = cp( "relation" );
    public static final Property rights = cp( "rights" );
    public static final Property source = cp( "source" );
    public static final Property subject = cp( "subject" );
    public static final Property title = cp( "title" );
    public static final Property type = cp( "type" );
}




/*
 *  (c) Copyright 2000, 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
