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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class StackTraceUtils {

    public static final boolean IS_ASSERT_ENABLED = isAssertEnabled();

    public static boolean isAssertEnabled() {
        boolean result;
        try {
           assert false;
           result = false;
        } catch (@SuppressWarnings("unused") AssertionError e) {
           result = true;
        }
        return result;
    }

    public static StackTraceElement[] getStackTraceIfEnabled() {
        StackTraceElement[] result = IS_ASSERT_ENABLED
                ? Thread.currentThread().getStackTrace()
                : null;

        return result;
    }


    public static String toString(StackTraceElement[] stackTrace) {
        String result = stackTrace == null
                ? "(stack traces not enabled - enable assertions using the -ea jvm option)"
                : Arrays.asList(stackTrace).stream().map(s -> "  " + Objects.toString(s))
                    .collect(Collectors.joining("\n"));

        return result;
    }

}
