<h2>Known datasets:</h2>
<ul class=''>
  <% _.each( datasets, function( ds ) { %>
    <li>
      <%= ds.name() %>
    </li>
  <% }) %>
</ul>
