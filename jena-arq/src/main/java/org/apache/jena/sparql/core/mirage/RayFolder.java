package org.apache.jena.sparql.core.mirage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.TriTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RayFolder implements Ray {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RayFolder.class);

	public static final Predicate<Path> ALL = ((p) -> {return true;});
	
	public static final Map<String, Function<Quad, Stream<Quad>>> TYPE_TO_DATA = keToM(new HashMap<>(), "ttl", (q) -> {return Stream.empty();}); 
			
	static <M extends Map<K, E>, K, E> M keToM(final M m, final K k, final E e) {
		m.put(k, e);
		return m;
	}
	
	public static String fileExtension(final String uri) {
		final int indexOf = uri.lastIndexOf(".");
		return (indexOf == -1 || indexOf == uri.length()) // No "." or it is the last character...
			? null
			: uri.substring(indexOf + 1);
	}
	
	public static final Predicate<String> RDF_LANGUAGES_FILTER = (
		(uri) -> {
			final boolean result = RDFLanguages.getRegisteredLanguages()
				.stream()
				.filter((lang) -> {
					return lang.getFileExtensions().contains(fileExtension(uri));
				})
				.findFirst()
				.isPresent();
			LOGGER.trace("path={} result={}", uri, result);
			return result;
		}
	);

	public static final Function<String, ContentType> contentTypeForExtension = (
		(uri) -> {
			final ContentType result = RDFLanguages.getRegisteredLanguages()
				.stream()
				.map((lang) -> {
					return (lang.getFileExtensions().contains(fileExtension(uri)) ? lang.getContentType() : null);
				})
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
			LOGGER.trace("uri={} result={}", uri, result);
			return result;
		}
	);
	
	protected final String root;
	
	protected final Predicate<String> filter;
	
	protected final Set<Function<String, String>> resolvers;
	
	protected final ConcurrentMap<ContentType, Function<Quad, Stream<Quad>>> cache;
	
	public RayFolder(final String path, final Predicate<String> filter) {
		super();
		this.root = path;
		this.filter = filter;
		resolvers = ConcurrentHashMap.newKeySet();
		cache = new ConcurrentHashMap<>(64);
		cache.put(RDFLanguages.TURTLE.getContentType(), (q) -> {
			LOGGER.info("q=[{}]", q);
			
			final ConcurrentMap<Node, TriTable> cache = new ConcurrentHashMap<>();

			final StreamRDFTriHexTable streamRDFTriHexTable = new StreamRDFTriHexTable();
			streamRDFTriHexTable.getTriples().begin(ReadWrite.WRITE);
			RDFDataMgr.parse(streamRDFTriHexTable, resolve(q.getGraph()));
			streamRDFTriHexTable.getTriples().commit();
			
			streamRDFTriHexTable.getTriples().begin(ReadWrite.READ);
			return streamRDFTriHexTable
				.getTriples()
				.find(q.getSubject(), q.getPredicate(), q.getObject())
				.peek(System.out::println)
				.map((t) -> {return new Quad(q.getGraph(), t);});
		});
		LOGGER.debug("{} path=[{}] filter=[{}]", RayFolder.class, path, filter);
	}

	public String getRoot() {
		return root;
	}

	public Set<Function<String, String>> getResolvers() {
		return resolvers;
	}
	
	protected Stream<Function<String, String>> streamResolvers() {
		return getResolvers().parallelStream();
	}

	/**
	 * Resolve the given URI using the resolvers, if x resolves to y then y should resolve to x...
	 */
	protected String resolve(final String uri) {
		LOGGER.debug("resolve(uri=[{}])", uri);
		String result = null;
		if (uri.equals(DatasetGraphMirage.NODE_ANY_URI)) {
			result = uri;
		} else {
			result = streamResolvers()
				.map((resolver) -> {return resolver.apply(uri);})
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
		}
		LOGGER.debug("result=[{}]", result);
		return result;
	}
	
	protected String resolve(final Node node) {
		return resolve(node.getURI());
	}

	protected String resolve(final Path path) {
		return resolve(path.toString());
	}

	protected String mapToFileSchema(final Path path) {
		return "file://" + path.toString();
	}
	
	/**
	 * Return a Node for the given URI, may return null.
	 */
	protected Node asNode(final String uri) {
		final String nodeURI = resolve(uri);
		Node result;
		if (uri == null) {
			result = null;
		} else if (uri.equals(DatasetGraphMirage.NODE_ANY_URI)) {
			result = Node.ANY;
		} else {
			result = NodeFactory.createURI(nodeURI);
		}
		return result;
	}

	/**
	 * Return a Path for the given URI, may return null.
	 */
	protected Path asPath(final String uri) {
		final String pathURI = resolve(uri);
		return (pathURI == null ? null : Paths.get(resolve(uri)));
	}
	
	@Override
	public Stream<Node> listGraphNodes() {
		try {
			return Files
				.list(Paths.get(root))
				.map((path) -> {return mapToFileSchema(path);}) // Map the Path to an URI String.
				.peek((uri) -> {LOGGER.debug("uri=[{}]", uri);})
				.filter((uri) -> {return filter.test(uri);}) 
				.map((uri) -> {return asNode(uri);}) // Convert the URI to a Node.
				.filter(Objects::nonNull); // Because asNode will return null if it cannot convert the URI to a Node.
		} catch (IOException e) {
			throw new JenaException(e);
		}
	}

	@Override
	public Stream<Quad> apply(final Quad q) {
		try {
			Stream<Quad> result = Stream.empty();
			final String uri = resolve(q.getGraph());
			if (uri.equals(DatasetGraphMirage.NODE_ANY_URI)) {
				
			} else {
				final ContentType contentType = contentTypeForExtension.apply(uri);
				if (contentType == null) {
					LOGGER.warn("apply(quad=[{}] no content type", q);
				} else {
					Function<Quad, Stream<Quad>> f = cache.get(contentType);
					if (f == null) {
						LOGGER.warn("apply(quad=[{}] no cache", contentType);
					} else {
						result = f.apply(q);
					}
				}
			}
			return result;
		} catch (final Exception exception) {
			// TODO Catch scheme and check prior to path = 
			throw new JenaException(exception);
		}
	}

	@Override
	public String toString() {
		return getRoot().toString();
	}

	/*
	 * Transactional
	 */

	@Override
	public Boolean supportsTransactions() {
		return true;
	}

	@Override
	public void begin(final ReadWrite readWrite) {
		
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		Ray.super.end();
	}
	

}
