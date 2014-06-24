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

package org.apache.jena.jdbc.mem;

import java.io.ByteArrayOutputStream ;
import java.io.PrintStream ;
import java.net.URL ;

import org.apache.jena.jdbc.AbstractJenaDriverTests ;
import org.apache.jena.jdbc.JenaDriver ;
import org.apache.log4j.LogManager ;
import org.apache.log4j.PropertyConfigurator ;
import org.apache.log4j.helpers.Loader ;
import org.junit.AfterClass ;

/**
 * Tests for the {@link MemDriver}
 *
 */
public class TestMemDriverWithLogging extends AbstractJenaDriverTests {

    private static ByteArrayOutputStream output;
    private static PrintStream orig;

    @AfterClass public static void afterClassResetLogging() {
        // Unfortunately, this code "knows" how log4j was set up in testing
        // Thsi is fragile.
        URL configURL = Loader.getResource("log4j.properties");
        new PropertyConfigurator().doConfigure(configURL,
                                               LogManager.getLoggerRepository());
    }
    
    @Override
    protected JenaDriver getDriver() {
        return new MemDriver();
    }

    @Override
    protected String getConnectionUrl() {
        return JenaDriver.DRIVER_PREFIX + MemDriver.MEM_DRIVER_PREFIX + MemDriver.PARAM_EMPTY + "=true&" + JenaDriver.PARAM_LOGGING + "=/jena-jdbc-log4j.properties";
    }

    @Override
    protected String getBadConnectionUrl() {
        return JenaDriver.DRIVER_PREFIX + MemDriver.MEM_DRIVER_PREFIX + MemDriver.PARAM_EMPTY + "=false";
    }
}
