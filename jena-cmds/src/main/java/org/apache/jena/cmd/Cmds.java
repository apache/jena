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

package org.apache.jena.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.jena.sys.JenaSystem;

/**
 * Command execute by registering a name and a way to call "main".
 * This is used by modules that are not a dependency of jena-cmds.
 */
public class Cmds {

    static { JenaSystem.init(); }

    private static Map<String, Consumer<String[]>> cmds;

    // Initialize via JenaSubsystemLifecycle and not rely on class initialization.
    static void init() {
        // Initialization should be minimal, just enough to allow modules to register commands.
        // We may be inside some other place where JenaSystem.init() was called.
        if ( cmds == null )
            cmds = new HashMap<>();
    }

    public static void injectCmd(String name, Consumer<String[]> main) {
        cmds.put(name, main);
    }

    public static Consumer<String[]> findCmd(String name) {
        return cmds.get(name);
    }

    public static void exec(String cmdName, String... args) {
        try {
            JenaSystem.init();
        } catch (NoClassDefFoundError ex) {
            System.err.println("NoClassDefFoundError: Class missing on the classpath/modulepath: "+ex.getMessage().replace('/', '.'));
            throw new CmdException("NoClassDefFoundError: "+ex.getMessage(), ex);
        }
        Consumer<String[]> main = findCmd(cmdName);
        if ( main == null ) {
            System.err.println("Command "+cmdName+" not found");
            return;
        }
        main.accept(args);
    }
}
