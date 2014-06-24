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

package tdb.cmdline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import arq.cmd.CmdException;


public class CmdSub
{
    public interface Exec { public void exec(String[] argv) ; }
    Map<String, Exec> dispatch = new HashMap<>() ;
    
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
