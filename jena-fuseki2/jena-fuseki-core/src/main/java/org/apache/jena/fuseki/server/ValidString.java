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

package org.apache.jena.fuseki.server;


import java.util.Objects;

import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.FusekiException;

/** A {@code String} that has passed a validation process. */
public class ValidString {

    /** Apply validation and return a {@code ValidatedString}.
     * Throws a {@link FusekiException} if validation fails.
     * Does not return null.
     */
    public static ValidString create(String string, Validator validator) {
        boolean isValid = validator.validator.apply(string);
        if ( ! isValid )
            throw new FusekiConfigException("String '"+string+"' not valid as '"+validator.getPolicy()+"'");
        return new ValidString(string, validator);
    }

    // It's a record.
    public final String string;
    public final Validator validator;

    private ValidString(String string, Validator validator) {
        this.string = string;
        this.validator = validator;
    }

    @Override
    public String toString() {
        return "Valid["+string+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, validator);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ValidString other = (ValidString)obj;
        // Object identity for 'validator'
        return Objects.equals(string, other.string) && Objects.equals(validator, other.validator);
    }
}
