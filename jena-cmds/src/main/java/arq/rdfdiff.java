/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package arq;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * A program which read two RDF models and provides a basic triple level diff
 *
 * <p>
 * This program will read two RDF models, in a variety of languages, and compare
 * them providing a basic triple level diff output. Since blank nodes are a
 * complicating factor diffs for blank node containing portions of the graph are
 * reported in terms of sub-graphs rather than individual triples.
 * </p>
 * <p>
 * Input can be read either from a URL or from a file. The program writes its
 * results to the standard output stream and sets its exit code to 0 if the
 * models are equal, to 1 if they are not and to -1 if it encounters an error.
 * </p>
 *
 * <p>
 * </p>
 *
 * <pre>
 * java jena.rdfdiff model1 model2 lang1? lang2? base1? base2?
 * </pre>
 */
public class rdfdiff extends java.lang.Object {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (args.length < 2 || args.length > 6) {
			usage();
			System.exit(-1);
		}

		String in1 = args[0];
		String in2 = args[1];
		String lang1 = "RDF/XML";
		if (args.length >= 3) {
			lang1 = args[2];
		}
		String lang2 = "N-TRIPLE";
		if (args.length >= 4) {
			lang2 = args[3];
		}
		String base1 = null;
		if (args.length >= 5) {
			base1 = args[4];
		}
		String base2 = base1;
		if (args.length >= 6) {
			base2 = args[5];
		}

        //System.out.println(in1 + " " + in2 + " " + lang1 + " " + lang2 + " " + base1 + " " + base2);
        try {
            Model m1 = ModelFactory.createDefaultModel();
            Model m2 = ModelFactory.createDefaultModel();

			read(m1, in1, lang1, base1);
			read(m2, in2, lang2, base2);

			if (m1.isIsomorphicWith(m2)) {
				System.out.println("models are equal");
				System.out.println();
				System.exit(0);
			} else {
				System.out.println("models are unequal");
				System.out.println();

				if (m1.size() != m2.size()) {
					System.out.println(String.format("source1: %,d triples", m1.size()));
					System.out.println(String.format("source2: %,d triples", m2.size()));
				}

				// Calculate differences
				Map<AnonId, Model> m1SubGraphs = new HashMap<>();
				StmtIterator iter = m1.listStatements();
				while (iter.hasNext()) {
					Statement stmt = iter.next();
					// blank nodes are somehow seen as concrete
					if (isConcrete(stmt)) {
//                    if (stmt.asTriple().isConcrete()) {
                        if (!m2.contains(stmt)) {
                            System.out.print("not found in source2: ");
                            System.out.println(stmt.toString());
                        }
                    } else {
					// Handle blank nodes via sub-graphs
					addToSubGraph(stmt, m1SubGraphs);
                    }
				}

				Map<AnonId, Model> m2SubGraphs = new HashMap<>();
				iter = m2.listStatements();
				while (iter.hasNext()) {
					Statement stmt = iter.next();
					if (isConcrete(stmt)) {
//                    if (stmt.asTriple().isConcrete()) {
                        if (!m1.contains(stmt)) {
                            System.out.print("not found in source1: ");
                            System.out.println(stmt.toString());
                        }
                    } else {
					// Handle blank nodes via sub-graphs
					addToSubGraph(stmt, m2SubGraphs);
                    }
				}

				// Compute sub-graph differences

				// Reduce to sets
				Set<Model> m1SubGraphSet = new TreeSet<>(new ModelReferenceComparator());
				m1SubGraphSet.addAll(m1SubGraphs.values());
				Set<Model> m2SubGraphSet = new TreeSet<>(new ModelReferenceComparator());
				m2SubGraphSet.addAll(m2SubGraphs.values());

//				System.out.println("==="); 
//				m1SubGraphSet.stream().forEach(e -> {
//					e.write(System.out, "TTL"); 
//				});
//				System.out.println("---"); 
//				m2SubGraphSet.stream().forEach(e -> {
//					e.write(System.out, "TTL"); 
//				});
//				System.out.println("==="); 

				if (m1SubGraphSet.size() != m2SubGraphSet.size()) {
				}
				if (m1SubGraphSet.size() > 0) {
					System.out.println("\nsource1: " + m1SubGraphs.size() + " sub-graphs");
					System.out.println("not found in source 2:");
					diffSubGraphs(m1SubGraphSet, m2SubGraphSet, "");
				}
				if (m2SubGraphSet.size() > 0) {
					System.out.println("\nsource2: " + m2SubGraphs.size() + " sub-graphs");
					System.out.println("not found in source 1:");
					diffSubGraphs(m2SubGraphSet, m1SubGraphSet, "");
				}

				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println("Unhandled exception:");
			System.err.println("    " + e.toString());
			System.exit(-1);
		}
	}
	
