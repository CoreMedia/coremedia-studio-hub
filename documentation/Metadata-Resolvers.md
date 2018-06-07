The Studio Hub comes with some pre-defined metadata resolvers that are located in the _connectors-lib_ module.
Metadata resolvers are used to enrich the metadata panel that is shown next to the connector item preview.

For file based connectors, the following metadata resolver are available:
 * _AudioMetaDataResolver_: tries to read the MP3 metadata out of audio files
 * _PdfMetaDataResolver_:  reads the PDF metadata out of a document, like the author's name, page count, etc. 
 * _PictureMetaDataResolver_: read the IPTC metadata out of a picture