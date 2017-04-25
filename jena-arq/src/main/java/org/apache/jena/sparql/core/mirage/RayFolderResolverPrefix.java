package org.apache.jena.sparql.core.mirage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RayFolderResolverPrefix implements RayFolderResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(RayFolderResolverPrefix.class);
	
	protected final String nodePrefix;
	
	protected final String filePrefix;
	
	public RayFolderResolverPrefix(final String nodePrefix, final String filePrefix) {
		super();
		this.nodePrefix = nodePrefix;
		this.filePrefix = filePrefix;
		LOGGER.debug("nodePrefx=[{}] filePrefix=[{}]", nodePrefix, filePrefix);
	}

	@Override
	public String apply(final String uri) {
		LOGGER.debug("apply(uri=[{}])", uri);
		String result = null;
		if (uri.startsWith(nodePrefix)) {
			result = filePrefix + uri.substring(nodePrefix.length()); 
		} else if (uri.startsWith(filePrefix)) {
			result = nodePrefix + uri.substring(filePrefix.length());
		}
		LOGGER.debug("result=[{}]", result);
		return result;
	}

}
