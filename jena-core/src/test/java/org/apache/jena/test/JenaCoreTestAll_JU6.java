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

package org.apache.jena.test;

import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.core_ttl.tests.TS6_TestTurtle;
import org.apache.jena.datatypes.TS6_dt;
import org.apache.jena.irix.TS6_IRIx2;
import org.apache.jena.langtagx.TS6_LangTagX;
import org.apache.jena.mem.TS6_GraphMem;
import org.apache.jena.memvalue.TS6_GraphMemValue;
import org.apache.jena.rdfxml.xmloutput.TS6_xmloutput;
import org.apache.jena.shared.TS6_SharedPackage;
import org.apache.jena.util.TS6_coreutil;
import org.apache.jena.util.iterator.TS6_coreiter;
import org.apache.jena.vocabulary.TS6_Vocabularies;

@Suite
@SelectClasses({
    TestSystemSetup.class,

    // Same order as JenaCoreTestAll_JU4
    // There should be a corresponding line commented out in JenaCoreTestAll_JU4
    TS6_IRIx2.class,
    TS6_LangTagX.class,
    TS6_dt.class,

    TS6_GraphMem.class,
    TS6_GraphMemValue.class,

    TS6_xmloutput.class,

    TS6_coreutil.class,
    TS6_coreiter.class,

    TS6_Vocabularies.class,
    TS6_SharedPackage.class,

    TS6_TestTurtle.class,

})

public class JenaCoreTestAll_JU6 {
    @BeforeSuite public static void beforeSuite() {
        JenaTestLib.setup();
    }
}
