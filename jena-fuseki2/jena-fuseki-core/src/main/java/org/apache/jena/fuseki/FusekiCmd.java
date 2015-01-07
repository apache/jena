/**
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

package org.apache.jena.fuseki;

import java.lang.reflect.Method ;

public class FusekiCmd {
    public static void main(String[] args) {
        // Must NOT use any logging.  The command processor sets that up.
        System.err.println("Deprecated: Use org.apache.jena.fuseki.cmd.FusekiCmd") ;
        try {
            // A
            callByRefection("org.apache.jena.fuseki.cmd.FusekiCmd", "main", args) ;
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            System.err.println("Failed to find the command processor: "+ ex.getMessage()) ;
        } catch (Exception ex) {
            System.err.println("Failed to invoke the command processor: "+ ex.getMessage()) ;
            ex.printStackTrace(System.err) ;
        }
    }

    // Call a static of no arguments by reflection.
    private static void callByRefection(String className, String staticMethod, String[] args) 
        throws Exception
    {
        Class<? > cls = Class.forName(className) ;
        // Pass up : ClassNotFoundException

        Method m = cls.getMethod(staticMethod, String[].class) ;
        m.invoke(null, (Object)args) ;
    }
}

