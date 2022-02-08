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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** A command line argument specification. */
public class ArgDecl
{
    boolean takesValue;

    List<String> names = new ArrayList<>();

    public static final boolean HasValue = true;
    public static final boolean NoValue = false;

    /**
     * Create a declaration for a command argument.
     *
     * @param hasValue Does it take a value or not?
     */
    public ArgDecl(boolean hasValue) {
        takesValue = hasValue;
    }

    /**
     * Create a declaration for a command argument.
     *
     * @param hasValue Does it take a value or not?
     * @param names Names of arguments
     */
    public ArgDecl(boolean hasValue, String...names) {
        this(hasValue);
        asList(names).forEach(this::addName);
    }

    public void addName(String name) {
        name = canonicalForm(name);
        if ( !names.contains(name) )
            names.add(name);
    }

    public String getKeyName() { return names.get(0); }

    public List<String> getNames() { return names; }
    public Iterator<String> names() { return names.iterator(); }

    public boolean takesValue() { return takesValue; }

    public boolean matches(Arg a) {
        String name = a.getName();
        return names.stream().anyMatch(name::equals);
    }

    public boolean matches(String arg) {
        arg = canonicalForm(arg);
        return names.contains(arg);
    }

    public static String canonicalForm(String str) {
        if ( str.startsWith("--") )
            return str.substring(2);

        if ( str.startsWith("-") )
            return str.substring(1);

        return str;
    }
}
