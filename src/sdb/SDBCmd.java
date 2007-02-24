/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    
    public static int qparse(String... args)       { return exit(new arq.qparse(args(args))) ; } 
    public static int sparql(String... args)       { return exit(new arq.query(args(args))) ; } 
    
    public static int sdbprint(String... args)     { return exit(new sdb.sdbprint(args(args))) ; } 
    public static int sdbformat(String... args)    { return exit(new sdb.sdbformat(args(args))) ; } 
    public static int sdbload(String... args)      { return exit(new sdb.sdbload(args(args))) ; } 
    public static int sdbdump(String... args)      { return exit(new sdb.sdbdump(args(args))) ; } 
    public static int sdbquery(String... args)     { return exit(new sdb.sdbquery(args(args))) ; } 
    public static int sdbtruncate(String... args)  { return exit(new sdb.sdbtruncate(args(args))) ; } 
    
    public static int sdbinfo(String... args)      { return exit(new sdb.sdbinfo(args(args))) ; } 
    public static int sdbmeta(String... args)      { return exit(new sdb.sdbmeta(args(args))) ; } 
    
    private static int exit(CmdMain cmd)
    {
        int code = cmd.main(false, false) ;
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

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */