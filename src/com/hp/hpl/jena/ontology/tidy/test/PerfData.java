/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PerfData.java,v 1.1 2004-01-11 21:20:15 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;
import java.util.*;
/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class PerfData {
	static class PD implements Comparable {
		int cnt;
		String name;
		String names[];
		PD(int c, String n, String ns[]) {
			cnt = c;
			name = n;
			names = ns;
		}
		public int compareTo(Object o) {
			PD pd = (PD) o;
			int rslt = cnt - pd.cnt;
			if (rslt != 0)
				return rslt;
			else
				return name.compareTo(pd.name);
		}
	}
	PD all[];
	void load() {
		all = new PD[SyntaxTest.cnts.size()];
		for (int i = 0; i < SyntaxTest.cnts.size(); i++) {
			all[i] =
				new PD(
					((Integer) SyntaxTest.cnts.get(i)).intValue(),
					(String) SyntaxTest.first.get(i),
					(String[]) SyntaxTest.files.get(i));

		}
		Arrays.sort(all);
	}
	void dump() {
		System.out.println("int cnt[] = {");
		for (int i=0;i<all.length;i++) {
			System.out.print(" "+all[i].cnt+",");
			if (i%5 == 4)
			 System.out.println();
		}
		System.out.println("\n  };");
		System.out.println("String name[] = {");
		for (int i=0;i<all.length;i++) {
			System.out.print(" \""+all[i].name+"\",");
			if (i%5 == 4)
			 System.out.println();
		}
		System.out.println("\n  };");
		System.out.println("String names[][] = {");
		for (int i=0;i<all.length;i++) {
			System.out.print(" {");
			for (int j=0;j<all[i].names.length;j++) {
				if ( !all[i].names[j].equals(all[i].name))
				 System.out.print(" \""+all[i].names[j]+"\",");
			}
			System.out.print(" },");
			if (i%5 == 4)
			 System.out.println();
		}
		System.out.println("\n  };");
		  
	}
}

/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
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