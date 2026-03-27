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

package org.apache.jena.sparql.exec.tracker.core;

import java.util.Optional;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.dispatch.ChainingUpdateDispatcher;
import org.apache.jena.sparql.engine.dispatch.UpdateDispatcher;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class ChainingUpdateExecBuilderFactoryExecTracker
    implements ChainingUpdateDispatcher
{
    @Override
    public UpdateExec create(UpdateRequest updateRequest, DatasetGraph dataset, Context context, UpdateDispatcher chain) {
        UpdateExec delegateExec = chain.create(updateRequest, dataset, context);

        String updateStr = Optional.ofNullable(delegateExec.getUpdateRequest()).map(Object::toString)
                .orElse(delegateExec.getUpdateRequestString());
        return new UpdateExecWrapper<>(delegateExec) {
            @Override
            public void execute() {
                System.out.println("Update started: " + updateStr);
                try {
                    super.execute();
                } catch (Exception e) {
                    System.out.println("Update completed WITH EXCEPTION: " + updateStr + " " + e);
                    throw e;
                }
                System.out.println("Update completed successfully: " + updateStr);
            }
        };
    }
}
