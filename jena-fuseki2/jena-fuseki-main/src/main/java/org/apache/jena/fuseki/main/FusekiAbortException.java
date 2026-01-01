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
 *
 *     SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main;

import org.apache.jena.fuseki.FusekiException;

/** Crash out of a FusekiServer. Used for unrecoverable errors. */
public class FusekiAbortException extends FusekiException {

    private final int exitCode;

    public FusekiAbortException(int exitCode, String msg, Throwable cause) {
        super(msg, cause);
        this.exitCode = exitCode;
    }

    public FusekiAbortException(int exitCode, String msg) {
        super(msg);
        this.exitCode = exitCode;
    }

    public FusekiAbortException(int exitCode, Throwable cause) {
        super(cause);
        this.exitCode = exitCode;
    }

    public FusekiAbortException(int exitCode) {
        super();
        this.exitCode = exitCode;
    }

    public int getCode() {
        return exitCode;
    }
}
