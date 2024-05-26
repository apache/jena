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

package org.apache.jena.rdfpatch.changes;

/** Text format code (start of line) */
public class PatchCodes {
    // An enum does not help.
    //
    // In the reader we need to do string -> dispatch in a string-switch statement.
    // In the writer, we need the label to output.

    public static final String HEADER     = "H";

    public static final String ADD_DATA   = "A";
    public static final String DEL_DATA   = "D";

    public static final String ADD_PREFIX = "PA";
    public static final String DEL_PREFIX = "PD";

    public static final String TXN_BEGIN  = "TX";
    public static final String TXN_COMMIT = "TC";
    public static final String TXN_ABORT  = "TA";

    public static final String SEGMENT    = "Z";

    /** Test whether the string is a known patch code */
    public static boolean isValid(String str) {
        switch(str) {
            case HEADER:
            case ADD_DATA: case DEL_DATA:
            case ADD_PREFIX: case DEL_PREFIX:
            case TXN_BEGIN: case TXN_COMMIT: case TXN_ABORT:
            case SEGMENT:
                return true;
            default:
                return false;
        }
    }
}
