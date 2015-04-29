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

package org.apache.jena.atlas.logging ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/* Simple wrappers and operations for convenient, non-time critical logging.
 */
public class Log {
    private Log() {}

    static public void info(String caller, String msg) {
        log(caller).info(msg) ;
    }

    static public void info(Object caller, String msg) {
        log(caller.getClass()).info(msg) ;
    }

    static public void info(Class<? > cls, String msg) {
        log(cls).info(msg) ;
    }

    static public void info(Object caller, String msg, Throwable th) {
        log(caller.getClass()).info(msg, th) ;
    }

    static public void info(Class<? > cls, String msg, Throwable th) {
        log(cls).info(msg, th) ;
    }

    static public void debug(String caller, String msg) {
        log(caller).debug(msg) ;
    }

    static public void debug(Object caller, String msg) {
        log(caller.getClass()).debug(msg) ;
    }

    static public void debug(Class<? > cls, String msg) {
        log(cls).debug(msg) ;
    }

    static public void debug(Object caller, String msg, Throwable th) {
        log(caller.getClass()).debug(msg, th) ;
    }

    static public void debug(Class<? > cls, String msg, Throwable th) {
        log(cls).debug(msg, th) ;
    }

    static public void warn(String caller, String msg) {
        log(caller).warn(msg) ;
    }

    static public void warn(Object caller, String msg) {
        warn(caller.getClass(), msg) ;
    }

    static public void warn(Class<? > cls, String msg) {
        log(cls).warn(msg) ;
    }

    static public void warn(Object caller, String msg, Throwable th) {
        warn(caller.getClass(), msg, th) ;
    }

    static public void warn(Class<? > cls, String msg, Throwable th) {
        log(cls).warn(msg, th) ;
    }

    static public void fatal(Object caller, String msg) {
        fatal(caller.getClass(), msg) ;
    }

    static public void fatal(Class<? > cls, String msg) {
        log(cls).error(msg) ;
    }

    static public void fatal(Object caller, String msg, Throwable th) {
        fatal(caller.getClass(), msg, th) ;
    }

    static public void fatal(Class<? > cls, String msg, Throwable th) {
        log(cls).error(msg, th) ;
    }

    static public void fatal(String caller, String msg) {
        log(caller).error(msg) ;
    }

    static private Logger log(Class<? > cls) {
        return LoggerFactory.getLogger(cls) ;
    }

    static private Logger log(String loggerName) {
        return LoggerFactory.getLogger(loggerName) ;
    }
}