	private static boolean isConcrete(Statement stmt) {
		return !(stmt.getSubject().isAnon() || stmt.getObject().isAnon());
	}

	private static void diffSubGraphs(Set<Model> m1SubGraphSet, Set<Model> m2SubGraphSet, String prefix) {
		for (Model subGraph : m1SubGraphSet) {

			// Find candidate matches
			List<Model> candidates = new ArrayList<>();
			for (Model subGraphCandidate : m2SubGraphSet) {
				if (subGraph.size() == subGraphCandidate.size()) {
					candidates.add(subGraphCandidate);
				}
			}

			if (candidates.size() == 0) {
				// No match
				printNonMatchingSubGraph(prefix, subGraph);
			} else if (candidates.size() == 1) {
				// Precisely 1 candidate
				if (!subGraph.isIsomorphicWith(candidates.get(0))) {
					printNonMatchingSubGraph(prefix, subGraph);
				} else {
					m2SubGraphSet.remove(candidates.get(0));
				}
			} else {
				// Multiple candidates
				boolean matched = false;
				for (Model subGraphCandidate : candidates) {
					if (subGraph.isIsomorphicWith(subGraphCandidate)) {
						// Found a match
						matched = true;
						m2SubGraphSet.remove(subGraphCandidate);
						break;
					}
				}

				if (!matched) {
					// Didn't find a match
					printNonMatchingSubGraph(prefix, subGraph);
				}
			}
		}
	}

	private static void printNonMatchingSubGraph(String prefix, Model subGraph) {
		StmtIterator sIter = subGraph.listStatements();
		while (sIter.hasNext()) {
			System.out.print(prefix);
			System.out.println(sIter.next().toString());
		}
	}

	private static void addToSubGraph(Statement stmt, Map<AnonId, Model> subGraphs) {
		Set<AnonId> ids = new HashSet<>();

		addToIdList(stmt, ids);

		// See whether one of the blank nodes was already encountered
		Model subGraph = null;
		for (AnonId id : ids) {
			if (subGraphs.containsKey(id)) {
				subGraph = subGraphs.get(id);
				break;
			}
		}

		// If not, create a new subgraph for the statement
		if (subGraph == null) {
//    		subGraph = Closure.closure(stmt);
			subGraph = ModelFactory.createDefaultModel();
		}

		subGraph.add(stmt);

		// (was already indexed on these when processing the earlier statements)
		// Find any further IDs that occur in the sub-graph
//        StmtIterator sIter = subGraph.listStatements();
//        while (sIter.hasNext()) {
//            addToIdList(sIter.next(), ids);
//        }

		// Associate the sub-graph with all of the statement's blank node IDs
		for (AnonId id : ids) {
//            if (subGraphs.containsKey(id))
//                throw new IllegalStateException(String.format("ID %s occurs in multiple sub-graphs", id));
			subGraphs.put(id, subGraph);
		}
	}

	private static void addToIdList(Statement stmt, Set<AnonId> ids) {
		if (stmt.getSubject().isAnon()) {
			ids.add(stmt.getSubject().getId());
		}
		if (stmt.getObject().isAnon()) {
			ids.add(stmt.getObject().asResource().getId());
		}
	}

	protected static void usage() {
		System.err.println("usage:");
		System.err.println("    java jena.rdfdiff source1 source2 [lang1 [lang2 [base1 [base2]]]]");
		System.err.println();
		System.err.println("    source1 and source2 can be URL's or filenames");
		System.err.println("    lang1 and lang2 can take values:");
		System.err.println("      RDF/XML");
		System.err.println("      N-TRIPLE");
		System.err.println("      TTL");
		System.err.println("    lang1 defaults to RDF/XML, lang2 to N-TRIPLE");
		System.err.println("    base1 and base2 are URIs");
		System.err.println("    base1 defaults to null");
		System.err.println("    base2 defaults to base1");
		System.err.println("    If no base URIs are specified Jena determines the base URI based on the input source");
		System.err.println();
	}

	protected static void read(Model model, String in, String lang, String base) throws java.io.FileNotFoundException {
		try {
			URL url = new URL(in);
			model.read(in, base, lang);
		} catch (java.net.MalformedURLException e) {
			model.read(new FileInputStream(in), base, lang);
		}
	}

	private static class ModelReferenceComparator implements Comparator<Model> {

		@Override
		public int compare(Model o1, Model o2) {
			if (o1 == o2)
				return 0;
			int h1 = System.identityHashCode(o1);
			int h2 = System.identityHashCode(o2);

			if (h1 == h2)
				return 0;
			return h1 < h2 ? -1 : 1;
		}

	}
}