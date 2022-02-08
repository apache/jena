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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/** Helper code to invoke a main method buy reflection
 *  This means command can be invoked from this module even
 *  if their implementation resides in a module later
 *  in the build sequence.
 */
public class Invoke {
    public static void invokeCmd(String className, String[] args) {
        Class<? > cmd = null;
        try {
            cmd = Class.forName(className);
        }
        catch (ClassNotFoundException ex) {
            System.err.println("Class '" + className + "' not found");
            System.exit(1);
        }

        Method method = null;
        try {
            method = cmd.getMethod("main", new Class[]{String[].class});
        }
        catch (NoSuchMethodException ex) {
            System.err.println("'main' not found but the class '" + className + "' was");
            System.exit(1);
        }

        try {
            method.invoke(null, new Object[]{args});
            return;
        }
        catch (IllegalArgumentException ex) {
            System.err.println("IllegalArgumentException exception: " + ex.getMessage());
            System.exit(7);
        }
        catch (IllegalAccessException ex) {
            System.err.println("IllegalAccessException exception: " + ex.getMessage());
            System.exit(8);
        }
        catch (InvocationTargetException ex) {
            System.err.println("InvocationTargetException exception: " + ex.getMessage());
            System.exit(9);
        }
    }
}
