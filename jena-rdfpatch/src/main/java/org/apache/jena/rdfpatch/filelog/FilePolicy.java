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

package org.apache.jena.rdfpatch.filelog;

import java.util.Locale;
import java.util.Objects;

import org.apache.jena.rdfpatch.filelog.rotate.FileRotateException;
import org.apache.jena.rdfpatch.filelog.rotate.ManagedOutput;

/** File naming strategies for rotating files. */
public enum FilePolicy {
    /**
     * Date based - "filename-yyyy-mm-dd", with nightly rollover.
     */
    DATE,
    /**
     * Timestamp with explicit roll over by calling {@link ManagedOutput#rotate()}
     * The file format is "filename-yyyy-mm-dd_hh-mm-ss".
     */
    TIMESTAMP,
    /**
     * Files are filename-0001, filename-0002, .. and "rotate" means next index.
     */
    INDEX,
    /**
     * Always write to a file with the base filename. On rotate, the files are shifted up
     * as filename.001, filename.002, ... and the base filename used for a new file.
     */
    SHIFT,
    /**
     * Use a fixed file
     */
    FIXED
    ;

    public static FilePolicy policy(String name) {
        Objects.requireNonNull(name);
        String nameLC = name.toLowerCase(Locale.ROOT);
        switch(nameLC) {
            case "date" :       return DATE;
            case "timestamp" :  return TIMESTAMP;
            case "index" :      return INDEX;
            case "rotate" :     return SHIFT;
            case "shift" :      return SHIFT;
            case "fixed" :      return FIXED;
            case "none" :       return FIXED;
            default:
                throw new FileRotateException("Unknown policy name: "+name);
        }
    }
}
