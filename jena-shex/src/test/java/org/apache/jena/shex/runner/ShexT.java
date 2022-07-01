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

package org.apache.jena.shex.runner;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/** Shex test vocabulary. */
public class ShexT {
    /*
--------------------
| p                |
====================
| sht:shape        | **
| sht:data         | **
| sht:schema       | **
| sht:focus        | **
| sht:trait        |
| sht:map          |
| sht:shapeExterns |
| sht:semActs      |
--------------------

-------------------------
| classes               |
=========================
| sht:ValidationTest    |
| sht:ValidationFailure |
-------------------------

Also class:
    sht:RepresentationTest
    and sx:

     */

    public static final String BASE_URI = "http://www.w3.org/ns/shacl/test-suite";

    public static final String NS = BASE_URI + "#";

    public final static Resource cValidationTest        = ResourceFactory.createResource(NS + "ValidationTest");
    public final static Resource cValidationFailure     = ResourceFactory.createResource(NS + "ValidationFailure");
    public final static Resource cRepresentationTest    = ResourceFactory.createResource(NS + "RepresentationTest");

    public final static Property shape = ResourceFactory.createProperty(NS + "shape");
    public final static Property data = ResourceFactory.createProperty(NS + "data");
    public final static Property schema = ResourceFactory.createProperty(NS + "schema");
    public final static Property focus = ResourceFactory.createProperty(NS + "focus");

    public final static Property trait = ResourceFactory.createProperty(NS + "trait");
    public final static Property map = ResourceFactory.createProperty(NS + "map");
    public final static Property shapeExterns = ResourceFactory.createProperty(NS + "shapeExterns");
    public final static Property semActs = ResourceFactory.createProperty(NS + "semActs");

    public static final String NS_SX = "https://shexspec.github.io/shexTest/ns#";
    public final static Property sx_shex = ResourceFactory.createProperty(NS_SX + "shex");
    public final static Property sx_json = ResourceFactory.createProperty(NS_SX + "json");
    public final static Property sx_ttl  = ResourceFactory.createProperty(NS_SX + "ttl");

}
