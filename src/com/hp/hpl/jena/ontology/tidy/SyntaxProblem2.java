/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy;

/**
 * 
 * Instances of this class encapsulate a
 * single syntax error within OWL.
 * @author Jeremy J. Carroll
 *
 */
public abstract class SyntaxProblem2 {
    final static public int BLANK_PROP = 1;

    final static public int LITERAL_PROP = 2;

    final static public int LITERAL_SUBJ = 3;

    final static public int BADID_USE = 4;

    final static public int BUILTIN_NON_PRED = 5;

    final static public int BUILTIN_NON_SUBJ = 6;

    final static public int TYPE_NEEDS_ID = 10;

    final static public int TYPE_NEEDS_BLANK = 11;

    final static public int TYPE_NEEDS_ID_OR_BLANK = 12;

    final static public int TYPE_OF_CLASS_ONLY_ID = 13;

    final static public int TYPE_OF_PROPERTY_ONLY_ID = 14;

    final static public int TYPE_FOR_BUILTIN = 15;

    final static private int ST_DOMAIN_RANGE = 20;

    final static public int BAD_DOMAIN = ST_DOMAIN_RANGE + 1;

    final static public int BAD_RANGE = ST_DOMAIN_RANGE + 2;

    final static public int BAD_DOMAIN_RANGE = ST_DOMAIN_RANGE + 3;

    protected final static int DIFFERENT_CATS = 30;

    final static public int DIFFERENT_CATS_S = DIFFERENT_CATS + 1;

    final static public int DIFFERENT_CATS_P = DIFFERENT_CATS + 2;

    final static public int DIFFERENT_CATS_O = DIFFERENT_CATS + 4;

    protected final static int DC_DOM_RANGE = 40;

    final static public int INCOMPATIBLE_SP = DC_DOM_RANGE + 3;

    final static public int INCOMPATIBLE_SO = DC_DOM_RANGE + 5;

    final static public int INCOMPATIBLE_PO = DC_DOM_RANGE + 6;
    
    final static public int SINGLE_TRIPLE_DUPLICATE_NODE = 50;
    final static public int MULTIPLE_TRIPLE_DUPLICATE_NODE = 60;
    

    abstract public int getTypeCode();
    abstract public String getMessage();

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
