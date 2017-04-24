package org.apache.jena.sparql.core.mirage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RayFolder implements Ray {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RayFolder.class);

	public static final Predicate<Path> ALL = ((p) -> {return true;});
	
	public static String fileExtension(final String uri) {
		final int indexOf = uri.lastIndexOf(".");
		return (indexOf == -1 || indexOf == uri.length()) // No "." or it is the last character...
			? null
			: uri.substring(indexOf + 1);
	}
	
	public static final Predicate<String> RDF_LANGUAGES = (
		(uri) -> {
			final boolean result = RDFLanguages.getRegisteredLanguages()
				.stream()
				.filter((lang) -> {
					final String fileExtension = fileExtension(uri);
					return (fileExtension == null ? false : lang.getFileExtensions().contains(fileExtension));
				})
				.findFirst()
				.isPresent();
			LOGGER.trace("path={} result={}", uri, result);
			return result;
		}
	);
	
	/**
	 * The set of unsupported as path nodes.
	 */
	protected final Set<String> unsupportedAsPath = ConcurrentHashMap.newKeySet();
	
	protected final String path;
	
	protected final Predicate<String> filter;
	
	protected final Set<Function<String, String>> resolvers;
	
	protected final Function<String, Stream<Quad>> data;
	
	public RayFolder(final String path, final Predicate<String> filter) {
		super();
		this.path = path;
		this.filter = filter;
		resolvers = ConcurrentHashMap.newKeySet();
		data = (p) -> {
			return Stream.empty();
		};
		LOGGER.debug("{} path=[{}] filter=[{}]", RayFolder.class, path, filter);
	}

	public String getPath() {
		return path;
	}

	public Set<Function<String, String>> getResolvers() {
		return resolvers;
	}
	
	protected Stream<Function<String, String>> streamResolvers() {
		return getResolvers().parallelStream();
	}

	/**
	 * Resolve the given uri, if x resolves to y then y should resolve to x...
	 */
	protected String resolve(final String uri) {
		LOGGER.debug("resolve(uri=[{}])", uri);
		String result = streamResolvers()
			.map((resolver) -> {return resolver.apply(uri);})
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		LOGGER.debug("result=[{}]", result);
		return result;
	}
	
	protected Node asNode(final String uri) {
		final String nodeURI = resolve(uri);
		return (nodeURI == null ? null : NodeFactory.createURI(nodeURI));
	}
	
	protected Path asPath(final String uri) {
		return Paths.get(resolve(uri));
	}

	protected Stream<Quad> pathStream(final Node g) {
		if (g == Node.ANY) {
			return Stream.empty();
		} else {
			return data.apply(resolve(g.getURI()));
		}
	}
	
	@Override
	public Stream<Node> listGraphNodes() {
		try {
			return Files
				.list(Paths.get(path))
				.map((path) -> {return "file://" + path.toString();}) // Map the Path to String.
				.peek((uri) -> {LOGGER.debug("uri=[{}]", uri);})
				.filter((uri) -> {return filter.test(uri);}) 
				.map((uri) -> {return asNode(uri);})
				.filter(Objects::nonNull);
		} catch (IOException e) {
			throw new JenaException(e);
		}
	}

	@Override
	public Stream<Quad> apply(final Quad q) {
		try {
			return pathStream(q.getGraph());
		} catch (final Exception exception) {
			// TODO Catch scheme and check prior to path = 
			throw new JenaException(exception);
		}
	}

	@Override
	public String toString() {
		return getPath().toString();
	}
}
