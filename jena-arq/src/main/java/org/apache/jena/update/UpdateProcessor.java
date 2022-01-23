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

import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.exec.UpdateExecDatasetBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecHTTPBuilder;
import org.apache.jena.sparql.util.Context ;

/**
 * An instance of a execution of an UpdateRequest.
 * Applies to UpdateExec (GPI) and UpdateExecution (API).
 */
public interface UpdateProcessor
{
    /**
     * The properties associated with a query execution -
     * implementation specific parameters  This includes
     * Java objects (so it is not an RDF graph).
     * Keys should be URIs as strings.
     * May be null (this implementation does not provide any configuration).
     * @deprecated UpdateProcessors are now built with the builder {@link UpdateExecDatasetBuilder} and {@link UpdateExecHTTPBuilder}.
     */
    @Deprecated
    public default Context getContext() { return null; }

    /**
     * The dataset against which the query will execute.
     * May be null, implying the there isn't a local GraphStore target for this UpdateProcessor.
     * @deprecated
     */
    @Deprecated
    public default DatasetGraph getDatasetGraph() { return null; }

    /** Execute */
    public void execute() ;
}
