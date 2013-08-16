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

package org.apache.jena.atlas.logging;

import java.util.IllegalFormatException ;

import org.slf4j.Logger ;

public class FmtLog {

    // -- Delayed argument formatting.
    /* Log at 'trace' level. */
    public static void trace(Logger log, String fmt, Object...args) {
        if ( log.isTraceEnabled() )
            log.trace(format(fmt, args)) ;
    }

    /* Log at 'debug' level */
    public static void debug(Logger log, String fmt, Object...args) {
        if ( log.isDebugEnabled() )
            log.debug(format(fmt, args)) ;
    }

    /* Log at 'info' level */
    public static void info(Logger log, String fmt, Object...args) {
        if ( log.isInfoEnabled() )
            log.info(format(fmt, args)) ;
    }

    /* Log at 'warn' level */
    public static void warn(Logger log, String fmt, Object...args) {
        if ( log.isWarnEnabled() )
            log.warn(format(fmt, args)) ;
    }

    /* Log at 'error' level */
    public static void error(Logger log, String fmt, Object...args) {
        if ( log.isErrorEnabled() )
            log.error(format(fmt, args)) ;
    }

    private static String format(String fmt, Object[] args) {
        try {
            return String.format(fmt, args) ;
        } catch (IllegalFormatException ex) {
            // return something, however grotty.
            return fmt+" "+args ;
        }
    }
    // ----

}

