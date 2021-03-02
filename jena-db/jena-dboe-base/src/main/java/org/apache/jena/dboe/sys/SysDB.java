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

import java.nio.ByteOrder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.DBOpEnvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Low level environment */
public class SysDB
{
    static final Logger log = LoggerFactory.getLogger("org.apache.jena.dboe");

    /** System log - use for general messages (a few) and warnings.
     *  Generally, do not log events unless you want every user to see them every time.
     *  Libraries and embedded systems should be seen and not heard.
     *  @see #errlog
     */

    /** General system log */
    public static final Logger syslog = LoggerFactory.getLogger("org.apache.jena.dboe.System");
    /** Send warnings and error */
    public static final Logger errlog = LoggerFactory.getLogger("org.apache.jena.dboe.System");

    /** Size, in bytes, of a Java long */
    public static final int SizeOfLong              = Long.BYTES; // Long.SIZE/Byte.SIZE ;

    /** Size, in bytes, of a Java int */
    public static final int SizeOfInt               = Integer.BYTES; //Integer.SIZE/Byte.SIZE ;

    public static final boolean is64bitSystem = determineIf64Bit();

    public static final ByteOrder NetworkOrder      = ByteOrder.BIG_ENDIAN;

    // To make the class initialize
    static public void init() {}

    public static void panic(Class<? > clazz, String string) {
        Log.error(clazz, string);
        throw new DBOpEnvException(string);
    }

    // Memory mapped files behave differently.
    public static final boolean isWindows = org.apache.jena.base.Sys.isWindows;

    //Or look in File.listRoots.
    //Alternative method:
    //  http://stackoverflow.com/questions/1293533/name-of-the-operating-system-in-java-not-os-name

    /** A general thread pool */
    public static Executor executor = Executors.newCachedThreadPool();

    private static boolean determineIf64Bit() {
        String s = System.getProperty("sun.arch.data.model");
        if ( s != null ) {
            boolean b = s.equals("64");
            syslog.debug("System architecture: " + (b ? "64 bit" : "32 bit"));
            return b;
        }
        // Not a SUN VM
        s = System.getProperty("java.vm.info");
        if ( s == null ) {
            log.warn("Can't determine the data model");
            return false;
        }
        log.debug("Can't determine the data model from 'sun.arch.data.model' - using java.vm.info");
        boolean b = s.contains("64");
        syslog.debug("System architecture: (from java.vm.info) " + (b ? "64 bit" : "32 bit"));
        return b;
    }
}
