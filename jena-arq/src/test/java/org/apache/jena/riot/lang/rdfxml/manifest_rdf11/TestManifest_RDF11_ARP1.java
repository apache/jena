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

package org.apache.jena.riot.lang.rdfxml.manifest_rdf11;

import org.apache.jena.arq.junit.manifest.Manifests;
import org.apache.jena.arq.junit.runners.Label;
import org.apache.jena.arq.junit.runners.RunnerRIOT;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.ReaderRIOTFactory;
import org.apache.jena.riot.lang.ReaderRIOTRDFXML1;
import org.apache.jena.sys.JenaSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(RunnerRIOT.class)
@Label("ARP1 RDF 1.1 rdf11-xml")
@Manifests({
    "testing/RIOT/rdf11-xml/manifest.ttl"
})

public class TestManifest_RDF11_ARP1 {
    private static ReaderRIOTFactory systemReaderfactory;

    @BeforeClass
    public static void beforeClass() {
        JenaSystem.init();;
        systemReaderfactory = RDFParserRegistry.getFactory(Lang.RDFXML);
        RDFParserRegistry.registerLangTriples(Lang.RDFXML, ReaderRIOTRDFXML1.factory);
    }

    @AfterClass
    public static void afterClass() {
        if ( systemReaderfactory != null )
            RDFParserRegistry.registerLangTriples(Lang.RDFXML, systemReaderfactory);
    }
}
