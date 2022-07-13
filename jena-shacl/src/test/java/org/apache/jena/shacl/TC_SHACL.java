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

package org.apache.jena.shacl;

import org.apache.jena.shacl.compact.TS_Compact;
import org.apache.jena.shacl.tests.TestImports;
import org.apache.jena.shacl.tests.TestValidationReport;
import org.apache.jena.shacl.tests.ValidationListenerTests;
import org.apache.jena.shacl.tests.jena_shacl.TS_JenaShacl;
import org.apache.jena.shacl.tests.std.TS_StdSHACL;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestValidationReport.class
    , TS_StdSHACL.class
    , TS_JenaShacl.class
    , TS_Compact.class
    , TestImports.class
    , ValidationListenerTests.class
} )

public class TC_SHACL { }
