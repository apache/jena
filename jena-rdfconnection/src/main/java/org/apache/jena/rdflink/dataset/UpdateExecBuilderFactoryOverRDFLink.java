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

package org.apache.jena.rdflink.dataset;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilderFactory;
import org.apache.jena.sparql.util.Context;

public class UpdateExecBuilderFactoryOverRDFLink implements UpdateExecBuilderFactory {
    private static final UpdateExecBuilderFactoryOverRDFLink INSTANCE = new UpdateExecBuilderFactoryOverRDFLink();
    public static UpdateExecBuilderFactoryOverRDFLink get() { return INSTANCE; }

    private UpdateExecBuilderFactoryOverRDFLink() {}

    @Override
    public boolean accept(DatasetGraph dataset, Context context) {
        return dataset instanceof DatasetGraphOverRDFLink;
    }

    @Override
    public UpdateExecBuilder create(DatasetGraph dataset, Context context) {
        DatasetGraphOverRDFLink d = (DatasetGraphOverRDFLink)dataset;
        return new UpdateExecBuilderOverRDFLink(new RDFLinkCreatorAdapter(d), dataset).context(context);
    }
}
