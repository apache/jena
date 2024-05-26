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

package org.apache.jena.fuseki.servlets;

import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.Endpoint;

/**
 * A NoOp implementation of {@ActionService}.
 * <p>
 * This is only for use as a placeholder during configuration. It must be replaced in
 * an {@link Endpoint} during server building with another implementation.
 * </p>
 */
public class ActionServicePlaceholder extends ActionService {

    @Override
    public void validate(HttpAction action) {
        throw new FusekiException("Call to ActionServicePlaceholder");
    }
    @Override
    public void execute(HttpAction action) {
        throw new FusekiException("Call to ActionServicePlaceholder");
    }
}
