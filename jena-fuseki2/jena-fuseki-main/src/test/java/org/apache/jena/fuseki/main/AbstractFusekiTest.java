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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class AbstractFusekiTest extends BaseFusekiTest {
    @BeforeClass public static void startServer() {

//      FusekiLogging.setLogging();
//      LogCtl.enable(Fuseki.actionLog);
//      LogCtl.enable(Fuseki.serverLog);

      server = FusekiServer.create()
              .port(0)
              //.verbose(true)
              .add(datasetPath(), dsgTesting)
              .enablePing(true)
              .enableMetrics(true)
              .build();
      server.start();
      port = server.getPort();
      serverURL = "http://localhost:"+port+"/";
  }

  @AfterClass public static void stopServer() {
      try {
          if ( server != null )
              server.stop();
      } catch (Throwable th) {
          th.printStackTrace();
      }
  }


  @Before public void beforeTest() { resetDatabase(); }
  @After public void afterTest() {}

}
