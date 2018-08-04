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

package org.apache.jena.fuseki.ctl;

import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Transactional ;

/** Base of async tasks - this caries some useful information around, leaving the
 * implementation of Callable.run() to the specific task.
 */
abstract class TaskBase implements Runnable {
    public final long actionId ;
    public final DatasetGraph dataset ;
    public final String datasetName ;
    public final Transactional transactional ;
    
    protected TaskBase(HttpAction action) {
        // The action is closed as part of action processing so is not
        // available in the async task. Anything from it that is needed,
        // taken out here.
        this.actionId = action.id ;
        this.dataset = action.getDataset() ;
        this.transactional = action.getTransactional() ; 
        this.datasetName = action.getDatasetName() ;
    }
}
