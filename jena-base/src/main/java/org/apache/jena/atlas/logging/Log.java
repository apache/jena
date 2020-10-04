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

import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.CacheSet ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Simple wrappers and operations for convenient, non-time critical logging. These
 * operations find/create the logger by name, or by the class of some object, or an
 * org.slf4j.Logger object.
 * 
 * @See FmtLog
 */
public class Log {
    private Log() {}

    public static void info(Object object, String msg) {
        log(object).info(msg) ;
    }

    public static void info(Object object, String msg, Throwable th) {
        log(object).info(msg, th) ;
    }

    public static void debug(Object object, String msg) {
        log(object).debug(msg) ;
    }

    public static void debug(Object object, String msg, Throwable th) {
        log(object).debug(msg, th) ;
    }

    public static void warn(Object object, String msg) {
        log(object).warn(msg) ;
    }

    public static void warn(Object object, String msg, Throwable th) {
        log(object).warn(msg, th) ;
    }

    public static void error(Object object, String msg) {
        log(object).error(msg) ;
    }

    public static void error(Object object, String msg, Throwable th) {
        log(object).error(msg, th) ;
    }

    private static Logger log(Object object) {
        if ( object instanceof String )
            return LoggerFactory.getLogger((String)object);
        if ( object instanceof Logger )
            return (Logger)object;
        if ( object instanceof Class<?> )
            return LoggerFactory.getLogger((Class<?>)object);
        return LoggerFactory.getLogger(object.getClass());
    }

    private static CacheSet<Object> warningsDone = CacheFactory.createCacheSet(100) ;
    /** Generate a warning, once(ish) */
    public static void warnOnce(Class<?> cls, String message, Object key) {
        if ( ! warningsDone.contains(key) ) {
            Log.warn(cls, message) ;
            warningsDone.add(key); 
        }
    }
}
