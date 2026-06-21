/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.riot.lang.rdfxml.rrx;

import static java.lang.String.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ErrorHandler;

class ErrorHandlerCollector implements ErrorHandler {
    List<String> warnings = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    List<String> fatals = new ArrayList<>();

    @Override
    public void warning(String message, long line, long col) {
        warnings.add(message);
    }

    @Override
    public void error(String message, long line, long col) {
        errors.add(message);
        throw new RiotException(message);
    }

    @Override
    public void fatal(String message, long line, long col) {
        fatals.add(message);
        throw new RiotException(message);
    }

    public boolean anySet() {
        return ! ( warnings.isEmpty() && errors.isEmpty() && fatals.isEmpty() );
    }

    public void reset() {
        warnings.clear();
        errors.clear();
        fatals.clear();
    }

    public String summary() {
        if ( fatals.isEmpty() )
            return format("E:%d W:%d", errors.size(), warnings.size());
        return format("E:%d W:%d F:%d", errors.size(), warnings.size(), fatals.size());
    }

    public void print(PrintStream out) {
        warnings.forEach(s -> out.println("W: " + s));
        errors.forEach(s -> out.println("E: " + s));
        fatals.forEach(s -> out.println("F: " + s));
    }
}