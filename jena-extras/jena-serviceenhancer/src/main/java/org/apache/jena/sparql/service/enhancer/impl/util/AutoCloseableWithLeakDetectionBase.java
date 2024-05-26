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

package org.apache.jena.sparql.service.enhancer.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation of AutoClosable that helps detecting resource leaks.
 * Creation of an instance of this class captures a snapshot of the stack trace.
 * If finalize is called (typically only by the GC) and there was no prior call to close then
 * a warning including the stack trace is logged.
 *
 * Implementing classes should override {@link #closeActual()} rather than
 * {@link #close()}.
 */
public class AutoCloseableWithLeakDetectionBase
    extends AutoCloseableBase
{
    private static final Logger logger = LoggerFactory.getLogger(AutoCloseableWithLeakDetectionBase.class);

    protected final StackTraceElement[] instantiationStackTrace = StackTraceUtils.getStackTraceIfEnabled();

    public StackTraceElement[] getInstantiationStackTrace() {
        return instantiationStackTrace;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        try {
            if (!isClosed) {
                String str = StackTraceUtils.toString(instantiationStackTrace);

                logger.warn("Close invoked via GC rather than user logic - indicates resource leak. Object constructed at " + str);

                close();
            }
        } finally {
            super.finalize();
        }
    }
}
