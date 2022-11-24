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

package org.apache.jena.shex.expressions;

import java.util.Objects;

public class SemAct {
    private final String iri;
    private final String code;

    public SemAct(String iri, String code) {
        this.iri = iri;
        this.code = code;
    }

    public String getIri() {
        return iri;
    }

    public String getCode() {
        return code;
    }

    static String semActStr(String iri, String code) {
        return String.format("%%<%s>{%s%%}", iri, code == null ? "" : code);
    }

    @Override
    public String toString() {
        return semActStr(iri, code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, iri);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SemAct other = (SemAct)obj;
        return Objects.equals(iri, other.iri) && Objects.equals(code, other.code);
    }
}
