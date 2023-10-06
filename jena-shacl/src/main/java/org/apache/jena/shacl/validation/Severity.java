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

package org.apache.jena.shacl.validation;

import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.vocabulary.SHACL;

public class Severity {
    // Section 2.1.4
    //   sh:Info         A non-critical constraint violation indicating an informative message.
    //   sh:Warning      A non-critical constraint violation indicating a warning.
    //   sh:Violation    A constraint violation.
    public static Severity Info = new Severity(SHACL.Info);
    public static Severity Warning = new Severity(SHACL.Warning);
    public static Severity Violation = new Severity(SHACL.Violation);

    private final Node level;

    private Severity(Node uri) {
        this.level = uri;
    }

    public Node level() {
        return level;
    }

    public static Severity create(Node n) {
        Objects.requireNonNull(n);
        if ( n.equals(SHACL.Violation) ) return Violation;
        if ( n.equals(SHACL.Warning) ) return Warning;
        if ( n.equals(SHACL.Info) ) return Info;
        if ( n.isURI() ) return new Severity(n);
        throw new ShaclException("Not a recognized severity: "+displayStr(n));
    }

    @Override
    public int hashCode() {
        return 53*Objects.hash(level);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( !(obj instanceof Severity other) )
            return false;
        return Objects.equals(level, other.level);
    }
}
