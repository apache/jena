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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This AspectJ aspect is responsible for telling us when our methods are
 * entered and exit. It is used to log trace level events on method entry and
 * exit which are useful when debugging JDBC drivers to see why some
 * functionality does not work as expected
 * <p>
 * Importantly this must not be in the affected package
 * <strong>org.apache.jena.jdbc</strong> as otherwise we will get a nasty
 * infinite stack recursion with it trying to log its own method entries and
 * exits.
 * </p>
 * 
 */
public aspect MethodTraceLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTraceLogger.class);

    private static final int CLIENT_CODE_STACK_INDEX;

    static {
        // Finds out the index of "this code" in the returned stack trace -
        // funny but it differs in JDK 1.5 and 1.6
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            i++;
            if (ste.getClassName().equals(MethodTraceLogger.class.getName())) {
                break;
            }
        }
        CLIENT_CODE_STACK_INDEX = i;
    }

    /**
     * Select all packages whose correctness is not impacted by the advice.
     * Determined empirically
     */
    pointcut safePkg() :
        execution( * org.apache.jena.jdbc..*(..))
;

    before(): safePkg() {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Jena JDBC Method Entry: {} ", MethodTraceLogger.fullyQualifiedMethodName());
    } // end Advice

    after(): safePkg() {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Jena JDBC Method Exit: {} ", MethodTraceLogger.fullyQualifiedMethodName());
    }

    /**
     * Gets the fully qualified method name of the calling method via inspection
     * of the current threads stack
     * 
     * @return Method Name or null if not determinable
     */
    public static String fullyQualifiedMethodName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length <= CLIENT_CODE_STACK_INDEX + 1) {
            // If the stack is this length then we have been called directly not
            // from another method so can't return anything
            return null;
        }
        // The current method will be at element 0 so our calling method will be
        // at element 1
        StackTraceElement element = stack[CLIENT_CODE_STACK_INDEX + 1];
        return element.getClassName() + "." + element.getMethodName() + "() from " + element.getFileName() + " Line "
                + element.getLineNumber();
    }
}
