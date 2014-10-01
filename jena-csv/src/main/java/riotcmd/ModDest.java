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

package riotcmd;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.ArgModuleGeneral;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;

class ModDest implements ArgModuleGeneral{
	
	private ArgDecl argDest     = new ArgDecl(ArgDecl.HasValue, "dest") ;
	private String dest         = null ;

	@Override
	public void processArgs(CmdArgModule cmdLine) {
		if ( cmdLine.contains(argDest) ) {
			dest = cmdLine.getValue(argDest) ;
        } else {
        	throw new CmdException("No destination output file! Please add '--dest=file' in the program arguements") ;
        }
	}

	@Override
	public void registerWith(CmdGeneral cmdLine) {
		cmdLine.getUsage().startCategory("Destination Output") ;
		cmdLine.add(argDest,    "--dest=file",      "The destination output file") ;	
	}
	
    public String getDest() {
        return dest ;
    }

}
