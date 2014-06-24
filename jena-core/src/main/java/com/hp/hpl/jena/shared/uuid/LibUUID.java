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

package com.hp.hpl.jena.shared.uuid;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Random;

import com.hp.hpl.jena.JenaRuntime;

class LibUUID
{
    //static boolean warningSent = false ;
    //private static boolean noRandWarningSent = false ;

    static Random makeRandom()
    {
        SecureRandom sRandom = new SecureRandom() ; // SecureRandom.getInstance("SHA1PRNG");
        
        // ---- Seeding.
        // If no setSeed() call is made before a nextBytes call, the
        // generator "self seeds".  If a setSeed() is called before
        // any nextBytes call, no self seeding is done.  We use the
        // self seeding and our own bytes.
        
        // Access the internal seed generator.
        byte[] seed1 = sRandom.generateSeed(16) ;
        byte[] seed2 = LibUUID.makeSeed() ;
        // seeds are cumulative
        sRandom.setSeed(seed1) ;                     
        sRandom.setSeed(seed2) ;                     
        return sRandom ; 
    }

    static byte[] makeSeed()
    {
        // Make a random number seed from various pieces of information.
        // One thing that is missing is something related to the identify
        // of this OS process (so two identical programs, starting at
        // exactly the same time, might get the same seed).
        
        StringBuilder seedInput = new StringBuilder(200) ;
        
        try { seedInput.append(InetAddress.getLocalHost().getHostAddress()) ; }
        // Not every machine has an IP address.
        catch (UnknownHostException ex) { }       
        
        seedInput.append(JenaRuntime.getSystemProperty("os.version")) ;
        seedInput.append(JenaRuntime.getSystemProperty("user.name")) ;
        seedInput.append(JenaRuntime.getSystemProperty("java.version")) ;
        seedInput.append(Integer.toString(Thread.activeCount())) ;
        seedInput.append(Long.toString(Runtime.getRuntime().freeMemory())) ;
        seedInput.append(Long.toString(Runtime.getRuntime().totalMemory())) ;
        seedInput.append(Long.toString(System.currentTimeMillis())) ;
        // Some heap variance.  Maybe.
        seedInput.append(Long.toString(new Object().hashCode())) ;
        return seedInput.toString().getBytes() ;
    }
}
