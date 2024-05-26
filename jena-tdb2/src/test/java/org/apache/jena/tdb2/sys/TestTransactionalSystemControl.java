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

package org.apache.jena.tdb2.sys;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class TestTransactionalSystemControl {

    // In case of deadlock.
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    @Test
    public void exclusiveMode1() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        DatasetGraphSwitchable dsgx = TDBInternal.getDatabaseContainer(dsg);
        dsg.executeRead(()->{});
        dsgx.execExclusive(()->{});
        dsg.executeRead(()->{});
    }

    @Test
    public void exclusiveMode2() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        DatasetGraphSwitchable dsgx = TDBInternal.getDatabaseContainer(dsg);
        dsg.executeWrite(()->{});
        dsgx.execExclusive(()->{});
        dsg.executeWrite(()->{});
    }
}
