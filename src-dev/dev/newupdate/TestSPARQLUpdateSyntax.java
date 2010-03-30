/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev.newupdate;

import java.io.Reader ;
import java.io.StringReader ;

import org.junit.Test ;

import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.lang.sparql_11.SPARQLParser11 ;
import com.hp.hpl.jena.sparql.util.ALog ;

import dev.RunARQ ;

public class TestSPARQLUpdateSyntax
{
    /*
    ( Modify()
        | Load()
        | Clear()
        | Drop()
        | Create()
        )
    */
    

    
    @Test public void update_0() { parse("") ; }
    @Test public void load_1() { parse("LOAD <foo> INTO <blah>") ; }
    @Test public void load_2() { parse("LOAD <foo1> <foo2> INTO <blah>") ; }

    // ??
    @Test public void load_3() { parse("LOAD <foo> INTO DEFAULT") ; }
    @Test public void load_4() { parse("LOAD <foo>") ; }

    @Test(expected=QueryParseException.class) 
    public void clear_01() { parse("CLEAR") ; }
    
    @Test public void clear_02() { parse("CLEAR  DEFAULT") ; }
    @Test public void clear_03() { parse("CLEAR <g>") ; }
    
    // Drop
    
    @Test public void drop_01() { parse("DROP <g>") ; }
    
    @Test(expected=QueryParseException.class) 
    public void drop_02() { parse("DROP") ; }

    @Test(expected=QueryParseException.class) 
    public void drop_03() { parse("DROP DEFAULT") ; }
    
    // Create
    @Test public void create_01() { parse("CREATE <g>") ; }
    
    @Test(expected=QueryParseException.class) 
    public void create_02() { parse("CREATE") ; }

    @Test(expected=QueryParseException.class) 
    public void create_03() { parse("CREATE DEFAULT") ; }
    
    // INSERT
    @Test public void insert_01() { parse("INSERT DATA { <X> <p> 123 }") ; }
    @Test public void insert_02() { parse("WITH <x> INSERT DATA { <X> <p> 123 }") ; }
    @Test public void insert_03() { parse("WITH <x> INSERT DATA { <X> <p> 123 GRAPH <g> { <x1> <p1> 456 . } }") ; }
    @Test public void insert_04() { parse("WITH <x> INSERT DATA { <X> <p> 123 GRAPH<g> { <x1> <p1> 456 . }  <X> <p> 123 }") ; }
    @Test public void insert_05() { parse("WITH <x> INSERT DATA { <X> <p> 123 GRAPH<g> { <x1> <p1> 456 . }  GRAPH<g> {<X> <p> 123 } } ") ; }

    
    @Test (expected=QueryParseException.class)
    public void insert_bad_01() { parse("WITH <x> INSERT WHERE { ?x ?p ?o }") ; }
    
    @Test (expected=QueryParseException.class)
    public void insert_bad_02() { parse("INSERT WHERE { GRAPH <g> { ?x ?p ?o } }") ; }
    
    // DELETE
    @Test public void delete_01() { parse("DELETE WHERE { ?x ?p ?o }") ; }
    @Test public void delete_02() { parse("WITH <x> DELETE WHERE { ?x ?p ?o }") ; }
    @Test public void delete_03() { parse("DELETE WHERE { GRAPH <g> { ?x ?p ?o } }") ; }

    // DELETE+INSERT
    @Test public void delete_insert_01() { parse("DELETE { ?x ?p ?o } WHERE { GRAPH <g> { ?x ?p ?o } }") ; }
    @Test public void delete_insert_02() { parse("DELETE { ?x ?p ?o } INSERT { ?x ?p ?o } WHERE { GRAPH <g> { ?x ?p ?o } }") ; }
    @Test public void delete_insert_03() { parse("INSERT { ?x ?p ?o } WHERE { GRAPH <g> { ?x ?p ?o } }") ; }

    
    // Compound
    @Test public void update_01() { parse("CREATE <g> CLEAR <G>  LOAD <data> INTO <G> WITH <x> DELETE WHERE { ?x ?p ?o }") ; }
    @Test public void update_02() { parse("CREATE <g> ; CLEAR <G> ; LOAD <data> INTO <G> ; WITH <x> DELETE WHERE { ?x ?p ?o }") ; }
    
    
    private static void parse(String str)
    {
        //System.out.println(str);
        Reader r = new StringReader(str) ;
        SPARQLParser11 parser = null ;
        try {
            parser = new SPARQLParser11(r) ;
            parser.setUpdateRequest(null) ;
            parser.UpdateUnit() ;
            //System.out.println();
            //validateParsedUpdate(update) ;
            System.out.println(str);
            System.out.println();
        }
        catch (com.hp.hpl.jena.sparql.lang.sparql_11.ParseException ex)
        { 
            throw new QueryParseException(ex.getMessage(),
                                          ex.currentToken.beginLine,
                                          ex.currentToken.beginColumn
            ) ; }
        catch (com.hp.hpl.jena.sparql.lang.sparql_11.TokenMgrError tErr)
        {
            // Last valid token : not the same as token error message - but this should not happen
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new QueryParseException(tErr.getMessage(), line, col) ; }

        catch (QueryException ex) { throw ex ; }
        catch (JenaException ex)  { throw new QueryException(ex.getMessage(), ex) ; }
        catch (Error err)
        {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1) ;
        }
        catch (Throwable th)
        {
            ALog.fatal(RunARQ.class, "Unexpected throwable: ",th) ;
            throw new QueryException(th.getMessage(), th) ;
        }
    }

}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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