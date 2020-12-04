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

package org.apache.jena.tdb2.store;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapWrapper;

/**
 * {@link PrefixMap} that goes to the current DatasetGraphTDB prefixes.
 */
public class PrefixMapSwitchable extends PrefixMapWrapper {
    // PrefixMapProxy not needed. It calls a Supplier
    // but in TDB2

    private final DatasetGraphSwitchable dsgx;
    protected DatasetGraphSwitchable getx() { return dsgx; }

    protected PrefixMapSwitchable(DatasetGraphSwitchable dsg) {
        // We override get() so don't set the wrapped object
        super(null);
        this.dsgx = dsg;
    }

    @Override
    protected PrefixMap getR() { return dsgx.get().prefixes(); }

    @Override
    protected PrefixMap getW() { return dsgx.get().prefixes(); }

}
