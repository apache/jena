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

package org.apache.jena.test;

import org.apache.jena.test.assembler.TS_Assembler;
import org.apache.jena.test.general.TestRemoteEndToEnd;
import org.apache.jena.test.integration.TS_Integration;
import org.apache.jena.test.rdfconnection.TS_RDFConnectionIntegration;
import org.apache.jena.test.service.TS_SPARQLService;
import org.apache.jena.test.txn.TS_TranactionIntegration;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_Integration.class
    , TS_TranactionIntegration.class
    , TS_RDFConnectionIntegration.class
    , TS_Assembler.class
    , TestSettings.class
    , TestRemoteEndToEnd.class
    , TS_SPARQLService.class
})

public class TC_Integration { }

