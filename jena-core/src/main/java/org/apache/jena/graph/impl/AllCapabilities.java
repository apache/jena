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

package org.apache.jena.graph.impl;

import org.apache.jena.graph.Capabilities ;

/**
 * A default implementation of capabilities, in which add and delete are allowed,
 * size is accurate and literals are "same by term".
 */
public class AllCapabilities implements Capabilities {

    public static Capabilities updateAllowed = create(true, true, true, false);

    public static Capabilities updateNotAllowed = create(true, false, false, false);

    public static Capabilities updateAllowedWithValues = create(true, true, true, true);

    public static Capabilities create(boolean sizeAccurate,
                                      boolean addAllowed,
                                      boolean deleteAllowed,
                                      boolean handlesLiteralTyping
                                      ) {
        return new Capabilities() {
            @Override public boolean sizeAccurate()     { return sizeAccurate; }
            @Override public boolean addAllowed()       { return addAllowed; }
            @Override public boolean deleteAllowed()    { return deleteAllowed; }
            @Override public boolean handlesLiteralTyping() { return handlesLiteralTyping; }
        };

    }

    // Legacy.
    /** @deprecated Do not use. Use one of the constants in this class, or {@link #create}. */
    @Deprecated
    protected AllCapabilities() {}

    @Override
    public boolean sizeAccurate() {
        return true;
    }

    @Override
    public boolean addAllowed() {
        return true;
    }

    @Override
    public boolean deleteAllowed() {
        return true;
    }

    @Override
    public boolean handlesLiteralTyping() {
        return false;
    }
}
