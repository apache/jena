/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.transaction.Journal ;
import com.hp.hpl.jena.tdb.transaction.JournalEntry ;

public class Replay
{
    public static void print(Journal journal)
    {
        for ( JournalEntry e : journal )
        {
            System.out.println("Entry: ") ;
            ByteBuffer bb = e.getByteBuffer() ;
            Block blk = e.getBlock() ;
            if ( bb != null )
                System.out.println("  "+bb) ;
            if ( blk != null )
                System.out.println("  "+blk) ;
            System.out.println("  "+e.getFileRef()) ;
            System.out.println("  "+e.getType()) ;
        }
    }
//            switch (e.getType())
//            {
//                case Block:
//                    // Get block ref.
//                    // Find block mgr
//                    // do it.
//                    break ;
//                case Object:
//                    // Get id
//                    // get bytes
//                    // dispatch to NodeTable.
//                    // ?? Node rebuilding?
//                    // Check ids.
//                    break ;
//                case Commit:
//                    break ;
//                case Abort:
//                    break ;
//                case Checkpoint:
//                    //default:
//            }
//        }
//    }
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