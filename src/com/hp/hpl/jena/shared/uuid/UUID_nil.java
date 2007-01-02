/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.shared.uuid;

/** The nil UUID.  There is only one in the system.
 * 
 * @author Andy Seaborne
 * @version $Id: UUID_nil.java,v 1.2 2007-01-02 11:51:49 andy_seaborne Exp $
 */

public final
class UUID_nil extends JenaUUID
{
    private static final String nilStr = "00000000-0000-0000-0000-000000000000" ;
    private static UUID_nil nil = new UUID_nil() ;
    
    // Constants
    static final int version = 0 ;
    static final int variant = 0 ;

    // The only state-per-object
    long bitsMostSignificant = 0 ;
    long bitsLeastSignificant = 0 ;
    
    private UUID_nil()
    {}
    
    
    public long getMostSignificantBits() { return bitsMostSignificant ; }
    public long getLeastSignificantBits() { return bitsLeastSignificant ; }
    
    public String toString()
    { return nilStr ; }

    public boolean equals(Object other)
    {
        if ( ! ( other instanceof UUID_nil ) )
            return false ;
        UUID_nil x = (UUID_nil)other ;
        return this.bitsMostSignificant == x.bitsMostSignificant &&  this.bitsLeastSignificant == x.bitsLeastSignificant ;
    }


    public int getVariant() { return variant ; }
    public int getVersion() { return version ; }
    
    // Testing only.
    public static UUID_nil getNil() { return nil ; }
    public static String  getNilString() { return nilStr ; }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
