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

package org.apache.jena.riot.system;

import java.util.Objects;

/** Entry in a prefix map. */
public class PrefixEntry {
    // Custom interface to get more appropriate names.

    public static PrefixEntry create(String prefix, String uri) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(uri);
        return new PrefixEntry(prefix, uri);
    }
    private final String prefix;
    private final String uri;

    private PrefixEntry(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "PrefixEntry["+prefix+": <"+uri+">]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, uri);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        PrefixEntry other = (PrefixEntry)obj;
        return Objects.equals(prefix, other.prefix) && Objects.equals(uri, other.uri);
    }
}
