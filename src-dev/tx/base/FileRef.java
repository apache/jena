/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.base;

final
public class FileRef
{
    // Symbol<T> ?
    private final String filename ;

    static public FileRef create(String symbolStr) { return new FileRef(symbolStr) ; }
    static public FileRef create(FileRef other) { return new FileRef(other) ; }
    
    private FileRef(String filename)
    {
        // Canonicalise filename.
        if ( filename == null )
            throw new IllegalArgumentException("Null for a FileRef filename") ;
        this.filename = filename.intern() ;
    }
    
    private FileRef(FileRef other)  { this.filename = other.filename ; }
    
    public String getFilename() { return filename ; }
    
    @Override
    public int hashCode()
    {
        final int prime = 37 ;
        int result = 1 ;
        result = prime * result + ((filename == null) ? 0 : filename.hashCode()) ;
        return result ;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        FileRef other = (FileRef)obj ;
        if (filename == null)
        {
            if (other.filename != null) return false ;
        } else
            if (!filename.equals(other.filename)) return false ;
        return true ;
    }
    
    @Override
    public String toString()  { return "file:"+filename ; }
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