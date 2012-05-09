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

package org.apache.jena.tdb.store.bulkloader3;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.openjena.atlas.data.SerializationFactory;
import org.openjena.atlas.lib.Sink;
import org.openjena.atlas.lib.Tuple;

public class QuadSerializationFactory implements SerializationFactory<Tuple<Long>> {
    @Override public Iterator<Tuple<Long>> createDeserializer(InputStream in) { return new TupleInputStream(in, 4); }
    @Override public Sink<Tuple<Long>> createSerializer(OutputStream out) { return new TupleOutputStream(out); }
    @Override public long getEstimatedMemorySize(Tuple<Long> item) { return 32L ; } // 8 * 4 = 32 bytes
}