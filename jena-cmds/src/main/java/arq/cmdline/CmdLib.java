/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package arq.cmdline;

import org.apache.jena.cmd.CmdException;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sparql.util.Symbol;

/**
 * Library of shared code for commands.
 */
public class CmdLib {

    /** Process the argument to "--set" */
    public static void processArgSet(String setArg, Context context) {
        String[] frags = setArg.split("=", 2);
        if ( frags.length != 2 )
            throw new CmdException("Can't split '" + setArg + "' -- looking for '=' to separate name and value");

        String symbolName = frags[0];
        String value = frags[1];

        // Make it a long name.
        symbolName = MappingRegistry.mapPrefixName(symbolName);
        Symbol symbol = Symbol.create(symbolName);
        context.set(symbol, value);
    }
}
