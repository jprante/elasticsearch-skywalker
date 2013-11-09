.. image:: ../../../elasticsearch-skywalker/raw/master/src/site/resources/cologne-skywalker.jpg

Image `Skywalking the Cologne Cathedral by Vadim Makhorov <http://dedmaxopka.livejournal.com/71750.html>`_

Elasticsearch Skywalker plugin
==============================

The Skywalker plugin for `Elasticsearch <http://github.com/elasticsearch/elasticsearch>`_ is like Luke for Lucene.

Well, almost, it's not coming with a GUI right now.

Skywalking gives you

- a deeper insight of how Elasticsearch stores Lucene indices and fields

- lists timestamps and checksums of Elasticsearch's Lucene files

- shows field names, both from mapping and Lucene indices

- shows field types and attributes, both from mapping and Lucene indices

- retrieves a ranked list of most frequent terms

Because Elasticsearch and Elasticsearch Head offer already a lot what Luke does with Lucene, Skywalker has rather few features, just to complement some missing parts.

It is recommended to use Skywalker for learning on rather small indices.

With large indices, or on production machines, unpredictable exceptions may happen, mostly out of memory execptions. You have been warned. Use at your own risk!

Installation
------------

Current version of the plugin: **3.1.0** (Nov  9, 2013)

.. image:: https://travis-ci.org/jprante/elasticsearch-skywalker.png

Prerequisites::

  Elasticsearch 0.90.x

=============  =========  =================  ============================================================
ES version     Plugin     Release date       Command
-------------  ---------  -----------------  ------------------------------------------------------------
0.90.0         3.0.0      May 24, 2013       ./bin/plugin --install skywalker --url http://bit.ly/1eYTIHj
0.90.5         **3.1.0**  Nov  9, 2013       ./bin/plugin --install skywalker --url http://bit.ly/HFJos6
=============  =========  =================  ============================================================

Do not forget to restart the node after installing.

Project docs
------------

The Maven project site is available at `Github <http://jprante.github.io/elasticsearch-skywalker>`_

Binaries
--------

Binaries are available at `Bintray <https://bintray.com/pkg/show/general/jprante/elasticsearch-plugins/elasticsearch-skywalker>`_


Documentation
-------------

Example request::

    curl -XDELETE 'localhot:9200/test'

    curl -XPUT 'localhost:9200/test/docs/1' -d '
    {
        "content" : "Hello World"
    }
    '

    curl -XGET 'localhost:9200/_skywalker'

    curl -XGET 'localhost:9200/test/_skywalker'


