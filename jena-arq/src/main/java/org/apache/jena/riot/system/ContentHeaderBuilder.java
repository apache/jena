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

public class ContentHeaderBuilder {

    public static ContentHeaderBuilder create() { return new ContentHeaderBuilder(null); }

    public static ContentHeaderBuilder create(String string) {
        return new ContentHeaderBuilder(string);
    }

    private final StringBuilder sBuff;

    private ContentHeaderBuilder(String initial) {
        if ( initial != null )
            sBuff = new StringBuilder(initial);
        else
            sBuff = new StringBuilder();
    }

    public ContentHeaderBuilder add(String mediaType) {
        return add(mediaType, -1);
    }

    public ContentHeaderBuilder add(String mediaType, double q) {
        if ( sBuff.length() != 0 )
            sBuff.append(", ") ;

        sBuff.append(mediaType) ;
        if ( q > 0 )
            sBuff.append(";q=").append(q) ;
        return this;
    }

    public String build() {
        return sBuff.toString();
    }
}

