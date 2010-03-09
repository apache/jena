/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb.cmdline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import arq.cmd.CmdException;


public class CmdSub
{
    public interface Exec { public void exec(String[] argv) ; }
    Map<String, Exec> dispatch = new HashMap<String, Exec>() ;
    
    String subCmd ;
    String args[] ;
    
    public CmdSub(String ...argv)
    {
        subCmd = subCommand(argv) ;
        args = cmdline(argv) ;
    }
    
    protected void exec()
    {
        Exec exec = dispatch.get(subCmd) ;
        if ( exec == null )
            throw new CmdException("No subcommand: "+subCmd) ;
        exec.exec(args) ;
    }

    protected static String[] cmdline(String ... argv)
    {
        String [] a = new String[argv.length-1] ;
        System.arraycopy(argv, 1, a, 0, argv.length-1) ;
        return a ; 
    }

    protected static String subCommand(String ... argv)
    {
        if ( argv.length == 0 )
            throw new CmdException("Missing subcommand") ;

        String subCmd = argv[0] ;
        if ( subCmd.startsWith("-") )
            throw new CmdException("Argument found where subcommand expected") ;
        return subCmd ;
    }
    
    protected void addSubCommand(String subCmdName, Exec exec)
    {
        dispatch.put(subCmdName, exec) ;
    }
    
    protected Collection<String> subCommandNames()
    {
        return dispatch.keySet() ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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