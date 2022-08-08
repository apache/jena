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

/**
 * Exception used with {@link RangeBuffer} when attempting to read a range of data
 * for which there exist one or more gaps in the buffer.
 * Read operations should typically be scheduled w.r.t. available data, however
 * concurrent modifications may invalidate such schedules and re-scheduling based on this
 * exception is a simple way to react to such changes.
 *
 */
public class ReadOverGapException
    extends IOException
{
    private static final long serialVersionUID = 1L;

    public ReadOverGapException() {
        super();
    }

    public ReadOverGapException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadOverGapException(String message) {
        super(message);
    }

    public ReadOverGapException(Throwable cause) {
        super(cause);
    }
}
