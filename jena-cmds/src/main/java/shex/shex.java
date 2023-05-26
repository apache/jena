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

package shex;

import java.util.Arrays;
import java.util.Optional;

import org.apache.jena.atlas.lib.Version;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.sys.JenaSystem;

public class shex {
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    public static void main(String...args) {
        if ( args.length == 0 ) {
            System.err.println("Usage: shex SUB ARGS...");
            System.exit(1);
        }

        String cmd = args[0];
        String[] argsSub = Arrays.copyOfRange(args, 1, args.length);
        String cmdExec = cmd;

        // Help
        switch (cmdExec) {
            case "help" :
            case "-h" :
            case "-help" :
            case "--help" :
                System.err.println("Commands: validate (v), parse (p)");
                return;
            case "version":
            case "--version":
            case "-version": {
                Optional<String> ver = Version.versionForClass(shex.class);
                Version.printVersion(System.err, "ShEx",  ver);
                System.exit(0);
            }
        }

        // Map to full name.
        switch (cmdExec) {
            case "val": case "validate": case "v":
                cmdExec = "validate";
                break;
            case "parse": case "p": case "print":
                cmdExec = "parse";
                break;
        }

        // Execute sub-command
        switch (cmdExec) {
            case "validate":        shex_validate.main(argsSub); break;
            case "parse":           shex_parse.main(argsSub); break;
            default:
                System.err.println("Failed to find a command match for '"+cmd+"'");
        }
    }
}
