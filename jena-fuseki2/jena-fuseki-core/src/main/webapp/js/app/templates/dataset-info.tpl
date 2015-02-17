<h2>Available services</h2>

<dl class="dl-horizontal">
  <% _.each( servicesDescription(), function( serviceDescription ) { %>
    <dt>
      <%= serviceDescription.label %>:
    </dt>
    <dd>
      <a href="<%= serviceDescription.url %>"><%= serviceDescription.url %></a>
    </dd>
  <% } ); %>
</dl>

<h2>Statistics</h2>
<div id="statistics"></div>

<h2>Dataset size</h2>
<p>
<strong>Note</strong> this may be slow and impose a significant load on large datasets:
<button href="#" class="action count-graphs btn btn-primary">count triples in all graphs</button>
</p>
<% if (countPerformed()) { %>
<dl class="dl-horizontal">
  <dt><span class="heading">graph name:</span></dt><dd><span class="heading">triples:</span></dd>
  <% _.each( counts(), function( n, g ) { %>
    <dt class="font-weight-normal">
      <%= g %>
    </dt>
    <dd>
      <div class="numeric"><%= n %></div>
    </dd>
  <% } ); %>
</dl>

<% } %>

<h2>Ongoing operations</h2>

<p><em>TBD. Will list any long-lasting operations that are ongoing or recently completed,
e.g. backups.</em></p>
