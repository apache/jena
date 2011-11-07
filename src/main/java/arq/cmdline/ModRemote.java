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


public class ModRemote implements ArgModuleGeneral
{
    protected final 
    ArgDecl serviceDecl = new ArgDecl(ArgDecl.HasValue, "service") ;
    
    // Or --serviceType GET, POST, SOAP
    protected final 
    ArgDecl postServiceDecl = new ArgDecl(ArgDecl.NoValue, "post", "POST") ;
    
    private String serviceURL ;
    private boolean usePost ;
    
    public ModRemote() {}
    
    public void checkCommandLine(CmdArgModule cmdLine)
    {}
    
    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        serviceURL = cmdLine.getValue(serviceDecl) ;
        usePost = cmdLine.contains(postServiceDecl) ;
    }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Remote") ;
        cmdLine.add(serviceDecl,
                    "--service=",
                    "Service endpoint URL") ;
        cmdLine.add(postServiceDecl,
                    "--post",
                    "Force use of HTTP POST") ;

    }

    public String getServiceURL()
    {
        return serviceURL ;
    }

    public boolean usePost()
    {
        return usePost ;
    }
    
    
    

}
