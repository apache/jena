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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Args {
    // ---- Arguments in a file.

    /**
     * Pre-process the command line.
     * <p>
     * If the command line is "@file", then take the argument from the contents of the file.
     * If there is no "@file", return the function parameter string array object.
     * <p>
     * The file format is:
     * <ul>
     * <li>One line per argument or argument value. Lines are striped of horizontal white space.</li>
     * <li>Any "@file" in the file is not processed</li>
     * <li>Lines can be "--argument", "--argument value". The line is tokenized at the first space after non-white space</li>
     * <li>Using "--argument=value" is preferred.</li>
     * <li>Arguments are ended by end-of-file, a line which is "--", or a line which does not start with "-". Subsequent lines are positional values.</li>
     * <li>There is no escape mechanism. The arguments/argument-values are processed as if they appeared on the command line after shell escaping.</li>
     * <li>Comments are lines with first non-whitespace character of {@code #}
     * <li>Blank lines and lines of only horizontal white space are ignores</li>
     * Example:
     * <pre>
     * # Example arguments file
     *     ## Another comment
     * --arg1
     * --arg2 value
     *   --arg3=value
     * # Use --arg= for an empty string value
     * --empty=
     *   -q
     *
     *   # Previous line ignored
     *   ## --notAnArgument
     * </ul>
     * @return The command line for argument and values.
     */
    public static String[] argsPrepare(String[] argv) {
        List<String> argsList = Arrays.asList(argv);
        // Count!
        List<String> indirects = argsList.stream().filter(s->s.startsWith("@")).toList();
        if ( indirects.isEmpty() )
            return argv;
        if ( indirects.size() > 1 )
            throw new CmdException("Multiple arguments files");
        if ( argsList.size() > 1 )
            // @args and something else
            throw new CmdException("Arguments file must be the only item on the command line");
        String indirect = indirects.get(0);
        String fn = indirect.substring(1);
        try {
            if ( fn.isEmpty() )
                throw new CmdException("Empty arguments file name");
            Path path = Path.of(fn);
            if ( ! Files.exists(path) )
                throw new CmdException("No such file: "+fn);
            List<String> lines = Files.readAllLines(path);
            String[] args2 = toArgsArray(lines);
            return args2;
        } catch (NoSuchFileException ex) {
            throw new CmdException("No such file: "+fn);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CmdException("Failed to process args file: "+ex.getMessage());
        }
    }

    /** Convert the lines of the file to argument/value pairs. */
    private static String[] toArgsArray(List<String> lines) {
        // Each line is "--arg" or "--arg SPC value"
        List<String> outcome = new ArrayList<>();
        boolean positionalsStarted = false;
        for ( String s : lines ) {
            s = s.strip();
            if ( s.startsWith("#") )
                // Comment
                continue;
            if ( s.isEmpty() )
                continue;
            if ( positionalsStarted ) {
                outcome.add(s);
                continue;
            }

            if ( s.startsWith("@") )
                throw new CmdException("Argument file may not contain an argument file reference");
            if ( ! s.startsWith("-") ) {
                // positional
                //throw new CmdException("Command line in file does not start with a '-': "+s);
                positionalsStarted = true;
                outcome.add(s);
                continue;
            }

            // argument or argument and value
            // Split on first space after argument, if any.
            int idx = s.indexOf(' ');
            if ( idx == -1 ) {
                outcome.add(s.stripTrailing());
                continue;
            }

            String a = s.substring(0,idx);
            String v = s.substring(idx+1).strip();
            outcome.add(a);
            outcome.add(v);
        }
        return outcome.toArray(new String[outcome.size()]);
    }
}
