/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

public class Tags
{
    public static final String LPAREN         = "(" ;
    public static final String RPAREN         = ")" ;
    
    public static final String LBRACKET       = "[" ;
    public static final String RBRACKET       = "]" ;
    
    public static final String LBRACE         = "{" ;
    public static final String RBRACE         = "}" ;
    
    // -- Common terms
    public static final String tagUndef         = "undef" ;
    public static final String tagNull          = "null" ;
    public static final String tagDefault       = "default" ;
    public static final String tagExec          = "exec" ;
    public static final String tagRow           = "row" ;

    // RDF
    public static final String tagGraph         = "graph" ;
    public static final String tagLoad          = "graph@" ;
    public static final String tagTriple        = "triple" ;
    public static final String tagQuad          = "quad" ;
    public static final String tagTriplePath    = "path" ;
    
    public static final String tagBase          = "base" ;
    public static final String tagPrefix        = "prefix" ;
    public static final String tagPrefixMap     = "prefixmap" ;
    public static final String tagPrefixMapping = "prefixmapping" ;

    // SPARQL 
    public static final String tagDataset       = "dataset" ;
    public static final String tagNamedGraph    = "namedgraph" ;
    public static final String tagBinding       = "binding" ;
    public static final String tagTable         = "table" ;
    public static final String tagResultSet     = "resultset" ;

    // SPARQL algebra
    public static final String tagBGP           = "bgp" ;
    public static final String tagQuadPattern   = "quadpattern" ;
    public static final String tagFilter        = "filter" ;
    // public static final String tagGraph = "graph" ;
    public static final String tagLabel         = "label" ;
    public static final String tagService       = "service" ;
    public static final String tagProc          = "proc" ;
    public static final String tagPropFunc      = "propfunc" ;
    public static final String tagJoin          = "join" ;
    public static final String tagSequence      = "sequence" ;
    public static final String tagDisjunction   = "disjunction" ;
    public static final String tagLeftJoin      = "leftjoin" ;
    public static final String tagConditional   = "conditional" ;
    public static final String tagDiff          = "diff" ;
    public static final String tagMinus         = "minus" ;
    public static final String tagUnion         = "union" ;

    public static final String tagToList        = "tolist" ;
    public static final String tagOrderBy       = "order" ;
    public static final String tagGroupBy       = "group" ;
    public static final String tagProject       = "project" ;
    public static final String tagDistinct      = "distinct" ;
    public static final String tagReduced       = "reduced" ;
    public static final String tagAssign        = "assign" ;
    public static final String symAssign        = ":=" ;
    public static final String tagSlice         = "slice" ;
    
    // Paths
    public static final String tagPath          = "path" ;
    public static final String tagPathSeq       = "seq" ;
    public static final String tagPathAlt       = "alt" ;
    public static final String tagPathMod       = "mod" ;
    public static final String tagPathReverse   = "reverse" ;
    public static final String tagPathRev       = "rev" ;
    public static final String pathNotOneOf     = "notoneof" ;

    // Not used - nowadays extensions are not explicitly flagged in the algebra.
    // But needed to override existing operations.
    public static final String tagExt           = "ext" ;
    
    // Expressions
    // sym => swiggly thing, tag => word-ish thing
    
    public static final String tagExpr          = "expr" ;
    public static final String tagExprList      = "exprlist" ;

    public static final String symEQ            = "=" ;
    public static final String symNE            = "!=" ;
    public static final String symGT            = ">" ;
    public static final String symLT            = "<" ;
    public static final String symLE            = "<=" ;
    public static final String symGE            = ">=" ;
    public static final String symOr            = "||" ;
    public static final String tagOr            = "or" ;
    public static final String symAnd           = "&&" ;
    public static final String tagAnd           = "and" ;
    public static final String symPlus          = "+" ;
    public static final String symMinus         = "-" ;
    public static final String symMult          = "*" ;
    public static final String symDiv           = "/" ;
    public static final String symNot           = "!" ;
    
    public static final String tagNot           = "not" ;
    public static final String tagStr           = "str" ;
    public static final String tagLang          = "lang" ;
    public static final String tagLangMatches   = "langmatches" ;
    public static final String tagSameTerm      = "sameterm" ;
    public static final String tagDatatype      = "datatype" ;
    public static final String tagBound         = "bound" ;
    public static final String tagCoalesce      = "coalesce" ;
    public static final String tagIf            = "if" ;
    public static final String tagIRI           = "isIRI" ;
    public static final String tagURI           = "isURI" ;
    public static final String tagIsBlank       = "isBlank" ;
    public static final String tagIsLiteral     = "isLiteral" ;
    public static final String tagRegex         = "regex" ;
    public static final String tagExists        = "exists" ;
    public static final String tagNotExists     = "notexists" ;

    public static final String tagIn             = "in" ;
    public static final String tagNotIn          = "notin" ;
    
    public static final String tagTrue          = "true" ;
    public static final String tagFalse         = "false" ;

    public static final String tagAsc           = "asc" ;
    public static final String tagDesc          = "desc" ;
    
    public static final String tagCount         = "count" ;
    public static final String tagSum           = "sum" ;
    public static final String tagMin           = "min" ;
    public static final String tagMax           = "max" ;
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * (c) Copyright 2010 Talis Information Ltd.
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