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

package org.apache.jena.fuseki.main;

import org.apache.jena.fuseki.main.sys.TestFusekiModules;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

  TestPlainServer.class

  // This tests modules and modifies the system state.
  , TestFusekiModules.class
  , TestMultipleEmbedded.class
  , TestFusekiCustomOperation.class
  , TestFusekiMainCmd.class
  , TestFusekiStdSetup.class
  , TestFusekiStdReadOnlySetup.class
  , TestConfigFile.class
  , TestFusekiServerBuild.class
  , TestFusekiDatasetSharing.class

  , TestFileUpload.class
  , TestAuthQuery_JDK.class
  , TestAuthUpdate_JDK.class
  , TestHttpOperations.class
  , TestHttpOptions.class

  , TestQuery.class
  , TestSPARQLProtocol.class

  , TestPatchFuseki.class
  , TestFusekiCustomScriptFunc.class

  // Test ping.
  , TestMetrics.class
  , TestFusekiShaclValidation.class
})
public class TS_FusekiMain {}

