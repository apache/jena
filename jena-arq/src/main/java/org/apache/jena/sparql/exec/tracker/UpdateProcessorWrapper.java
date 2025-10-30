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

package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateProcessorWrapper<T extends UpdateProcessor>
    implements UpdateProcessor
{
    private T delegate;

    public UpdateProcessorWrapper(T delegate) {
        super();
        this.delegate = delegate;
    }

    protected T getDelegate() {
        return delegate;
    }

    @Override
    public UpdateRequest getUpdateRequest() {
        return getDelegate().getUpdateRequest();
    }

    @Override
    public String getUpdateRequestString() {
        return getDelegate().getUpdateRequestString();
    }

    @Override
    public void execute() {
        getDelegate().execute();
    }
}
