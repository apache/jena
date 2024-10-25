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

package org.apache.jena.iri3986.provider;

import org.apache.jena.rfc3986.ErrorHandler;
import org.apache.jena.rfc3986.SystemIRI3986;
import org.apache.jena.rfc3986.Violations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize IRI3986 as the system-wide IRIx provider.
 */
public class InitIRI3986 {

    private static Logger LOG = LoggerFactory.getLogger("IRI3986");

    private static boolean initialized = false;

    public static void init() {
        if ( initialized )
            return;
        synchronized(InitIRI3986.class) {
            if ( initialized )
                return;
            initialized = true;
            // Errors and warnings should be handled via exceptions and violations.
            // In case any aren't, set the IRI3986 system error handler, for safety.
            ErrorHandler iri3986errorHandler = ErrorHandler.create(msg->LOG.error(msg), msg->LOG.warn(msg));
            SystemIRI3986.setErrorHandler(iri3986errorHandler);
            // Jena setup.
            Violations.setSystemSeverityMap(JenaSeveritySettings.jenaSystemSettings());
        }
    }
}
