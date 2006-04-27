/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

/** A class to create and recreate UUIDs.
 * @author   Andy Seaborne
 * @version  $Id: UUID.java,v 1.4 2006-04-27 10:49:08 der Exp $
 * http://www.opengroup.org/onlinepubs/009629399/apdxa.htm
 */

package com.hp.hpl.jena.shared.uuid;

import java.security.* ;
import java.net.InetAddress ;
import java.net.UnknownHostException ;
import java.io.ByteArrayInputStream;

import com.hp.hpl.jena.JenaRuntime;

public class UUID
{
    // The UUID is string format.
    
    String uuid = null ;

    int version ;
	int variant ;
    
    protected static final String nilStr = "00000000-0000-0000-0000-000000000000" ;
    protected static UUID nil = new UUID(0,0) ;
    
    static final int HEX = 16 ;
    public static boolean useSecureRandom = true ;

    
    // Initialize a UUID - special case of nil UUID
    UUID(int ver, int var)
    {
    	uuid = null ;
		version = ver ;
		variant = var ;
        if ( ver == 0 && var == 0 )
            uuid = nilStr ;
	}
    
    public boolean equals(UUID u)
    {
        return uuid.equals(u.uuid) ;
    }

    public String toString() { return uuid ; }
    /** Format as a URI - that is uuid:ABCD */
    public String asURI() { return "uuid:"+uuid ; }
    /** Format as a URN - that is urn:uuid:ABCD */
    public String asURN() { return "urn:uuid:"+uuid ; }

	// ----------------------------------------------------		
	// Factory    

    
    // Keep these in step.
    public static void reset()    { UUID_V1.reset() ; }
    public static void uninit()   { UUID_V1.uninit() ; }
    public static void init()     { UUID_V1.init() ; }
    
    /** Create a UUID */
    public static UUID create()   { return new UUID_V1() ; }
    /** Create a UUID variant 1 (time based) */
    public static UUID createV1() { return new UUID_V1() ; }
    /** Create a UUID variant 4 (hash based) */
    public static UUID createV4() { return new UUID_V4() ; }
    
    /** The nil UUID */
    public static UUID nilUUID()   { return nil ; }

    /** Recreate a UUID from string*/
    public static UUID create(String s)
    {
    	if ( s.equals(nilStr) )
    	{
    		if ( nil == null )
    		{
	    		nil = new UUID(0,0) ;
				nil.uuid = nilStr ;
    		}
    		return nil ;
    	}
    	
    	
        // Canonical: this works in conjunction with .equals
        s = s.toLowerCase() ;

        if ( s.startsWith("urn:") )
            s = s.substring(4) ;
        if ( s.startsWith("uuid:") )
            s = s.substring(5) ;

        if ( s.length() != 36 )
            throw new FormatException("UUID string is not 36 chars long: it's "+s.length()+" ["+s+"]") ;

        if ( s.charAt(8)  != '-' || s.charAt(13) != '-' || s.charAt(18) != '-' || s.charAt(23) != '-' )
            throw new FormatException("String does not have dashes in the right places: "+s) ;

		//       00000000-0000-0000-0000-000000000000
		//       ^        ^    ^    ^    ^           
		// Byte: 0        4    6    8    10
		// Char: 0        9    14   19   24  inclusing hyphens
		
		// Variant: variable number of MSB of octet 8
		// We are taking the string apreat so allow for hyphens
		
		int octet8 = Integer.parseInt(s.substring(19,21), HEX) ;

		if ( (octet8 & 0x80) == 0 )
		{
			System.out.println(s) ;
			System.out.println("Octet8: "+Integer.toHexString(octet8)) ;
			System.out.println("Oh look! An NCS UUID ID.  Call the museum.") ;
			UUID u = new UUID(0, 0) ;
			u.uuid = s ;
			return u ;
		}
		
		final int DCE = 2 ;
		if ( (octet8 >> 6) == DCE ) // High bits 1 0
		{
			String tmp = s.substring(14,15) ;
			int version = Integer.parseInt(tmp,HEX) ;
			if ( version == 1 )
				return new UUID_V1(s) ;
			
			if ( version == 4 )
				return new UUID_V4(s) ;
			
			throw new FormatException("String specifies unsupport DCE version ("+version+"): "+s) ;
		}
		

		final int MS_GUID = 6 ;			// MSB bits: 110 = 6
		
		if ( ( octet8 >> 5 ) == MS_GUID )
		{
			UUID u = new UUID(0, MS_GUID) ;
			u.uuid = s ;
			return u ;
		}

		final int RESERVED = 7 ;			// MSB: 111
		if ( (octet8>>5) == RESERVED )
		{
			throw new RuntimeException("UUID: UUID string with reserved variant") ;
			//UUID u = new UUID(0, RESERVED) ;
			//u.uuid = s ;
			//return u ;
		}

		throw new FormatException("String specifies unsupport UUID variant: octet8 is 0x"+s.substring(19,20)) ;
		//return null ;	
    }
    
