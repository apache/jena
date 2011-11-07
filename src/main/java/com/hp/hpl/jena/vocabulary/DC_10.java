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
