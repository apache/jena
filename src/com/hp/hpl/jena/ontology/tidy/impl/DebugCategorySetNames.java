/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.impl;

/**
 * @author Jeremy J. Carroll
 *  
 */
class DebugCategorySetNames extends CategorySetNames {

    private static int nameInfo[][] = new int[CategorySetNames.names.length][CategorySetNames.names.length];

	static private String okNames[][] = {
			//		{ "an individual", "an unnamed individual"},
			//		{ "an ontology", "an unnamed ontology"},
			{ "a class description",
					"a class description participating in an owl:disjointWith construct" },
			{ "a class description",
					"a class description participating in an owl:equivalentClass construct" },
			{
					"a class description",
					"a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a class description",
					"a datarange or a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{ "a class description",
					"a class description participating in an rdfs:subClassOf construct" },
			{ "a non-empty list", "a list of literals" },
			{ "a non-empty list",
					"a list of literals or a list of named individuals" },
			{ "a non-empty list", "a list of class expressions" },
			{ "a non-empty list",
					"a list of class expressions or a list of named individuals" },
			{ "a non-empty list", "a list of named individuals" },
			{ "a property restriction", "a restriction on a datatype property" },
			{ "a property restriction",
					"a restriction on a datatype property or on a non-transitive object property" },
			{ "a property restriction",
					"a restriction participating in an owl:disjointWith construct" },
			{ "a property restriction",
					"a description or a restriction participating in an owl:disjointWith construct" },
			{ "a property restriction",
					"a restriction participating in an owl:equivalentClass construct" },
			{
					"a property restriction",
					"a description or a restriction participating in an owl:equivalentClass construct" },
			{
					"a property restriction",
					"a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a property restriction",
					"a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a property restriction",
					"a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a property restriction",
					"a possible member of a list (a class expression, a literal, or a named individual)" },
			{ "a property restriction",
					"a restriction participating in an rdfs:subClassOf construct" },
			{ "a property restriction",
					"a description or a restriction participating in an rdfs:subClassOf construct" },
			{ "a property restriction", "a restriction on an object property" },
			{ "a property restriction",
					"a restriction on a non-transitive object property" },
			{ "a property restriction",
					"a restriction on a transitive object property" },
			{ "a class description or a property restriction",
					"a description or a restriction participating in an owl:disjointWith construct" },
			{
					"a class description or a property restriction",
					"a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a class description or a property restriction",
					"a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{
					"a class description or a property restriction",
					"a possible member of a list (a class expression, a literal, or a named individual)" },
			{
					"a class description or a property restriction or a datarange",
					"a datarange or a description or a restriction participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{ "a class description or a property restriction",
					"a description or a restriction participating in an rdfs:subClassOf construct" },
			{
					"a class description or a property restriction",
					"a description or a restriction participating in an owl:equivalentClass construct" },
			{
					"a class description or a datarange",
					"a datarange or a class description participating as the object of a triple other than rdfs:subClassOf, owl:equivalentClass or owl:disjointWith" },
			{ "a property of some sort",
					"an annotation property, an object property or an ontology property" },
			{ "a property of some sort",
					"an annotation property, a datatype property or an object property" },
			{ "an object property", "a non-transitive object property" },
			{ "an ontology", "a named ontology" },
			{ "an object property", "a transitive object property" },
			{ "an object or datatype property",
					"a datatype property or a non-transitive object property" },
			{ "an individual", "a named individual" },
			{ "a literal", "a non-negative integer" },
			{ "a literal", "a literal other than a non-negative integer" },
			{ "a class expression",
					"a named class or a class description or a datarange" },
			{
					"a class expression",
					"a possible member of a list (a class expression, a literal, or a named individual)" },
			{ "a blank node",
					"a blank node in an owl:AllDifferent construction" },
			{ "a blank node", "a property restriction" },
			{ "a blank node", "a class description or a property restriction" },
			{ "a blank node", "a class expression" },
			{ "a blank node",
					"a class description or a property restriction or a datarange" },
			{ "a blank node", "a generalized class or datatype expression" },
			{ "a restriction on an object property",
					"a restriction on a non-transitive object property" },
			{ "a restriction on an object property",
					"a restriction on a transitive object property" },
			{ "a blank node",
					"a restriction on a datatype property or on a non-transitive object property" },
			{ "a generalized class or datatype expression",
					"a named class or a class description or a datarange" },
			{ "a blank node", "a non-empty list" },
			{ "a blank node", "a possibly empty list" },

	};


    static private boolean usedName[] = new boolean[names.length];

    static void showNameInfo(boolean shorter, int not, int pref) {
        String prefix = shorter ? "!!" : "//";
        if (!shorter) {
            String p = (String) CategorySetNames.names[pref][1];
            String o = (String) CategorySetNames.names[not][1];

            for (int i = 0; i < okNames.length; i++) {
                if (p.equals(okNames[i][0]) && o.equals(okNames[i][1]))
                    return;
            }
            System.err.println("{ \"" + CategorySetNames.names[pref][1]
                    + "\", \"" + CategorySetNames.names[not][1] + "\"},");
        }
        System.err.println(prefix + "Preferred: \""
                + CategorySetNames.names[pref][1] + "\"");
        System.err.println(prefix + "Over: \"" + CategorySetNames.names[not][1]
                + "\"");
        System.err.println(prefix + "Array lengths: "
                + ((int[]) CategorySetNames.names[pref][0]).length + " vs "
                + ((int[]) CategorySetNames.names[not][0]).length
                + "    Desc length: " + descLength(pref) + " vs "
                + descLength(not));

    }

    static int descLength(int nm) {
        Object a[] = CategorySetNames.names[nm];
        return ((String) a[1]).length()
                + (a.length > 2 ? ((String) a[2]).length() : 0);
    }

    // TODO use this code in test case ..
    static boolean anyUsedNames() {
        boolean rslt = false;
        if (CategorySetNames.DEBUG_NAMES)
            for (int i = 0; i < usedName.length; i++)
                if (!usedName[i]) {
                    System.err.println("Unused: \""
                            + CategorySetNames.names[i][1] + "\"");
                    rslt = true;
                }
        return rslt;
    }

    static void debugCatNames(int rslt, int i) {
        {
            if (rslt == i) {
                usedName[rslt] = true;
            } else {
                boolean moreSpecific = ((int[]) names[i][0]).length < ((int[]) names[rslt][0]).length;

                if (((int[]) names[i][0]).length == ((int[]) names[rslt][0]).length
                        && Q
                                .subset((int[]) names[i][0],
                                        (int[]) names[rslt][0])
                        && Q
                                .subset((int[]) names[rslt][0],
                                        (int[]) names[i][0])) {
                    System.err.println("Duplicates!!!!");
                    DebugCategorySetNames.showNameInfo(true, i, rslt);
                }
                boolean shorter = DebugCategorySetNames.descLength(i) < DebugCategorySetNames
                        .descLength(rslt);
                int old = DebugCategorySetNames.nameInfo[i][rslt];
                DebugCategorySetNames.nameInfo[i][rslt] |= 1
                        + (moreSpecific ? 2 : 0) + (shorter ? 4 : 0);
                if ((old == 0 || old == 1) && (moreSpecific || shorter))
                    DebugCategorySetNames.showNameInfo(shorter, i, rslt);
            }
        }
    }

}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
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

