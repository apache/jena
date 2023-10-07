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

package org.apache.jena.riot.lang.rdfxml;

import javax.xml.stream.XMLInputFactory;

import org.apache.jena.util.JenaXMLInput;

/**
 * Common code across RRX parsers. This class is not public API
 *
 * @see JenaXMLInput
 */
public class SysRRX {

    public static XMLInputFactory createXMLInputFactory() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        JenaXMLInput.initXMLInputFactory(xmlInputFactory);
        // Additional features. Enable character entity support.
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        return xmlInputFactory;
    }

}
