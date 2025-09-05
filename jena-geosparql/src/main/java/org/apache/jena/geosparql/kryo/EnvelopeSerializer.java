/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jena.geosparql.kryo;

import org.locationtech.jts.geom.Envelope;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

public class EnvelopeSerializer extends Serializer<Envelope> {
    @Override
    public void write(Kryo kryo, Output output, Envelope envelope) {
        output.writeDouble(envelope.getMinX());
        output.writeDouble(envelope.getMaxX());
        output.writeDouble(envelope.getMinY());
        output.writeDouble(envelope.getMaxY());
    }

    @Override
    public Envelope read(Kryo kryo, Input input, Class<? extends Envelope> type) {
        double xMin = input.readDouble();
        double xMax = input.readDouble();
        double yMin = input.readDouble();
        double yMax = input.readDouble();
        if (xMin <= xMax) {
            return new Envelope(xMin, xMax, yMin, yMax);
        } else {
            // Null envelope cannot be constructed using Envelope(xMin, xMax, yMin, yMax)
            return new Envelope();
        }
    }
}
