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

import com.hp.hpl.jena.sparql.util.Timer ;


public class ModTime implements ArgModuleGeneral
{
    public ModTime() {}
    
    protected final ArgDecl timeDecl = new ArgDecl(ArgDecl.NoValue, "time") ;
    
    protected Timer timer = new Timer() ;
    
    private boolean timing = false ;
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Time") ;
        cmdLine.add(timeDecl, "--time", "Time the operation") ;
    }
    
    public void checkCommandLine(CmdArgModule cmdLine)
    {}

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        timing = cmdLine.contains(timeDecl) ;
    }
    
    public boolean timingEnabled() { return timing ; }
    
    public void setTimingEnabled(boolean timingEnabled) { timing = timingEnabled ; }
    
    public void startTimer()
    { timer.startTimer() ; } 
    
    public long endTimer()
    { return timer.endTimer() ; } 
    
    public long readTimer() 
    { return timer.readTimer() ; }
    
    public long getTimeInterval()
    { return timer.getTimeInterval() ; }
    
    public String timeStr(long timeInterval)
    { return Timer.timeStr(timeInterval) ; }
    
}
