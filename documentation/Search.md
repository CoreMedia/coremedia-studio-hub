## Search Types

The list of search types, available in the Studio library, depends on the selected connector.
The list of type is defined by the _Connector Item Types_ setting document, so a custom list of types
can be configured for each connector.

For connectors like RSS and YouTube, this settings document is not required since the connector only provides 
one item type. Therefore, the search type combo-box for these connectors only have one entry which is 'All'.

## Aggregated Search

When a connector search is triggered in the Studio library and the current folder selection is a connector, and not 
a connection, search search will be triggered for all connections of the connector. The result will be aggregated 
and shown in the search list. One a concrete connection will be selected, e.g. through the breadcrumb menu, the search is 
limited to the selected connection only.