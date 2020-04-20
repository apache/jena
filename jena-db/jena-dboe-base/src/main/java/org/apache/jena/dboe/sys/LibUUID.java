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

package org.apache.jena.dboe.sys;

import java.util.UUID;

import org.apache.jena.atlas.lib.Bytes;

public class LibUUID {

    /** Generate bytes for a Java UUID (most significant first) */
    public static byte[] uuidAsBytes(UUID uuid) {
        // Not to be confused with UUID.nameUUIDFromBytes (a helper for version 3 UUIDs)
        return uuidAsBytes(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    /** UUID, as two longs, as bytes */
    public static byte[] uuidAsBytes(long mostSignificantBits, long leastSignificantBits) {
        byte[] bytes = new byte[16];
        Bytes.setLong(mostSignificantBits, bytes, 0);
        Bytes.setLong(leastSignificantBits, bytes, 8);
        return bytes;
    }

    /** A UUID string to bytes */
    public static byte[] uuidAsBytes(String str) {
        return uuidAsBytes(UUID.fromString(str));
    }

    /** UUID, as two longs, in RFC string format */
    public static String uuidToString(long mostSignificantBits, long leastSignificantBits) {
        return new UUID(mostSignificantBits, leastSignificantBits).toString();
    }
}
