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

import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.cmds.ServerArgs;
import org.apache.jena.rdf.model.Model;

/**
 * Interface to implement for extending the CLI argument parsing portion of a
 * {@link FusekiServer}.
 * <p>
 * Customisation code is registered by calling {@link FusekiMain#addCustomiser}
 * before invoking {@link FusekiMain#build}. This can be done from Java code, or
 * during {@link FusekiAutoModule#start()} for dynamically loaded code.
 * <p>
 * The customiser modifies the Fuseki arguments setup after the standard Fuseki main
 * arguments have been registered. The customiser is then called
 * after the standard arguments have been used to produce the {@link ServerArgs}.
 * <p>
 * The lifecycle for command line argument processing is:
 * <ul>
 * <li>
 *   Add customisers by calling {@link FusekiMain#addCustomiser} from
 *   {@link FusekiAutoModule#start()} or from Java application code.
 * </li>
 * <li>
 *    {@link #serverArgsModify} &ndash; Register or modify the argument setup to be
 *     used to parse the command line.
 * </li>
 * <li>
 *   {@link #serverArgsPrepare} &ndash; Called after parsing the command line and
 *   recoding the command line settings in {@link ServerArgs}. Customisers can record
 *   argument values and flags.
 * </li>
 * <li>
 *   {@link #serverArgsBuilder} &ndash; Called after the {@link ServerArgs} have
 *   been used to construct a server builder.
 * </li>
 * </ul>
 * Following command like processing, server construction proceeds with the {@link FusekiBuildCycle},
 * the first step of which is {@link FusekiBuildCycle#prepare}.
 */
public interface FusekiServerArgsCustomiser {

    /**
     * Called after the standard Fuseki main arguments have been added
     * and before argument processing of the command line.
     * This allows a Fuseki module to add custom arguments via
     * {@link CmdGeneral#addArg(String, String)} and
     * {@link CmdGeneral#addModule(org.apache.jena.cmd.ArgModuleGeneral)}.
     * <p>
     * This method can throw {@link CmdException} to indicate errors.
     * This will cause a error message to be printed, without the stack trace.
     * The server construction is aborted.
     *
     * @param fusekiCmd Fuseki Main command line arguments
     * @param serverArgs Initial setting before command line processing.
     */
    public default void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) { }

    /**
     * Called at the end command line argument processing.
     * <p>
     * This allows a Fuseki module to pull out custom arguments it has added and
     * process them appropriately, including validating or modifying the
     * {@link ServerArgs} that will be used to build the server.
     * This method can set the set the dataset, in which case
     * a command line dataset setup or configuration file server
     * set up is not performed.
     * <p>
     * This method can throw {@link CmdException} to indicate errors.
     * This will cause a error message to be printed, without the stack trace.
     * The server construction is aborted.
     *
     * @param fusekiCmd Fuseki Main
     * @param serverArgs Standard server argument settings, before building the
     *     server.
     */
    public default void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) { }

    /**
     * Called at the end of applying the {@link ServerArgs} to the builder.
     * <p>
     * This step can do validation and argument processing dependent on the configuration model.
     * <p>
     * If there is a configuration model, this has been processed by the builder
     * <p>
     * If a command line dataset setup is being used, this is the dataset has been created.
     * <p>
     * This method can throw {@link CmdException} to indicate errors.
     * This will cause a error message to be printed, without the stack trace.
     * The server construction is aborted.
     *
     * @param serverBuilder The server builder.
     * @param configModel The configuration model; this may be null.
     */
    public default void serverArgsBuilder(FusekiServer.Builder serverBuilder, Model configModel) {}
}
