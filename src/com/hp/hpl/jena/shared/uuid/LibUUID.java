/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
        
        StringBuffer seedInput = new StringBuffer(200) ;
        
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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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