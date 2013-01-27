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

package com.hp.hpl.jena.shared;

/**
 * An Exception indicating that Jena is not working
 * because it is not correctly configured. For example,
 * the classpath is not set up correctly for the desired
 * functionality.
 */
public class ConfigException extends JenaException {

    /**
     * @param message
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * 
     */
    public ConfigException() {
        super();
    }

    /**
     * @param cause
     */
    public ConfigException(Throwable cause) {
        super("Jena not correctly configured: ",cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
