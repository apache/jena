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

package org.apache.jena.update;

import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;

/** An instance of a execution of an UpdateRequest */
public interface UpdateExecution extends UpdateProcessor
{
    public static UpdateExecutionDatasetBuilder create() {
        return UpdateExecutionDatasetBuilder.create();
    }

    public static UpdateExecutionDatasetBuilder dataset(Dataset dataset) {
        return create().dataset(dataset);
    }

    public static UpdateExecutionHTTPBuilder service(String serviceURL) {
        return UpdateExecutionHTTP.service(serviceURL);
    }

    /** Execute */
    @Override
    public void execute() ;

}
