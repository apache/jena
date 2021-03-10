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

import static org.junit.Assert.assertTrue;

import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

/** Check that the code has the production settings. */
public class TestSettings {

    static { JenaSystem.init(); }

    @Test
    public void setting_RDFstar_fastpath() {
        assertTrue(org.apache.jena.sparql.engine.iterator.RX.DATAPATH);
    }

    @Test
    public void setting_RDFstar_fastpath_tdb1() {
        assertTrue(org.apache.jena.tdb.solver.SolverRX.DATAPATH);
    }

    @Test
    public void setting_RDFstar_fastpath_tdb2() {
        assertTrue(org.apache.jena.tdb2.solver.SolverRX.DATAPATH);
    }
}
