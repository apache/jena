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

package org.apache.jena.sparql.service.enhancer.slice.impl;

import java.io.IOException;

import org.apache.jena.sparql.service.enhancer.slice.api.ArrayOps;
import org.apache.jena.sparql.service.enhancer.slice.api.HasArrayOps;

public interface ArrayReadable<A>
    extends HasArrayOps<A>
{
    int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException;

    @SuppressWarnings("unchecked")
    default int readIntoRaw(Object tgt, int tgtOffset, long srcOffset, int length) throws IOException {
        return readInto((A)tgt, tgtOffset, srcOffset, length);
    }

    default Object get(long index) throws IOException {
        ArrayOps<A> arrayOps = getArrayOps();
        A singleton = arrayOps.create(1);
        readInto(singleton, 0, index, 1);
        Object result = arrayOps.get(singleton, 0);
        return result;
    }
}
