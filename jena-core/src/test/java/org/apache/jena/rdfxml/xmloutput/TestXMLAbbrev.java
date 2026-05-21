/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdfxml.xmloutput;

import java.io.IOException;

public class TestXMLAbbrev extends BaseTestXMLOutput
{
    @Override
    protected String getLang() {
        return  "RDF/XML-ABBREV";
    }

    public void testNoPropAttr() throws IOException
    {
        checkY(BaseTestXMLFeatures.file1,
              null,
              "prop1=",
              Change.blockRules( "propertyAttr" )
                );
    }

    public void testNoRdfCollection() throws IOException
    {
        checkY("testing/abbreviated/collection.rdf",
              null,
              "[\"']Collection[\"']",
              Change.blockRules( "parseTypeCollectionPropertyElt" )
                );
    }

    public void testNoLi() throws IOException
    {
        checkY("testing/abbreviated/container.rdf",
              null,
              "rdf:li",
              Change.blockRules( "section-List-Expand" )
                );
    }

    public void testNoID() throws IOException
    {
        checkB("testing/abbreviated/container.rdf",
               "rdf:ID",
               Change.blockRules( "idAttr" ),
               "http://example.org/foo"
                );
    }

    public void testNoID2() throws IOException
    {
        checkB("testing/abbreviated/container.rdf",
               "rdf:ID",
               Change.blockRules( "idAttr" ),
               "http://example.org/foo#"
                );
    }

    public void testNoID3() throws IOException {
        // Minimal version of testNoID2
        checkB("testing/abbreviated/rdf-id.rdf",
               "rdf:ID",
               Change.blockRules("idAttr"),
               "http://example.org/foo#"
                );
    }

    public void testNoResource() throws IOException
    {
        checkB("testing/abbreviated/container.rdf",
               "['\"]Resource[\"']",
               Change.blockRules( "parseTypeResourcePropertyElt" ),
               "http://example.org/foo#"
                );
    }

    public void testPropAttrs() throws IOException
    {
        checkY("testing/abbreviated/namespaces.rdf",
              ":prop0 *=",
              null,
              Change.blockRules( ""  )
                );
    }

    public void testNoPropAttrs() throws IOException
    {
        checkY("testing/abbreviated/namespaces.rdf",
              null,
              ":prop0 *=",
              Change.none()
                );
    }

    public void testNoReification() throws IOException
    {
        // System.err.println("WARNING: reification output tests suppressed.");
        String filename = "testing/abbreviated/reification.rdf";
        String base = "http://example.org/foo";
        /* * Heisenbug, reification prettiness sometimes fails. * /
         check(filename,null,null,"rdf:subject",false,new Change(){
                    public void code(RDFWriter w){}
                },base);
        /* */
        checkZ(filename,
              null,
              "rdf:subject",
              null,
              false,
              Change.blockRules( "section-Reification" ),
              base
                );
    }


    public void testNoCookUp() throws IOException
    {
        checkY("testing/abbreviated/cookup.rdf",
              null,
              "(j\\.fixup|j\\.cook\\.up)",
              Change.blockRules( "" )
                );
    }
}
