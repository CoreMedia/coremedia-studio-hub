# Content Push

The Content Hub not only allows

The _Content Upload Types_ document describes which CoreMedia document types are allowed for uploading
and what are the blob property names of them. By default, the content upload is enabled when a category is writeable
or _isContentUploadEnabled_ returns true. In that case this settings document is used to apply the DnD logic for the Studio.
By default, the _upload_ method of the connector category is used to upload content blobs to the specific target system.


//TODO 
//add doc for ConnectorContentUploadInterceptor
//support dynamic black and whitelists according to connection configuration
//add doc about image variants and content properties
//add doc about "original" property
//add doc about push config vs. push dialog
//implement disable fro blacklists
