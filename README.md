Skywalker plugin for ElasticSearch
===================================

The Skywalker plugin for Elasticsearch is like Luke for Lucene.

Well, almost, it's not coming with a GUI right now.

Skywalking gives you

- an deeper insight of how Elasticsearch stores Lucene indices and fields

- lists timestamps and checksums of Elasticsearch Lucene files

- shows field names, both from mapping and from Lucene indices

- shows field types and attributes, both from mapping and from Lucene indices

- retrieves a ranked list of most frequent terms

Because Elasticsearch and ELasticsearch Head does already a lot what Luke offers on the basis of Lucene, Skywalker has rather few features, just to complement some missing parts.

Planned features

- a JSON-based backup/restore tool, saving all Elasticsearch internals to a readable file ("portable index")

- reconstruct a just deleted document, by document ID

Usage example
-------------

Usage is simple

	curl -XGET 'localhost:9200/_skywalker'

Or for a single index 

	curl -XGET 'localhost:9200/indexname/_skywalker'


Installation
------------

In order to install the plugin, simply run: `bin/plugin -install jprante/elasticsearch-skywalker/1.0.0`.

    -------------------------------------
    | OAI Plugin     | ElasticSearch    |
    -------------------------------------
    | master         | 0.19.3 -> master |
    -------------------------------------
    | 1.0.0          | 0.19.3           |
    -------------------------------------


Documentation
-------------

The Maven project site is [here](http://jprante.github.com/elasticsearch-skywalker)

The Javadoc API can be found [here](http://jprante.github.com/elasticsearch-skywalker/apidocs/index.html)

