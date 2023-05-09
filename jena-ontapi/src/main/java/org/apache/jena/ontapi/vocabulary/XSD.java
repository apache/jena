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

package org.apache.jena.ontapi.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * XML Schema Vocabulary
 * See <a href="http://www.w3.org/2001/XMLSchema">XSD</a>
 */
public class XSD extends org.apache.jena.vocabulary.XSD {

    public static final Property length = property("length");
    public static final Property minLength = property("minLength");
    public static final Property maxLength = property("maxLength");
    public static final Property pattern = property("pattern");
    public static final Property minInclusive = property("minInclusive");
    public static final Property minExclusive = property("minExclusive");
    public static final Property maxInclusive = property("maxInclusive");
    public static final Property maxExclusive = property("maxExclusive");
    public static final Property totalDigits = property("totalDigits");
    public static final Property fractionDigits = property("fractionDigits");

    private static Property property(String name) {
        return ResourceFactory.createProperty(NS + name);
    }
}
