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

package org.apache.jena.sparql.modify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;

public class TestUpdateBuild {

    static String insertData =  "INSERT DATA { <http://example/s> <http://example/p> 123 . }";
    static UpdateRequest update = UpdateFactory.create(insertData);

    @Test public void update_build_01() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        UpdateExec.newBuilder()
            .dataset(dsg)
            .update(update)
            .build()
            .execute();
        assertFalse(dsg.getDefaultGraph().isEmpty());
    }

    @Test public void update_build_02() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        String insertStr =  "INSERT { <http://example/s> <http://example/p> ?x } WHERE { }";
        UpdateRequest update = UpdateFactory.create(insertStr);

        Binding binding = SSE.parseBinding("(binding (?x 123))");
        UpdateExec.newBuilder()
            .dataset(dsg)
            .update(update)
            .substitution(binding)
            .build()
            .execute();
        assertFalse(dsg.getDefaultGraph().isEmpty());
    }

    @Test public void update_build_03() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        String insertStr =  "INSERT { <http://example/s> <http://example/p> 123 } WHERE { FILTER(?x = 456) }";
        UpdateRequest update = UpdateFactory.create(insertStr);

        Binding binding = SSE.parseBinding("(binding (?x 456))");
        UpdateExec.newBuilder()
            .dataset(dsg)
            .update(update)
            .substitution(binding)
            .build()
            .execute();
        assertFalse(dsg.getDefaultGraph().isEmpty());
    }

    @Test public void update_build_execute_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        assertTrue(dsg.isEmpty());
        dsg.execute(()->{
            UpdateExec.newBuilder()
                .dataset(dsg)
                .update("INSERT DATA { <x:s> <x:p> <x:o> }")
                .execute();
        });
        assertFalse(dsg.isEmpty());
    }

    @Test public void update_build_execute_2() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        assertTrue(dsg.isEmpty());
        dsg.execute(()->{
            UpdateExec.newBuilder()
                .dataset(dsg)
                .update("INSERT DATA { <x:s> <x:p> <x:o1> }")
                .update("INSERT DATA { <x:s> <x:p> <x:o2> }")
                .execute();
        });
        assertEquals(2, Iter.count(dsg.find()));
    }
}
