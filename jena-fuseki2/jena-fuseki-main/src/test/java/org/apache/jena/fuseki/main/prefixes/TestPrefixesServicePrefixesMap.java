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

package org.apache.jena.fuseki.main.prefixes;

import org.apache.jena.fuseki.servlets.prefixes.PrefixesAccess;
import org.apache.jena.fuseki.servlets.prefixes.PrefixesMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/** Test the {@link PrefixesMap} implementation. */
public class TestPrefixesServicePrefixesMap extends AbstractTestPrefixesImpl {
    public TestPrefixesServicePrefixesMap() {
        super(make());
    }

    private static PrefixesAccess make() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        return new PrefixesMap(dsg.prefixes(), dsg);
    }
}
