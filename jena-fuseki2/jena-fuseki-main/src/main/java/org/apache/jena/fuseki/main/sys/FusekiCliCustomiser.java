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

package org.apache.jena.fuseki.main.sys;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;

/**
 * Interface to implement for extending the CLI argument parsing portion of a {@link FusekiServer}
 */
public interface FusekiCliCustomiser {

    /**
     * Called prior to any CLI argument parsing starting, allows a Fuseki module to add custom arguments via
     * {@link FusekiMain#addArg(String, String)} and its related overloads
     *
     * @param fuseki Fuseki Main
     */
    public default void customiseCli(FusekiMain fuseki) {
    }

    /**
     * Called during CLI argument parsing, allows a Fuseki module to pull out any custom arguments it has added and
     * process them appropriately, including by modifying the builder that is starting to be built.
     * <p>
     * This happens prior to pretty much any other argument processing so can be used to do all sorts of customisation
     * of the builder.  Note that Fuseki's default argument processing still applies so implementors need to be aware
     * that default argument processing could overwrite some configuration.
     * </p>
     *
     * @param fuseki  Fuseki Main
     * @param builder Fuseki Builder
     */
    public default void processCliArgs(FusekiMain fuseki, FusekiServer.Builder builder) {
    }
}
