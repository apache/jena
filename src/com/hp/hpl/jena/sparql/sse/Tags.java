/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

public class Tags
{
    static public final String LPAREN         = "(" ;
    static public final String RPAREN         = ")" ;
    
    static public final String LBRACKET       = "[" ;
    static public final String RBRACKET       = "]" ;
    
    static public final String LBRACE         = "{" ;
    static public final String RBRACE         = "}" ;
    
    // -- Common terms
    static public final String tagUndef         = "undef" ;
    static public final String tagNull          = "null" ;
    static public final String tagDefault       = "default" ;
    static public final String tagExec          = "exec" ;
    static public final String tagRow           = "row" ;

    // RDF
    static public final String tagGraph         = "graph" ;
    static public final String tagLoad          = "graph@" ;
    static public final String tagTriple        = "triple" ;
    static public final String tagQuad          = "quad" ;
    
    static public final String tagBase          = "base" ;
    static public final String tagPrefix        = "prefix" ;
    static public final String tagPrefixMap     = "prefixmap" ;
    static public final String tagPrefixMapping = "prefixmapping" ;

    // SPARQL 
    static public final String tagDataset       = "dataset" ;
    static public final String tagNamedGraph    = "namedgraph" ;
    static public final String tagBinding       = "binding" ;
    static public final String tagTable         = "table" ;
    static public final String tagResultSet     = "resultset" ;

    // SPARQL algebra
    static public final String tagBGP           = "bgp" ;
    static public final String tagQuadPattern   = "quadpattern" ;

    static public final String tagFilter        = "filter" ;
    // static public final String tagGraph = "graph" ;
    static public final String tagService       = "service" ;
    static public final String tagJoin          = "join" ;
    static public final String tagLeftJoin      = "leftjoin" ;
    static public final String tagDiff          = "diff" ;
    static public final String tagUnion         = "union" ;

    static public final String tagToList        = "tolist" ;
    static public final String tagOrderBy       = "order" ;
    static public final String tagGroupBy       = "group" ;
    static public final String tagProject       = "project" ;
    static public final String tagDistinct      = "distinct" ;
    static public final String tagReduced       = "reduced" ;
    static public final String tagAssign        = "assign" ;
    static public final String symAssign        = ":=" ;
    static public final String tagSlice         = "slice" ;
    
    // Expressions
    // sym => swiggly thing, tag => word-ish thing
    
    static public final String tagExpr          = "expr" ;
    static public final String tagExprList      = "exprlist" ;

    static public final String symEQ            = "=" ;
    static public final String symNE            = "!=" ;
    static public final String symGT            = ">" ;
    static public final String symLT            = "<" ;
    static public final String symLE            = "<=" ;
    static public final String symGE            = ">=" ;
    static public final String symOr            = "||" ;
    static public final String tagOr            = "or" ;
    static public final String symAnd           = "&&" ;
    static public final String tagAnd           = "and" ;
    static public final String symPlus          = "+" ;
    static public final String symMinus         = "-" ;
    static public final String symMult          = "*" ;
    static public final String symDiv           = "/" ;
    static public final String symNot           = "!" ;
    static public final String tagNot           = "not" ;
    static public final String tagStr           = "str" ;
    static public final String tagLang          = "lang" ;
    static public final String tagLangMatches   = "langmatches" ;
    static public final String tagSameTerm      = "sameterm" ;
    static public final String tagDatatype      = "datatype" ;
    static public final String tagBound         = "bound" ;
    static public final String tagIRI           = "isIRI" ;
    static public final String tagURI           = "isURI" ;
    static public final String tagIsBlank       = "isBlank" ;
    static public final String tagIsLiteral     = "isLiteral" ;
    static public final String tagRegex         = "regex" ;

    static public final String tagTrue          = "true" ;
    static public final String tagFalse         = "false" ;

    static public final String tagAsc           = "asc" ;
    static public final String tagDesc          = "desc" ;
    
    static public final String tagCount         = "count" ;
    
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */