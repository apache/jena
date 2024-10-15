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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This error handler is a pair of functions, one for warnings, one for errors.
 */
public class ErrorHandlerBase implements ErrorHandler {

    private final Consumer<String> onError;
    private final Consumer<String> onWarning;

    /**
     * Create an error handler.
     */
    public static ErrorHandler create(Consumer<String> onError, Consumer<String> onWarning) {
        return new ErrorHandlerBase(handler(onError), handler(onWarning));
    }

    private ErrorHandlerBase(Consumer<String> onError, Consumer<String> onWarning) {
        this.onError = Objects.requireNonNull(onError, "onError");
        this.onWarning = Objects.requireNonNull(onWarning, "onWarning");
    }

    /** Ensure consumer is defined : default null to "do nothing" */
    private static Consumer<String> handler(Consumer<String> eventHandler) {
        return eventHandler == null ? (x)->{} : eventHandler;
    }

    @Override
    public void warning(String message) { onWarning.accept(message); }

    @Override
    public void error(String message) { onError.accept(message); }
}