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

package org.apache.jena.sparql.modify;

import org.apache.jena.sparql.modify.request.QuadDataAccSink ;
import org.apache.jena.update.Update ;

/**
 * Adds using clauses from the UsingList to UpdateWithUsing operations; will throw an
 * UpdateException if the modify operation already contains a using clause.
 */
public class UsingUpdateSink implements UpdateSink {
    private final UpdateSink sink;
    private final UsingList usingList;

    public UsingUpdateSink(UpdateSink sink, UsingList usingList) {
        this.sink = sink;
        this.usingList = usingList;
    }

    @Override
    public void send(Update update) {
        // ---- check USING/USING NAMED/WITH not used.
        // ---- update request to have USING/USING NAMED
        if ( null != usingList && usingList.usingIsPresent() )
            update = UsingList.modifyUpdateForUsingList(update, usingList);
        sink.send(update);
    }

    @Override
    public QuadDataAccSink createInsertDataSink() {
        return sink.createInsertDataSink();
    }

    @Override
    public QuadDataAccSink createDeleteDataSink() {
        return sink.createDeleteDataSink();
    }

    @Override
    public void flush() {
        sink.flush();
    }

    @Override
    public void close() {
        sink.close();
    }
}
