/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dump;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import atlas.io.PeekReader;
import atlas.lib.Bytes;
import atlas.lib.InternalErrorException;

import com.hp.hpl.jena.riot.tokens.Token;
import com.hp.hpl.jena.riot.tokens.TokenType;
import com.hp.hpl.jena.riot.tokens.Tokenizer;
import com.hp.hpl.jena.riot.tokens.TokenizerText;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;
import com.hp.hpl.jena.tdb.sys.Names;

public class DumpIndex
{
    
    public static void dump(OutputStream w, String location, String name)
    {

        RecordFactory rf = null ;

        if ( contains(name, Names.tripleIndexes) )
            rf = FactoryGraphTDB.indexRecordTripleFactory ;
        else if ( contains(name, Names.quadIndexes) )
            rf = FactoryGraphTDB.indexRecordQuadFactory ;
        else if ( contains(name, Names.prefixIndexes) )
            rf = DatasetPrefixesTDB.factory ;
        else if ( name.equals(Names.indexNode2Id) )
            rf = FactoryGraphTDB.nodeRecordFactory ;

        if ( rf == null )
        {
            System.err.printf("Can't determine the RecordFactory for %s\n", name) ;
            return ;
        }

        try
        {
            PrintStream ps = new PrintStream(w, true, "UTF-8") ;
            ps.println(rf) ;
        } catch (UnsupportedEncodingException ex1) {}
        
        FileSet fs = IndexBuilder.filesetForIndex(new Location(location), name) ;
        Index index = IndexBuilder.createIndex(fs, rf) ;
        dump(w, index) ;
    }

    static private boolean contains(String x , String[] strings)
    {
        for ( String s: strings )
        {
            if ( s==null && x == null ) return true ;
            if ( s.equals(x) ) return true ;
        }
        return false ;
    }


    public static void dump(OutputStream w, Index index)
    {
        try
        {
            RecordFactory f = index.getRecordFactory() ;
            
            // Buffer one line.
            // 0x
            // 2 bytes per byte of key
            // If value, space 0x and 2 bytes per byte of value
            // space
            // DOT
            // newline
            int size ;
            if ( f.hasValue() )
                size = 2*f.recordLength()+8 ;
            else
                size = 2*f.keyLength()+5 ;
            byte[] bytes = new byte[size] ;
            
            Iterator<Record> iter = index.iterator() ;
            while( iter.hasNext() )
            {
                Record record = iter.next();
                DumpLib.outputBytes(record, bytes) ;
                w.write(bytes) ;
            }
        } catch (IOException ex)
        {
            throw new InternalErrorException("IOException", ex) ;
        }
    }
    
    public static void reload(InputStream in, Index index)
    {
        RecordFactory f = index.getRecordFactory() ;
        PeekReader pr = PeekReader.makeUTF8(in) ;
        Tokenizer tokenizer = new TokenizerText(pr) ;
        // Read name
        // Read sizes 
        // How do we tell whether it's "key" or "key,value"?
        while ( tokenizer.hasNext() )
        {
            // --TokenInputStream.
            Token t1 = tokenizer.next() ;
            
            byte[] key = hexTokenToBytes(t1) ;
            
            Token t2 = tokenizer.next() ;
            byte[] value = null ;
            if ( ! t2.hasType(TokenType.DOT) )
            {
                value = hexTokenToBytes(t2) ;
                t2 = tokenizer.next() ;
            }
            
            if ( ! t2.hasType(TokenType.DOT) )
            {
                throw new TDBException("Bad index dump file: "+t2) ;
            }
            
            Record record = f.create(key, value) ;
            // BAD WAY TO DO IT.
            // Because we know it's sorted.  Use the BPT rewriter when ready.
            // But, hey, this is functionally correct.
            index.add(record) ;
        }
    }

    private static byte[] hexTokenToBytes(Token token)
    {
        String string = token.getImage() ;
        int N = string.length() ;
        int x = (N-2)/2 ;

        byte[] b = new byte[x] ;
        int idx = 0 ;
        // Read two chars per cycle, skipping 0x
        for(int i = 2 ; i < N ; )
        {
            char c1 = string.charAt(i++) ;
            char c2 = string.charAt(i++) ;
            int hi = Bytes.hexCharToInt(c1) ;
            int lo = Bytes.hexCharToInt(c2) ;
            b[idx++] = (byte)(hi<<4|lo) ;
        }
        return b ;
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