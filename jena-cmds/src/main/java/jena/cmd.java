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

package jena;

import java.util.Arrays;

import org.apache.jena.cmd.Cmds;
import org.apache.jena.sys.JenaSystem;

/**
 * Indirection to another command.
 * <pre>
 * jena.cmd OtherCmd arg1 arg2 ...
 * </pre>
 */
public class cmd {
    public static void main(String...args) {
        if ( args.length == 0 ) {
            System.err.println("Usage: jena.cmd subcmd args...");
            System.exit(1);
        }

        try {
            JenaSystem.init();
        } catch (NoClassDefFoundError ex) {
            System.err.println("NoClassDefFoundError: Class missing on the classpath/modulepath: " + ex.getMessage().replace('/', '.'));
            System.exit(2);
        }
        String cmd = args[0];
        String[] argsSub = Arrays.copyOfRange(args, 1, args.length);

        // Currently, only externally registered commands.
        Cmds.exec(cmd, argsSub);
    }
}
