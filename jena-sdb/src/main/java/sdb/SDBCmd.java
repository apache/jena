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

package sdb;

import com.hp.hpl.jena.sparql.util.Utils;

import arq.cmdline.CmdMain;

/** Convenience ways to execute commands (import static) */
public class SDBCmd
{
    private static String sdbFile = null ;
    public static void setSDBConfig(String f) { sdbFile = f ; }
    
    private static boolean exitOnError = true ;
    public static void setExitOnError(boolean exitOn) { exitOnError = exitOn ; }
    
    public static int qparse(String... args)       { return exit(new arq.qparse(args)) ; } 
    public static int sparql(String... args)       { return exit(new arq.query(args)) ; } 
    
    public static int sdbprint(String... args)     { return exit(new sdb.sdbprint(args(args))) ; } 
    public static int sdbconfig(String... args)    { return exit(new sdb.sdbconfig(args(args))) ; } 
    public static int sdbload(String... args)      { return exit(new sdb.sdbload(args(args))) ; } 
    public static int sdbdump(String... args)      { return exit(new sdb.sdbdump(args(args))) ; } 
    public static int sdbquery(String... args)     { return exit(new sdb.sdbquery(args(args))) ; } 
    public static int sdbtruncate(String... args)  { return exit(new sdb.sdbtruncate(args(args))) ; } 

    public static int sdbinfo(String... args)      { return exit(new sdb.sdbinfo(args(args))) ; } 
    public static int sdbmeta(String... args)      { return exit(new sdb.sdbmeta(args(args))) ; } 
    public static int sdbsql(String... args)       { return exit(new sdb.sdbsql(args(args))) ; } 
    
    private static int exit(CmdMain cmd)
    {
        int code = cmd.mainRun(false, false) ;
        if ( code != 0 && exitOnError )
        {
            System.err.println("Exit: command: "+Utils.className(cmd)) ;
            System.exit(code) ;
        }
        return code ;
    }
    
    private static String[] args(String[] a)
    {
        // A better way to set the global?
        if ( sdbFile != null )
        {
            String a2[] = new String[a.length+1] ;
            a2[0] = "--sdb="+sdbFile ;
            System.arraycopy(a, 0, a2, 1, a.length) ;
            a = a2 ;
        }
        
        return a ;
        
    }
    
}
