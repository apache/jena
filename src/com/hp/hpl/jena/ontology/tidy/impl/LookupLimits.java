/*
   (c) Copyright 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: LookupLimits.java,v 1.2 2003-12-13 21:10:50 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;

/**
 * Bit fields etc, used by {@link LookupTable}, both
 * internally and in the opaque key returned to 
 * {@link AbsChecker}
 * <p>
 * The integers in the main tables
 * have the following form:
 * <pre>
 * 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16
 *  0 [*** SUBJECT **9 bits****] [* NEW SUBJ 6b *]
 * 
 * 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00
 * [*NEW P] [*NEW OBJ 5b*] [** ACTION 8bits *****]
 * </pre>
 * <ul>
 * <li>
 * The *NEW SUBJ* id ranges from 1 to 46
 * <li>
 * The *NEW P* from 0 to 4
 * <li>
 * The *NEW OBJ* from 0 to 23
 * </ul>
 * NOTE: the new subj is counted from 1, the others from 0.
 * This is to ensure that at least one of the fields
 * other than SUBJECT is non-zero, which adds to uniformity
 * since we use (SUBJECT 0 0 0 0) as a key, which we
 * know is <em>not</em> in the array of values.
 * <p>
 * The NEW SUBJ, NEW OBJ and NEW PRED values
 * are used in conjuction with the old value
 * of these fields to select the new value.
 * <p>
 * These are the values passed between the 
 * AbsChecker and the LookupTable.
 * 
 * <p>
 * At the time of design there were the following
 * numeric facts:
 * <ul>
 * <li>
 * just under 400 categories
 * <li>
 *  342 can be subjects
 * <li>
 * 
 *   52 can be predicates
 * <li>
 * 
 *  356 can be objects
 * <li>
 * 
 *  For each of the 52 predicates 
 *  the refinement selects one of at most 5 refined 
 * predicates
 * <li>
 * 
 *  For each of the 356 objects 
 *  the refinement selects on of at most 24 
 * refined objects (1 with 24, the others 14 or less)
 * <li>
 * 
 *  For each of the 342 subjects the refinement 
 * selects one of at most 46 refined subjects
 * (1 with 46 the others with 23 or less)
 * <li>
 * 
 * The action is in fact only 6 bits
 * (a 7th is planned)
 * </ul>
 * <p>
 * The compiler prints out these facts as it operates.
 * <p>
 * Theoretically it would be possible to compress a 
 * bit further ...
 * If we treated the subject with 46 refinements as
 * a special case, noting that 
 * 24*5*52*23*356*64 = 3269959680
 * which is less than 2^32 it would be
 * possible to pack a prop and object
 * and the new fields and the action into 4 bytes.
 * <p>
 * However, this is made difficult because:
 * <ul>
 * <li>
 * the need for the special case subject
 * <li>
 * unsigned arithmetic in java is hard.
 * </ul>
 * Given that we have not got either the
 * object or the predicate in the 32 bits
 * we have to select on them earlier.
 * <p>
 * This makes the arrangement of the tables of data
 * as arrays somewhat complex, being
 * indexed by object and a prop-index
 * The appropriate prop-index is found by looking
 * for the property in an additional array
 * indexed by object (well, that was as clear as mud).
 * See the code of {@link LookupTable#qrefine} for details of the indexing,
 * that code is reasonably small, and should help elucidate.
 * <p>
 * The compiler must check that no field overflow
 * occurs. If the grammar changes and the current
 * limits prove insufficient, here are some areas
 * of slack:
 * <ul>
 * <li>
 * it should be possible to use the top-bit
 *   (since we are using bitfields rather than
 *    % and /)
 *   However, given that essentially this would
 *   be using a signed int as if it were unsigned
 *   some care may be needed.
 * <li> the action field is currently only 6 bits
 *   the top two bits are unused (verify before
 *   modifying)
 * <li>one bit of the new subj field and one bit
 *   of the new obj field are used very rarely,
 *   an overflow mechanism could be used for 
 *   those rare cases - it would probably be best
 *   to correct the overflow in qrefine and
 *   then use the high bits of the key returned
 *   to AbsChecker for the return result.
 *   (the SUBJECT field is current set but not 
 *   actually needed in the return result of 
 *   qrefine).
 * </ul>
 * 
 *
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public interface LookupLimits {
	/** Limits for {@link CategorySet} */
	static final int CATWIDTH = 9;
	/** Limits for {@link CategorySet} */
	static final int MAXCAT   = (1<<CATWIDTH)-2;
	/** Subject field */
  static final int SUBJSHIFT = 22;
  /** Subject field */
  static final int SUBJWIDTH = CATWIDTH;
  /** Subject field */
  static final int SUBJMAX = (1<<SUBJWIDTH)-1;
  /** Subject field */
  static final int SUBJMASK  = SUBJMAX <<SUBJSHIFT;
  /** New subject selector field */
  static final int NSUBJSHIFT = 16;
  /** New subject selector field */
  static final int NSUBJWIDTH = 6;
  /** New subject selector field */
  static final int NSUBJMAX   = (1<<NSUBJWIDTH)-1;
  /** New subject selector field */
  static final int NSUBJMASK  = NSUBJMAX<<NSUBJSHIFT;
  /** New property selector field */
  static final int NPROPSHIFT = 13;
  /** New property selector field */
  static final int NPROPWIDTH = 3;
  /** New property selector field */
  static final int NPROPMAX = (1<<NPROPWIDTH)-1;
  /** New property selector field */
  static final int NPROPMASK  = (NPROPMAX)<<NPROPSHIFT;
  /** New object selector field */
  static final int NOBJSHIFT = 8;
  /** New object selector field */
  static final int NOBJWIDTH = 5;
  /** New object selector field */
  static final int NOBJMAX   = (1<<NOBJWIDTH)-1;
  /** New object selector field */
  static final int NOBJMASK  = NOBJMAX<<NOBJSHIFT;
  /** Action field */
  static final int ACTIONSHIFT = 0;
  /** New object selector field */
  static final int ACTIONWIDTH = 8;
  /** New object selector field */
  static final int ACTIONMAX   = (1<<ACTIONWIDTH)-1;
  /** New object selector field */
  static final int ACTIONMASK  = ACTIONMAX<<ACTIONSHIFT;
}

/*
   (c) Copyright 2003 Hewlett-Packard Development Company, LP
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/