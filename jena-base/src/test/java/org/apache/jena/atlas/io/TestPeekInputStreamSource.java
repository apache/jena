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

package org.apache.jena.atlas.io;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class TestPeekInputStreamSource extends AbstractTestPeekInputStream {
    @Override
    PeekInputStream make(String contents, int size) {
        // Very carefully ensure this is not a byte array-based PeekReader
        ByteArrayInputStream bin = new ByteArrayInputStream(contents.getBytes(StandardCharsets.US_ASCII));
        return PeekInputStream.make(bin, size);
    }
}
