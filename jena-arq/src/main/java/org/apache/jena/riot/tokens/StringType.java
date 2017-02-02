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

package org.apache.jena.riot.tokens;

/** The seen form of a {@link TokenType#STRING} */
public enum StringType {
    /** A string delimited by 1 single-quote character : {@code 'abc'} */
    STRING1,
    /** A string delimited by 1 double-quote character : {@code 'abc'} */
    STRING2,
    /** A string delimited by 3 single-quote characters : {@code '''abc'''} */
    LONG_STRING1,
    /** A string delimited by 3 double-quote characters : {@code """abc"""} */
    LONG_STRING2
}
