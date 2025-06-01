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

package org.apache.jena.irix;

import org.apache.jena.iri3986.provider.InitIRI3986;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test of IRIx for the system default IRIProvider.
 * This forms the contract for IRIPRoviders.
 *
 * @See {@code org.apache.jena.rfc3986.TS_iri3986} for detailed test of jena-iri3986.
 */

//JUnit5. Does not mix with JUnit3. So until jena-core updates to JUnit 4 or 5 ...
//@Suite
//@SelectClasses({
@RunWith(Suite.class)
@Suite.SuiteClasses( {
    // RFC3986 syntax only
    TestIRIxSyntaxRFC3986.class,

    // Contract for Jena, including schema violations.
    TestIRIxJenaSystem.class,

    // Operations on IRIx
    TestIRIxAbsoluteRelative.class,
    TestIRIxNormalize.class,
    TestIRIxReference.class,
    TestIRIxRelative.class,
    TestIRIxRelativize.class,
    TestIRIxResolve.class,

    // odds and ends that come up
    TestIRIxOther.class
} )
public class TS_IRIx {
    static {
        SystemIRIx.init();
        InitIRI3986.init();
    }
}
