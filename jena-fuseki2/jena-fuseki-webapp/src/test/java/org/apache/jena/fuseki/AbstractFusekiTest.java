/**
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

package org.apache.jena.fuseki;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * This package has the Fuseki full server tests.  ServerCtl manages a full server for testing.
 *
 * Tests on the server also test the client-side operation
 * It is testing various client APIs.
 *
 * jena-integration-tests tests RDFConnection with the basic Fuseki server and
 * RDFConnection calls the different client APIs. So both servers get tested with the
 * client APIs, by different means.
 *
 * See {@link org.apache.jena.fuseki.AbstractFusekiTest}.
 *
 * Framework for tests using client-side operation onto a forked Fuseki server. Not
 * general - some test sets set up their own environment for different, additional
 * requirements.
 */

public class AbstractFusekiTest
{
    @BeforeClass public static void ctlBeforeClass() { ServerCtl.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { ServerCtl.ctlAfterClass(); }
    @Before      public void ctlBeforeTest()         { ServerCtl.ctlBeforeTest(); }
    @After       public void ctlAfterTest()          { ServerCtl.ctlAfterTest(); }
}

