<table class="table">
  <tr>
    <% _.each( headings, function( h ) { %>
      <th><%= h %></th>
    <% } ); %>
  </tr>
  <% _.each( rows, function( row ) { %>
    <tr>
      <% _.each( row, function( cell ) { %>
        <td><span class="text-right"><%= cell %></span></td>
      <% } ); %>
    </tr>
  <% } ) %>
</table>