Example output::

	{
	  "ok" : true,
	  "_shards" : {
	    "total" : 5,
	    "successful" : 5,
	    "failed" : 0
	  },
	  "result" : {
	    "testindex" : {
	      "3" : {
	        "store" : [ {
	          "name" : "_v.fdx",
	          "length" : 1332,
	          "lastmodified" : "2012-05-24T14:47:09.000Z",
	          "func" : "stored fields index data",
	          "checksum" : "ee2hoy"
	        }, {
	          "name" : "_z.frq",
	          "length" : 32465,
	          "lastmodified" : "2012-05-24T14:47:15.000Z",
	          "func" : "term frequency postings data",
	          "checksum" : "db6lgl"
	        }, {
	          "name" : "segments.gen",
	          "length" : 20,
	          "lastmodified" : "2012-05-24T15:16:03.000Z",
	          "func" : "generation number - global file",
	          "checksum" : null
	        }, {
	          "name" : "_z.fnm",
	          "length" : 2368,
	          "lastmodified" : "2012-05-24T14:47:15.000Z",
	          "func" : "field names / infos",
	          "checksum" : "13tfrvf"
	        }, {
	          "name" : "_y.fdt",
	          "length" : 593872,
	          "lastmodified" : "2012-05-24T14:47:14.000Z",
	          "func" : "stored fields data",
	          "checksum" : "rmf3zi"
	        }, {
	[...]
	        }, {
	          "name" : "segments_2",
	          "length" : 1832,
	          "lastmodified" : "2012-05-24T15:16:03.000Z",
	          "func" : "per-commit list of segments",
	          "checksum" : null
	        }, {
	          "name" : "_10.nrm",
	          "length" : 1524,
	          "lastmodified" : "2012-05-24T14:47:16.000Z",
	          "func" : "norms data for all fields",
	          "checksum" : "yv7s2l"
	        } ],
	        "numTerms" : 153043,
	        "topterms" : [ {
	          "field" : "_type",
	          "text" : "__dc:subject.xbib:subject",
	          "docFreq" : 6191
	        }, {
	          "field" : "dc:subject.xbib:subject.xbib:subjectAuthority",
	          "text" : "RSWK",
	          "docFreq" : 4342
	        }, {
	          "field" : "dc:subject.xbib:subject.xbib:subjectIDAuthority",
	          "text" : "gnd",
	          "docFreq" : 3217
	        }, {
	          "field" : "dc:subject.xbib:subject.xbib:subjectType",
	          "text" : "topic",
	          "docFreq" : 2451
	[...]
	        } ],
	        "maxlastmodified" : 1337872563000,
	        "hasDeletions" : false,
	        "directoryImpl" : "org.elasticsearch.index.store.Store$StoreDirectory",
	        "indexFormat" : {
	          "id" : -11,
	          "capabilities" : "lock-less, single norms, shared doc store, checksum, del count, omitTf, user data, diagnostics, hasVectors",
	          "genericName" : "Lucene 3.1"
	        },
	        "minlastmodified" : 1337870802000,
	        "numDocs" : 8229,
	        "indexversion" : "1337870762887",
	        "maxDoc" : 8229,
	        "commits" : [ {
	          "files" : [ "_p.fdx", "_z.frq", "_p.fdt", "_12.fdt", "_12.tii", "_z.fnm", "_12.fdx", 	"_y.fdt", "_10.tii", "_p.nrm", "_w.tii", "_y.fdx", "_y.nrm", "_12.tis", "_w.fnm", "_10.tis", "_x.tis", "_l.nrm", "_w.tis", "_w.fdt", "_w.frq", "_l.prx", "_11.fdx", "_w.fdx", "_11.fdt", "_x.tii", "_z.nrm", "_10.prx", "_l.fdx", "_12.fnm", "_11.prx", "_l.fdt", "_12.frq", "_x.fdt", "_z.fdt", "_x.nrm", "_11.tii", "_10.fdt", "_l.fnm", "_z.tii", "_p.fnm", "_y.tis", "_x.fdx", "_z.fdx", "_y.frq", "_11.tis", "_z.tis", "_l.frq", "_w.prx", "_p.frq", "_y.tii", "_10.fdx", "_l.tis", "_11.nrm", "_p.tii", "_w.nrm", "_l.tii", "_y.fnm", "_10.fnm", "_x.fnm", "_p.tis", "_z.prx", "_12.prx", "_10.frq", "_x.frq", "_11.frq", "_y.prx", "_12.nrm", "_x.prx", "_11.fnm", "segments_2", "_10.nrm", "_p.prx" ],
	          "userdata" : {
	            "translog_id" : "1337870762809"
	          },
	          "count" : 9,
	          "segment" : "segments_2",
	          "deleted" : false
	        } ],
	        "numDeletedDocs" : 0
	      },
	      "fieldInfos" : [ {
	        "name" : "_uid",
	        "mapper" : {
	          "indexNameClean" : "_uid",
	          "indexed" : true,
	          "omitTermFreqAndPositions" : false,
	          "analyzed" : false,
	          "indexName" : "_uid",
	          "boost" : 1.0,
	          "fullName" : "_uid",
	          "fieldDataType" : "STRING",
	          "omitNorms" : true
	        },
	        "number" : 0,
	        "storePayloads" : true,
	        "omitNorms" : false,
	        "options" : "DOCS_AND_FREQS_AND_POSITIONS",
	        "storeTermVector" : false,
	        "isindexed" : true
	      }, {
	        "name" : "_type",
	        "mapper" : {
	          "indexNameClean" : "_type",
	          "indexed" : true,
	          "omitTermFreqAndPositions" : true,
	          "analyzed" : false,
	          "indexName" : "_type",
	          "boost" : 1.0,
	          "fullName" : "_type",
	          "fieldDataType" : "STRING",
	          "omitNorms" : true
	        },
	        "number" : 1,
	        "storePayloads" : false,
	        "omitNorms" : false,
	        "options" : "DOCS_ONLY",
	        "storeTermVector" : false,
	        "isindexed" : true
	      }, {
	[...]

License
=======

This plugin re-uses code of the Luke project <http://code.google.com/p/luke/>

Elasticsearch Skywalker Plugin

Copyright (C) 2012,2013 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.