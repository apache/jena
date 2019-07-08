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

import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.fuseki.async.AsyncPool;
import org.apache.jena.fuseki.async.AsyncTask;
import org.apache.jena.fuseki.servlets.HttpAction;

/** Base helper class for creating async tasks on "items", based on POST  */
public abstract class ActionAsyncTask extends ActionItem
{
    private String name;

    public ActionAsyncTask(String name) {
        super();
        this.name = name;
    }

    @Override
    final
    protected JsonValue execGetItem(HttpAction action) {
        throw new InternalErrorException("GET for AsyncTask -- Should not be here!");
    }

    @Override
    final
    protected JsonValue execPostItem(HttpAction action) {
        Runnable task = createRunnable(action);
        AsyncTask aTask = Async.execASyncTask(action, AsyncPool.get(), name, task);
        Async.setLocationHeader(action, aTask);
        return Async.asJson(aTask);
    }

    protected abstract Runnable createRunnable(HttpAction action);
}

