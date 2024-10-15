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

/**
 * Handling parse errors.
 * Parse errors must be exceptions (parsing stops).
 */
/*package*/ class ParseErrorIRI3986 {

    /*package*/ static IRIParseException parseError(CharSequence source, String s) {
        return parseError(source, -1, s);
    }

    /*package*/ static IRIParseException parseError(CharSequence source, int posn, String s) {
        return parseException(source, SystemIRI3986.formatMsg(source, posn, s));
    }

    private static IRIParseException parseException(CharSequence iriStr, String s) {
        return new IRIParseException(iriStr, s);
    }
}
