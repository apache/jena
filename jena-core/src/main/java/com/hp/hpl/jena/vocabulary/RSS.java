/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
    The standard RSS vocavulary.
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
