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

package org.apache.jena.graph;

import static org.apache.jena.atlas.lib.Lib.lowercase;

import org.apache.jena.shared.JenaException;

public enum TextDirection {

    LTR("ltr"), RTL("rtl") ;

    // "name" is used by enum.
    private final String direction;

    private TextDirection(String string) {
        this.direction = string;
    }

    public String direction() {
        return direction;
    }

    @Override
    public String toString() {
        return direction;
    }

    public static TextDirection create(String label) {
        String s = lowercase(label);
        return switch(s) {
            case "ltr" -> LTR;
            case "rtl"-> RTL;
            default ->
                throw new JenaException("Initial text direction must be 'ltr' or 'rtl'");
        };
    }
}
