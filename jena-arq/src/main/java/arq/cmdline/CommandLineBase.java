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

package arq.cmdline;

import java.util.ArrayList ;
import java.util.List ;

/** 
 * Incoming String[] to a list of argument/values + items.
 */


public class CommandLineBase {
    private List<String> argList    = new ArrayList<>() ;
    boolean              splitTerms = true ;

    public CommandLineBase(String[] args) {
        setArgs(args) ;
    }

    public CommandLineBase() {}
    
    public void setArgs(String[] argv)
    { argList = processArgv(argv) ; }
    
    protected List<String> getArgList() { return argList ; }

    protected String getArg(int i) {
        if ( i < 0 || i >= argList.size() )
            return null ;
        return argList.get(i) ;
    }

    protected void apply(ArgProc a) {
        a.startArgs() ;
        for (int i = 0; i < argList.size(); i++) {
            String arg = argList.get(i) ;
            a.arg(arg, i) ;
        }
        a.finishArgs() ;
    }
    
    /** Process String[] to a list of tokens.
     *  All "=" and ":" terms are split.
     *  Make -flag/--flag consistent.
     * @param argv The words of the command line.
     */    
    private List<String> processArgv(String[] argv) {
        // Combine with processedArgs/process?
        List<String> argList = new ArrayList<>() ;

        boolean positional = false ;

        for ( String anArgv : argv )
        {
            String argStr = anArgv;

            if ( positional || !argStr.startsWith( "-" ) )
            {
                argList.add( argStr );
                continue;
            }

            if ( argStr.equals( "-" ) || argStr.equals( "--" ) )
            {
                positional = true;
                argList.add( "--" );
                continue;
            }

            // Starts with a "-"
            // Do not canonicalize positional arguments.
            if ( !argStr.startsWith( "--" ) )
            {
                argStr = "-" + argStr;
            }

            if ( !splitTerms )
            {
                argList.add( argStr );
                continue;
            }

            // If the flag has a "=" or :, it is long form --arg=value.
            // Split and insert the arg
            int j1 = argStr.indexOf( '=' );
            int j2 = argStr.indexOf( ':' );
            int j = -1;

            if ( j1 > 0 && j2 > 0 )
            {
                j = Math.min( j1, j2 );
            }
            else
            {
                if ( j1 > 0 )
                {
                    j = j1;
                }
                if ( j2 > 0 )
                {
                    j = j2;
                }
            }

            if ( j < 0 )
            {
                argList.add( argStr );
                continue;
            }

            // Split it.
            String argStr1 = argStr.substring( 0, j );
            String argStr2 = argStr.substring( j + 1 );

            argList.add( argStr1 );
            argList.add( argStr2 );
        }
        return argList ;
    }
}