	// ----------------------------------------------------
	// Worker functions
	
//	private void copyBytes(byte[] dst, int dst_i, byte[] src, int src_i, int len)
//	{ }
	
    //  4-2-2-6 bytes
    //  8-4-4-12 hex digits
    
    // Bit starts are:
    // 4     2      2      2      6
    // 0-31  32-47  48-63  64-79  80-127
    
    //static int[] bitIndex = int[]

    protected String extract(String s, int start, int finish) throws FormatException
    {
        String tmp = s.substring(start, finish) ;
        try {
            long l = Long.parseLong(tmp, 16) ;
            return tmp ;
        } catch (NumberFormatException e)
        {
            if ( finish-start == 1 )
                throw new FormatException("Hex parse error (octet "+start+"["+tmp+"]) in "+s) ;
            else
                throw new FormatException("Hex parse error (octets "+start+"-"+finish+"["+tmp+"]) in "+s) ;
            //return null ;
        }
    }
    
    // Derived from MD5.java
    static String stringify (byte buf[]) {
        StringBuffer sb = new StringBuffer(2*buf.length) ;
        for (int i = 0 ; i < buf.length; i++) {
            int h = (buf[i] & 0xf0) >> 4 ;
            int l = (buf[i] & 0x0f) ;
            sb.append (new Character((char)((h>9) ? 'a'+h-10 : '0'+h))) ;
            sb.append (new Character((char)((l>9) ? 'a'+l-10 : '0'+l))) ;
        }
        return sb.toString() ;
    }
    
  	static public class FormatException extends RuntimeException
	{
		public FormatException()
		{
			super();
		}

		public FormatException(String msg)
		{
			super(msg);
		}
	}

	private static boolean warningSent = false ;
	
	static byte[] makeSeed()
	{
		// Seed for the random number generator
        
        byte[] seed = null ;
        
        //Thread current = Thread.currentThread() ;
        StringBuffer nid = new StringBuffer(200) ;
        
		try { nid.append(InetAddress.getLocalHost().getHostAddress()) ; }
		catch (UnknownHostException ex) { }       
		
        nid.append(JenaRuntime.getSystemProperty("os.version")) ;
        nid.append(JenaRuntime.getSystemProperty("user.name")) ;
        nid.append(JenaRuntime.getSystemProperty("java.version")) ;
		nid.append(Integer.toString(Thread.activeCount())) ;
		nid.append(Long.toString(Runtime.getRuntime().freeMemory())) ;
		nid.append(Long.toString(Runtime.getRuntime().totalMemory())) ;
   		nid.append(Long.toString(System.currentTimeMillis())) ;
   		
		try {
			MessageDigest md_sha = MessageDigest.getInstance("SHA");
			seed = md_sha.digest(nid.toString().getBytes()) ;
		} catch (NoSuchAlgorithmException ex)
    	{
    		if ( ! warningSent )
    		{
	    		System.err.println("No SHA message digest.") ;
	    		warningSent = true ;
    		}
	        MD5 md5 = new MD5(new ByteArrayInputStream(nid.toString().getBytes())) ;
    	    md5.processString();
    	    seed = md5.processString() ;
    	}
		
		return seed ;
	}

}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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
