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

package org.apache.jena.sparql.exec.http;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecWrapper;
import org.apache.jena.sparql.exec.tracker.UpdateExecTransform;

public class UpdateExecHTTPWrapper
    extends UpdateExecWrapper<UpdateExec>
    implements UpdateExecHTTP
{
    private final UpdateExecHTTP httpDelegate;

    // Closing the exec delegate is assumed to close the http delegate.
    public static UpdateExecHTTP transform(UpdateExecHTTP qExec, UpdateExecTransform transform) {
        UpdateExecHTTP httpDelegate = qExec;
        UpdateExec execDelegate = qExec;

        // Unwrap an existing wrapper.
        if (qExec instanceof UpdateExecHTTPWrapper wrapper) {
            httpDelegate = wrapper.getHttpDelegate();
            execDelegate = wrapper.getDelegate();
        }

        UpdateExec ue = transform.transform(execDelegate);
        if (ue instanceof UpdateExecHTTP ueh) {
            return ueh;
        }

        return new UpdateExecHTTPWrapper(httpDelegate, ue);
    }

    public UpdateExecHTTPWrapper(UpdateExecHTTP delegate) {
        this(delegate, delegate);
    }

    public UpdateExecHTTPWrapper(UpdateExecHTTP httpDelegate, UpdateExec execDelegate) {
        super(execDelegate);
        this.httpDelegate = httpDelegate;
    }

    protected UpdateExecHTTP getHttpDelegate() {
        return httpDelegate;
    }
}
