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

package org.apache.jena.riot.system.irix;

import org.junit.jupiter.api.BeforeAll;

import org.apache.jena.iri3986.provider.IRIProvider3986;
import org.apache.jena.iri3986.provider.JenaSeveritySettings;
import org.apache.jena.irix.IRIProvider;
import org.apache.jena.rfc3986.Violations;

/** Test IRIx in parser usage. */
public class TestIRIxRIOT_iri3986 extends AbstractTestIRIxRIOT_system {

    protected TestIRIxRIOT_iri3986() {
        super("IRI3986");
    }

    private static final IRIProvider testProvider = new IRIProvider3986();

    @Override
    protected IRIProvider getProviderForTest() {
        return testProvider;
    }

    @BeforeAll public static void beforeClass() {
        Violations.setSystemSeverityMap(JenaSeveritySettings.jenaSystemSettings());
    }
}
