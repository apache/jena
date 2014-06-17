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

package org.apache.jena.iri.impl;

import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class BuildViolationCodes {
    public static void main(String[] args) throws TransformerException, TransformerFactoryConfigurationError {
        Transformer xsl =
        TransformerFactory.newInstance().newTransformer(
                new StreamSource(new File("src/main/xml/org/apache/jena/iri/impl/viol2java.xsl"))
                );
        xsl.transform(
                new StreamSource(new File("src/main/xml/org/apache/jena/iri/impl/violations.xml")),
                new StreamResult(new File("src/main/java/org/apache/jena/iri/ViolationCodes.java"))
                        
        );
    }
}
