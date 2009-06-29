/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.hp.hpl.jena.util.FileUtils;

public class PropertyUtils
{
    /** Java5 does not have read/write from readers/writers - needed for UTF-8 */ 
    
    static public void loadFromFile(Properties properties, String filename) throws IOException
    {
        String x = FileUtils.readWholeFileAsUTF8(filename) ;
        byte b[] = x.getBytes(FileUtils.encodingUTF8) ;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(b);
        properties.load(inputStream) ;
    }
    
    static public void storeToFile(Properties properties, String comment, String filename) throws IOException
    {
        String str = comment ;
        if ( str == null )
            str = filename ;
        FileOutputStream fos = new FileOutputStream(filename) ;
//        Writer w = FileUtils.asUTF8(fos) ;
//        w = new BufferedWriter(w) ;
//        //properties.store(w, "Metadata: "+str) ;   // Java6.
        // Warning - not UTF-8 safe.
        properties.store(fos, str) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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