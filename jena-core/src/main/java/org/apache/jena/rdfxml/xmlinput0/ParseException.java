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

package org.apache.jena.rdfxml.xmlinput0;

import org.apache.jena.rdfxml.xmlinput0.impl.ARPLocation;

/**
 * An exception during the RDF processing of ARP. Note: it is distinguished from
 * an XML related exception from Xerces because while both are
 * SAXParseException's, the latter are not {@link ParseException}'s.
 */
public class ParseException extends org.apache.jena.rdfxml.xmlinput1.ParseException {

    protected ParseException(int id, ARPLocation where, String msg) {
        super(id, where.inputName, where.endLine, where.endColumn, msg);
    }

    public ParseException(int id, ARPLocation where, Exception e) {
        super(id, where.inputName, where.endLine, where.endColumn, e);
    }
}
