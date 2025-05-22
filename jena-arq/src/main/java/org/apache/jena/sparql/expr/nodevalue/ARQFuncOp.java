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

package org.apache.jena.sparql.expr.nodevalue;

import java.util.Optional;

import org.apache.jena.atlas.lib.Version;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.NodeValue;

/** Functions specific to ARQ */
public class ARQFuncOp {

    /** The return version information as a human-readable string.*/
    public static NodeValue version() {
        String verStr = versionString();
        return NodeValue.makeString(verStr);
    }

    private static String versionString() {
        if ( true )
            return ARQ.NAME+" "+ARQ.VERSION;
        Optional<String> version = Version.versionForClass(ARQ.class);
        if ( version.isPresent() )
            return String.format("Apache Jena version %s", version.get());

        return "Apache Jena";
    }
}
