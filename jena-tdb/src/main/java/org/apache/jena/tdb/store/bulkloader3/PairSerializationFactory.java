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
import org.openjena.atlas.lib.Pair;
import org.openjena.atlas.lib.Sink;

public class PairSerializationFactory implements SerializationFactory<Pair<byte[], byte[]>> {
    @Override public Iterator<Pair<byte[], byte[]>> createDeserializer(InputStream in) { return new PairInputStream(in); }
    @Override public Sink<Pair<byte[], byte[]>> createSerializer(OutputStream out) { return new PairOutputStream(out); }
    @Override public long getEstimatedMemorySize(Pair<byte[], byte[]> item) { return 8 + item.getLeft().length + item.getRight().length ; } // 8 because 4 bytes for the length of each of the pair items
}
