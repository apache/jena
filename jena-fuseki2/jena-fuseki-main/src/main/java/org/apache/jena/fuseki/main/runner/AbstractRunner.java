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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.runner;

import java.io.IOException;

import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiAbortException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;

/**
 * Machinery to construct a Fuseki server from modules and the command line.
 * <p>
 * See {@link FusekiServer.Builder} for programmatic construction.
 * See {@link FusekiRunner} for including {@link FusekiModule FusekiModules}.
 *
 */
abstract /*package*/ class AbstractRunner implements Runner {

    // Build one server at a time.
    private static Object lock = new Object();

    /** {@inheritDoc} */
    @Override
    public void exec(String... args) {
          FusekiServer fusekiServer = process(args);
          startAsync(fusekiServer);
          fusekiServer.join();
    }

    private static boolean isBindException(FusekiException ex) {
        if ( ex.getCause() instanceof IOException ex2 ) {
            if ( ex2.getCause() instanceof java.net.BindException exBind ) {
                return true;
            }
        }
        return false;
    }

    private static void exitWithMessage(int exitStatus, String msg) {
        throw new FusekiAbortException(exitStatus, msg);
    }

    /** {@inheritDoc} */
    @Override
    public FusekiServer runAsync(String...args) {
        FusekiServer server = process(args);
        startAsync(server);
        return server;
    }

    /**
     * Build and start.
     */
    protected FusekiServer process(String...args) {
        synchronized(lock) {
            // Build one at a time
            FusekiServer server = construct(args);
            return server;
        }
    }

    private static FusekiServer startAsync(FusekiServer server) {
        int port = server.getHttpPort();
        try {
            return server.start();
        } catch (FusekiAbortException ex) {
            throw ex;
        } catch (FusekiException ex) {
            if ( isBindException(ex) ) {
                if ( port > 0 )
                    exitWithMessage(2, "Failed to bind port "+port+" - another Fuseki running on this machine?");
            }
            throw ex;
        } catch (Exception ex) {
            throw exception("Failed to start server: " + ex.getMessage(), ex);
        }
    }

    private static RuntimeException exception(String message, Throwable ex) {
        throw new FusekiException(message, ex);
    }

    private static RuntimeException abort(String message, Throwable ex) {
        throw new FusekiException(message, ex);
    }



    /** {@inheritDoc} */
    @Override
    public FusekiServer construct(String... args) {
        FusekiServer.Builder builder = builder(args);
        return builder.build();
    }

    /**
     * Create a {@link org.apache.jena.fuseki.main.FusekiServer.Builder FusekiServer.Builder}
     * initialized according to the command line arguments processed.
     */
    abstract protected FusekiServer.Builder builder(String... args);
}
