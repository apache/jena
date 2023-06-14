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

package org.apache.jena.sparql.exec.http;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.util.Context;

/**
 * Helper code to enable SERVICE for the duration of a test suite.
 * Change the system setting to allow SERVICE execution, and reset afterwards.
 */
public class CtlService {
    public static Object systemContextAllowedSetting = null;

    /*@BeforeClass*/ public static void enableAllowServiceExecution() {
        systemContextAllowedSetting = Fuseki.getContext().get(ARQ.httpServiceAllowed);
        ARQ.getContext().set(ARQ.httpServiceAllowed, "true");
    }

    /*@AfterClass*/ public static void resetAllowServiceExecution() {
        ARQ.getContext().set(ARQ.httpServiceAllowed, systemContextAllowedSetting);
    }

    /** Minimal context that allows SERVICE execution */
    public static Context minimalContext() {
        return new Context().set(ARQ.httpServiceAllowed, true);
    }

    // ---- Add to a test suite
//    // ---- Enable service
//    @BeforeClass public static void enableAllowServiceExecution() { CtlService.enableAllowServiceExecution(); }
//    @AfterClass public static void resetAllowServiceExecution() { CtlService.resetAllowServiceExecution(); }
//    /** Minimal context that allows SERVICE execution */
//    public static Context minimalContext() { return CtlService.minimalContext() }
//    // ----


}
