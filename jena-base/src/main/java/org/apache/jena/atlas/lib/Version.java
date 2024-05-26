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

package org.apache.jena.atlas.lib;

import java.io.PrintStream;
import java.util.Optional;

/** In support of version information. */
public class Version {

    private Version() {}

    /**
     * Return the version of a class.
     * <p>
     * This depends on the class being in a jar with a
     * manifest that has the version field.
     * <p>
     * Otherwise return {@link Optional#empty()}.
     */
    public static Optional<String> versionForClass(Class<?> cls) {
        String x = cls.getPackage().getImplementationVersion();
        return Optional.ofNullable(x);
    }

    public static void printVersion(PrintStream out, String system, Optional<String> versionString) {
        printVersion(out, system, versionString.orElse("<development>"));
    }

    public static void printVersion(PrintStream out, String system, String versionString) {
        if ( system == null )
            out.printf("Apache Jena version %s\n", versionString);
        else
            out.printf("Apache Jena %s version %s\n", system, versionString);
    }
}
