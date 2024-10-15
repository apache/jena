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

package org.apache.jena.rfc3986;

import java.util.function.Consumer;

/**
 * An Error handler captures the policy for dealing with warnings, errors and
 * fatal errors. Fatal errors mean termination of processing and must throw
 * an exception. Errors and warnings may throw an exception to terminate
 * processing or may return after, for example, logging a message. The exact
 * policy is determined the error handler itself.
 * <p>
 * IRI parsing does not throw errors except for bad syntax.
 * Scheme specific issues are "violations" carried by the {@link IRI3986} object
 * and accessed with {@link IRI3986#forEachViolation}.
 * <p>
 * Use with {@code SystemIRI3986.toHandler}.
 */
public interface ErrorHandler
{
    /**
     * Create an error handler with two functions, one for warning, one for error.
     * A value of null implies using a "no action" function.
     */
    public static ErrorHandler create(Consumer<String> onError, Consumer<String> onWarning) {
        return ErrorHandlerBase.create(onError, onWarning);
    }

    /** Report a warning. This method may return. */
    public void warning(String message);

    /** Report an error : should not return. */
    public void error(String message) ;
}
