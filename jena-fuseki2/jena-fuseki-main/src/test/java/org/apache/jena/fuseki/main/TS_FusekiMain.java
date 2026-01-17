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

package org.apache.jena.fuseki.main;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.fuseki.main.prefixes.TS_PrefixesService;
import org.apache.jena.fuseki.main.runner.TestFusekiSetupInternal;
import org.apache.jena.fuseki.main.sys.TestFusekiModules;

@Suite
@SelectClasses({
  TestPlainServer.class

  // This tests modules and modifies the system state.
  , TestFusekiModules.class

  , TestMultipleEmbedded.class
  , TestFusekiSetupInternal.class
  , TestFusekiStart.class
  , TestFusekiCustomOperation.class
  , TestFusekiCmdLineArgs.class
  , TestFusekiMainCmdArguments.class
  , TestFusekiMainCmdCustomArguments.class
  , TestFusekiStdSetup.class
  , TestFusekiStdReadOnlySetup.class
  , TestConfigFile.class
  , TestCrossOriginFilter.class
  , TestFusekiServerBuild.class
  , TestFusekiDatasetSharing.class

  , TestFileUpload.class
  , TestAuthQuery.class
  , TestAuthUpdate.class
  , TestHttpOperations.class
  , TestHttpOptions.class
  , TestQuery.class
  , TestSPARQLProtocol.class

  , TestPatchFuseki.class
  , TestFusekiCustomScriptFunc.class

  , TS_PrefixesService.class
  , TestMetrics.class
  , TestFusekiShaclValidation.class
  
  // Temporary independent test due for tarcking failures.
  , TestFusekiShaclValidation2.class

})
public class TS_FusekiMain {}

