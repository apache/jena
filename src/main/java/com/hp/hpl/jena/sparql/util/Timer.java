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

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.sparql.ARQException ;


public class Timer
{

    protected long timeFinish = -1 ;
    protected boolean inTimer = false ;
    protected long timeStart  = 0 ;

    public Timer() { }

    public void startTimer()
    { 
        if ( inTimer )
            throw new ARQException("Already in timer") ;

        timeStart = System.currentTimeMillis() ;
        timeFinish = -1 ;
        inTimer = true ;
    }

    /** Return time in millisecods */
    public long endTimer()
    { 
        if ( ! inTimer )
            throw new ARQException("Not in timer") ;
        timeFinish = System.currentTimeMillis() ;
        inTimer = false ;
        return getTimeInterval() ;
    }

    public long readTimer() 
    {
        if ( ! inTimer )
            throw new ARQException("Not in timer") ;
        return System.currentTimeMillis()-timeStart  ;
    }

    public long getTimeInterval()
    {
        if ( inTimer )
            throw new ARQException("Still timing") ;
        if ( timeFinish == -1 )
            throw new ARQException("No valid interval") ;

        return  timeFinish-timeStart ;
    }

    static public String timeStr(long timeInterval)
    {
//        DecimalFormat f = new DecimalFormat("#0.###") ;
//        String s = f.format(timeInterval/1000.0) ;
//        return s ;
        //Java5
        return String.format("%.3f", timeInterval/1000.0) ;
    }

    protected String timeStr(long timePoint, long startTimePoint)
    {
        return timeStr(timePoint-startTimePoint) ;
    }
}
