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

public abstract class CmdArgModule extends CmdMain
{
    List<ArgModuleGeneral> modules = new ArrayList<>() ;
    
    protected CmdArgModule(String[] argv)
    {
        super(argv) ;
    }
    
    protected void addModule(ArgModuleGeneral argModule)
    {
        modules.add(argModule) ;
    }

    @Override
    final
    public void process()
    {
        super.process() ;
        forEach(new Action(){
            @Override
            public void action(CmdArgModule controller, ArgModuleGeneral module)
            { 
                module.processArgs(controller) ;
            }
        } ) ;
        processModulesAndArgs() ;
    }
    
    abstract
    protected void processModulesAndArgs() ;
    
    private void forEach(Action action)
    {
        for ( ArgModuleGeneral am : modules )
        {
            action.action( this, am );
        }
    }
                    
    interface Action
    {
        public void action(CmdArgModule controller, ArgModuleGeneral module) ;
    }
}
