/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import org.openjena.atlas.lib.InternalErrorException ;
import org.openjena.atlas.logging.Log ;

public enum JournalEntryType 
{ 
    // Abort is used 
    Block(1), Buffer(2), Object(3), Commit(4), Abort(5), Checkpoint(6) ;
    
    final int id ;
    JournalEntryType(int x) { id = x ; }
    int getId() { return id ; }
    static public JournalEntryType type(int x)
    {
        if ( x == Block.id )                return Block ;
        else if ( x == Buffer.id )          return Buffer ;
        else if ( x == Object.id )          return Object ;
        else if ( x == Commit.id )          return Commit ;
        else if ( x == Abort.id )           return Abort ;
        else if ( x == Checkpoint.id )      return Checkpoint ;
        else
        {
            Log.fatal(JournalEntryType.class, "Unknown type: "+x) ;
            throw new InternalErrorException() ;
        }
    }
}
/*
 * (c) Copyright 2011 Epimorphics Ltd.
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