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

package org.apache.jena.atlas.logging;

import org.apache.jena.atlas.lib.ProgressMonitor ;
import org.slf4j.Logger ;

/** 
 * @deprecated Use {@link ProgressMonitor#create}. This class will be removed.
 */
@Deprecated
public class ProgressLogger extends ProgressMonitor
{
    public ProgressLogger(Logger log, String label, long tickPoint, int superTick)
    {
        super(label, tickPoint, superTick, (fmt, args)->print(log, fmt, args) ) ;
    }
    
    /** Print a message in the form for this ProgressLogger */ 
    static void print(Logger log, String fmt, Object...args)
    {
        if ( log != null && log.isInfoEnabled() )
        {
            String str = String.format(fmt, args) ;
            log.info(str) ;
        }
    }
}
