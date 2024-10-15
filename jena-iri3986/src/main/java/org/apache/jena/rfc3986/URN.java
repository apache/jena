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

package org.apache.jena.rfc3986;

/**
 * URN structure, following <a href="https://datatracker.ietf.org/doc/html/rfc8141">RFC 8141</a>.
 * <p>
 * The {@code URNComponents} may be null, indicating "none".
 * Allow international characters in NSS and URNComponents.
 * <p>
 * See {@link ParseURN#parseURN(String)} to create a URN object from a string.
 *
 * @see ParseURN
 */
public record URN(String scheme, String NID, String NSS, URNComponents components) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(scheme);
        sb.append(':');
        sb.append(NID);
        sb.append(':');
        sb.append(NSS);
        if ( components != null ) {
            if ( components.rComponent() != null ) {
                sb.append("?+");
                sb.append(components.rComponent());
            }
            if ( components.qComponent() != null ) {
                sb.append("?=");
                sb.append(components.qComponent());
            }
            if ( components.fComponent() != null ) {
                sb.append("#");
                sb.append(components.fComponent());
            }
        }
        return sb.toString();
    }
}
