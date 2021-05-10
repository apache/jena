<table class="table">
  <tr>
    <% _.each( headings, function( h ) { %>
      <th class="text-right"><%- h %></th>
    <% } ); %>
  </tr>
  <% _.each( rows, function( row ) { %>
    <tr>
      <% _.each( row, function( cell ) { %>
        <td class="text-right"><%- cell %></td>
      <% } ); %>
    </tr>
  <% } ) %>
</table>