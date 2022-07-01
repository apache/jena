/**
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

package org.apache.jena.riot.system ;

import java.util.Map ;

import org.apache.jena.shared.PrefixMapping ;

/** Empty prefix map that throws away updates. */
public class PrefixMapSink extends PrefixMapNull {
    public static PrefixMap sink = new PrefixMapSink() ;

    private PrefixMapSink() {}

    @Override
    public void add(String prefix, String iri) { }

    @Override
    public void putAll(PrefixMap pmap) { }

    @Override
    public void putAll(PrefixMapping pmap) { }

    @Override
    public void putAll(Map<String, String> mapping) { }

    @Override
    public void delete(String prefix) { }

    @Override
    public void clear() { }

    @Override
    public String toString() {
        return "PrefixMapSink";
    }
}
