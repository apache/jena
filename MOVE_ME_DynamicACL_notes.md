# Proposal: Extension to jena-fuseki-access to support dynamic ACL

## What

`jena-fuseki-access` already offers the means to restrict graph visibility in a dataset on a per-user basis, configurable in Fuseki on startup (or whenever a new dataset is added).
This proposal:

1. Includes a new "dynamic" visibility restriction such that the set of visible graphs can be chosen on a per-query basis
2. Has no impact on existing ACL functionality
3. Defaults to "no graph visibility"
3. Is designed for intermediate (rather than end-user) applications (see "Why" below.)

## Why

A use-case we're exploring models metadata as many small individual graphs, each with a handful or related subjects and their properties. The aim is to allow our users to query said metadata via SPARQL whilst restricting visibility to a subset of all graphs. The set of users, graphs stored and associated ACLs are dynamic in nature and not a good fit for the current fixed configuration model.
In our use-case, we can calculate the set of "allowed" graphs at the time a user requests to run a SPARQL query and thus can supply the ACL to Fuseki on a per-query basis.

Since this proposal is generic in nature (and only a small extension to the existing `jena-fuseki-access` module), we'd like to gauge:

1. Is a useful extension that the Jena Community would consider for inclusion in upcoming releases?
2. Are better ways to support such a feature?
3. Are there any other concerns/considerations/question beyond what is documented here and in the accompanied PR?

### Alternatives

- Why not implemented this in [Jena Permissions](https://jena.apache.org/documentation/permissions/evaluator.html)? Because right now it can reason about individual graphs/models, not whole datasets. In addition this is expected to not perform as well as Fuseki ACL, which uses TDB hooks. (See also previous discussion [here](https://jena.markmail.org/thread/d44ecdeyn4dnspgx).)

- Why not implement this is a query-rewrite, applying a set of `FROM` clauses to restrict visibility? This would require parsing of the input query (or modification of the generated `Query` object) and from limited testing, a large set of FROM clauses does not perform very well. (See same discussion as in above bullet point.)

## How

### High-level flow

1. The (ACL-enabled) target dataset is assigned a user which enables the new dynamic behaviour, i.e.:
    ```turtle
    access:entry ("theUser" <urn:jena:accessGraphsDynamic>) ;
    ```
    - **Note**: If there is more than one graph listed, the user behaves like before, i.e. dynamic mode is not enabled.

2. A service/proxy (i.e. something intermediate leveraging Fuseki) receives a SPARQL request and based on it's understanding of users/roles/other calculates the set of visible graphs.

3. The SPARQL query, run as Fuseki user `theUser`, is prefixed with the calculated set of visible graphs (here `graph:one`, `graph:two` and `graph:three`):
   ```sparql
   #pragma acl.graphs graph:one|graph:two|graph:three
   SELECT * WHERE {
      ...
   }
   ```
    - **Note**: An invalid or missing `#pragma` or one without any graphs specified will result in no graphs being visible.
    - End-users are of course not meant to be directly authenticating as `theUser` in this dynamic mode.
    - Why does the proposal read the set of allowed graphs from a preamble/comment in the query rather than either an HTTP header or a URL parameter? Because both of these have fairly low maximum size expectations (by REST servers) such that it's not feasible to store 100s of graph URIs in them.

4. When the query is executed, access is restricted to the previously provided set of graphs
    - **Note**: The dynamic behaviour only applies to SPARQL queries, not [GSP](https://www.w3.org/TR/sparql11-http-rdf-update). If a GSP request is made, no graphs are visible.

### Technical summary

1. A new `SecurityContext` implementation, `SecurityContextDynamic`, acts as the marker to indicate that the visible graphs should be chosen from the query preamble.
    - `AssemblerSecurityRegistry` detects this new mode and choose the `SecurityContext` accordingly.

2. When a SPARQL query hits `AccessCtl_SPARQL_QueryDataset`, the raw query string (and thus `#pragma`) are stored in the `HttpAction`'s context.

3. If (based on user) the determined security context is dynamic, the query is parsed to construct a new `SecurityContextView` with the set of chosen graphs.

4. Thereafter execution and ACL guarantees are the same as for pre-existing ACL functionality
