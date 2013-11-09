
package org.xbib.elasticsearch.skywalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.BytesRef;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatProvider;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.similarity.SimilarityProvider;
import org.elasticsearch.index.store.StoreFileMetaData;

import org.xbib.elasticsearch.skywalker.stats.FieldTermCount;
import org.xbib.elasticsearch.skywalker.stats.TermStats;
import org.xbib.elasticsearch.skywalker.stats.TermStatsQueue;

/**
 *
 * Skywalker class for examining Lucene format
 *
 */
public class Skywalker implements LuceneFormats {

    private final static Map<String, String> knownExtensions = new HashMap();

    private IndexReader reader;
    private int numTerms;
    private FormatDetails formatDetails;
    private TermStats[] topTerms;

    private String version;
    private String dirImpl;

    /**
     * http://lucene.apache.org/core/4_2_0/core/org/apache/lucene/codecs/lucene42/package-summary.html
     */

    static {
        knownExtensions.put("cfs", "Lucene compound file with various index data");
        knownExtensions.put("cfe", "Lucene compound file entries list");
        knownExtensions.put("gen", "Lucene generation number - global file");
        knownExtensions.put("si", "Lucene per-commit list of segments and user data");
        knownExtensions.put("doc", "Lucene frequencies");
        knownExtensions.put("pos", "Lucene positions");
        knownExtensions.put("pay", "Lucene payloads");
        knownExtensions.put("fdt", "Lucene field data");
        knownExtensions.put("fdx", "Lucene field data index");
        knownExtensions.put("fnm", "Lucene fields");
        knownExtensions.put("del", "Lucene deleted documents");
        knownExtensions.put("dvm", "Lucene per-document values");
        knownExtensions.put("dvd", "Lucene per-dicument values");
        knownExtensions.put("nvm", "Lucene norms");
        knownExtensions.put("nvd", "Lucene norms");
        knownExtensions.put("tim", "Lucene term dictionary");
        knownExtensions.put("tip", "Lucene term dictionary index positions");
        knownExtensions.put("tvx", "Lucene term vector index");
        knownExtensions.put("tvd", "Lucene term vector documents");
        knownExtensions.put("tvf", "Lucene term vector fields");
        // Elasticsearch
        knownExtensions.put("blm", "Elasticsearch bloom filter");
    }

    public Skywalker(IndexReader reader) {
        this.reader = reader;
        this.dirImpl = "N/A";
        this.version = "-1";
        this.formatDetails = new FormatDetails("N/A", "N/A", "N/A");
        Directory dir = null;
        if (reader instanceof DirectoryReader) {
            dir = ((DirectoryReader) reader).directory();
            this.dirImpl = dir.getClass().getName();
            this.version = Long.toString(((DirectoryReader) reader).getVersion());
            this.formatDetails = getIndexFormat(dir);
        }
    }

    public String getVersion() {
        return version;
    }

    public FormatDetails getFormatDetails() {
        return formatDetails;
    }

    public String getDirImpl() {
        return dirImpl;
    }

    public String getFileFunction(String file) {
        if (file == null || file.trim().length() == 0) {
            return file;
        }
        String res = "undefined";
        file = file.trim();
        int idx = file.indexOf('.');
        String suffix = null;
        if (idx != -1) {
            suffix = file.substring(idx + 1);
        }
        if (suffix != null) {
            res = knownExtensions.get(suffix);
            if (res != null) {
                return res;
            }
            // perhaps per-field norms?
            if (suffix.length() == 2) {
                return knownExtensions.get(suffix.substring(0, 1));
            }
        }
        if (file.startsWith("segments_")) {
            return "Lucene segment";
        }
        // elasticsearch checksums
        if (file.startsWith("_checksum")) {
            return "Elasticsearch checksum file";
        }
        return res;
    }

    private FormatDetails detectOldFormats(int format) {
        switch (format) {
            case OLD_FORMAT:
                return new FormatDetails("old plain", "Lucene Pre-2.1", "2.0?");
            case FORMAT_LOCKLESS:
                return new FormatDetails("lock-less", "Lucene 2.1", "2.1");
            case FORMAT_SINGLE_NORM_FILE:
                return new FormatDetails("lock-less, single norms file", "Lucene 2.2", "2.2");
            case FORMAT_SHARED_DOC_STORE:
                return new FormatDetails("lock-less, single norms file, shared doc store", "Lucene 2.3", "2.3");
            case FORMAT_CHECKSUM:
                return new FormatDetails("lock-less, single norms, shared doc store, checksum", "Lucene 2.4", "2.4");
            case FORMAT_DEL_COUNT:
                return new FormatDetails("lock-less, single norms, shared doc store, checksum, del count", "Lucene 2.4", "2.4");
            case FORMAT_HAS_PROX:
                return new FormatDetails("lock-less, single norms, shared doc store, checksum, del count, omitTf", "Lucene 2.4", "2.4");
            case FORMAT_USER_DATA:
                return new FormatDetails("lock-less, single norms, shared doc store, checksum, del count, omitTf, user data", "Lucene 2.9-dev", "2.9-dev");
            case FORMAT_DIAGNOSTICS:
                return new FormatDetails("lock-less, single norms, shared doc store, checksum, del count, omitTf, user data, diagnostics", "Lucene 2.9", "2.9");
            case FORMAT_HAS_VECTORS:
                return new FormatDetails("lock-less, single norms, shared doc store, checksum, del count, omitTf, user data, diagnostics, hasVectors", "Lucene 2.9", "2.9");
            case FORMAT_3_1:
                return new FormatDetails("lock-less, single norms, shared doc store, checksum, del count, omitTf, user data, diagnostics, hasVectors", "Lucene 3.1", "3.1");
            case FORMAT_PRE_4:
                return new FormatDetails("flexible, unreleased 4.0 pre-alpha", "Lucene 4.0-dev", "4.0-dev");
            default:
                if (format < FORMAT_PRE_4) {
                    return new FormatDetails("flexible, unreleased 4.0 pre-alpha", "Lucene 4.0-dev", "4.0-dev");
                } else {
                    return new FormatDetails("unknown", "Lucene 1.3 or earlier, or unreleased", "1.3?");
                }
        }
    }

    public FormatDetails getIndexFormat(final Directory dir) {
        SegmentInfos.FindSegmentsFile fsf = new SegmentInfos.FindSegmentsFile(dir) {

            protected Object doBody(String segmentsFile) throws IOException {
                FormatDetails res = new FormatDetails("unknown", "unknown", "-1");
                IndexInput in = dir.openInput(segmentsFile, IOContext.READ);
                try {
                    int indexFormat = in.readInt();
                    if (indexFormat == CodecUtil.CODEC_MAGIC) {
                        res.setCapabilities("flexible, codec-specific");
                        res.setGenericName("Lucene 4.x");
                        int actualVersion = SegmentInfos.VERSION_40;
                        try {
                            actualVersion = CodecUtil.checkHeaderNoMagic(in, "segments", SegmentInfos.VERSION_40, Integer.MAX_VALUE);
                            if (actualVersion > SegmentInfos.VERSION_40) {
                                res.setCapabilities(res.getCapabilities() + " (WARNING: newer version of Lucene than this tool)");
                            }
                        } catch (Exception e) {
                            res.setCapabilities(res.getCapabilities() +
                                    " (error reading: " + e.getMessage() + ")");
                        }
                        res.setGenericName("Lucene 4." + actualVersion);
                        res.setVersion("4." + actualVersion);
                    } else {
                        res = detectOldFormats(indexFormat);
                        res.setGenericName(res.getGenericName() + " (" + indexFormat + ")");
                        if (res.getVersion().compareTo("3") < 0) {
                            res.setCapabilities(res.getCapabilities() + " (UNSUPPORTED)");
                        }
                    }
                } finally {
                    in.close();
                }
                return res;
            }
        };
        try {
            return (FormatDetails) fsf.run();
        } catch (IOException e) {
            return null;
        }
    }

    public List<String> getDeletableFiles(Directory dir) throws Exception {
        List<String> known = getIndexFiles(dir);
        Set<String> dirFiles = new HashSet<String>(Arrays.asList(dir.listAll()));
        dirFiles.removeAll(known);
        return new ArrayList<String>(dirFiles);
    }

    public List<String> getIndexFiles(Directory dir) {
        List<IndexCommit> commits;
        try {
            commits = DirectoryReader.listCommits(dir);
        } catch (IndexNotFoundException e) {
            return Collections.emptyList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
        Set<String> known = new HashSet<String>();
        try {
            for (IndexCommit ic : commits) {
                known.addAll(ic.getFileNames());
            }
            if (dir.fileExists(IndexFileNames.SEGMENTS_GEN)) {
                known.add(IndexFileNames.SEGMENTS_GEN);
            }
        } catch (IOException e) {
            // ignore
        }
        return new ArrayList<String>(known);
    }

    public long getTotalFileSize(Directory directory) throws Exception {
        long totalFileSize = 0L;
        String[] files;
        files = directory.listAll();
        if (files == null) {
            return totalFileSize;
        }
        for (String file : files) {
            totalFileSize += directory.fileLength(file);
        }
        return totalFileSize;
    }

    public Map<String, Object> getFieldInfo(MapperService mapperService, FieldInfo fi) {
        Map<String, Object> m = new HashMap();
        m.put("name", fi.name);
        m.put("number", fi.number);
        m.put("isIndexed", fi.isIndexed());
        m.put("hasDocValues", fi.hasDocValues());
        m.put("hasNorms", fi.hasNorms());
        m.put("hasPayloads", fi.hasPayloads());
        m.put("hasVectors", fi.hasVectors());
        if (fi.getDocValuesType() != null) {
            m.put("docValuesType", fi.getDocValuesType().name());
        }
        if (fi.getNormType() != null) {
            m.put("normType", fi.getNormType().name());
        }
        if (fi.getIndexOptions() != null) {
            m.put("options", fi.getIndexOptions().name());
        }
        m.put("attributes", fi.attributes());
        FieldMapper fieldMapper = mapperService.smartNameFieldMapper(fi.name);
        if (fieldMapper != null) {
            Map<String, Object> mapper = new HashMap();
            mapper.put("fullName", fieldMapper.names().fullName());
            mapper.put("indexName", fieldMapper.names().indexName());
            mapper.put("indexNameClean", fieldMapper.names().indexNameClean());

            mapper.put("boost", fieldMapper.boost());

            if (fieldMapper.indexAnalyzer() != null) {
                mapper.put("indexAnalyzer", fieldMapper.indexAnalyzer().toString());
            }
            if (fieldMapper.searchAnalyzer() != null) {
                mapper.put("searchAnalyzer", fieldMapper.searchAnalyzer().toString());
            }
            if (fieldMapper.searchQuoteAnalyzer() != null) {
                mapper.put("searchQuoteAnalyzer", fieldMapper.searchQuoteAnalyzer().toString());
            }

            FieldDataType dataType = fieldMapper.fieldDataType();
            if (dataType != null) {
                mapper.put("fieldDataType", dataType.getType());
            }

            FieldType type = fieldMapper.fieldType();
            if (type != null) {
                mapper.put("indexed", type.indexed());
                mapper.put("stored", type.stored());
                mapper.put("tokenized", type.tokenized());
                mapper.put("omitNorms", type.omitNorms());
                mapper.put("storeTermVectors", type.storeTermVectors());
                mapper.put("storeTermVectorOffsets", type.storeTermVectorOffsets());
                mapper.put("storeTermVectorPayloads", type.storeTermVectorPayloads());
                mapper.put("storeTermVectorPositions", type.storeTermVectorPositions());
                if (type.numericType() != null) {
                    mapper.put("numericType", type.numericType().name());
                    mapper.put("numericPrecisionStep", type.numericPrecisionStep());
                }
                if (type.docValueType() != null) {
                    mapper.put("docValueType", type.docValueType().name());
                }
            }

            SimilarityProvider similarityProvider = fieldMapper.similarity();
            if (similarityProvider != null) {
                mapper.put("similarityPovider", similarityProvider.name());
                mapper.put("similarity", similarityProvider.get().getClass().getName() );
            }

            PostingsFormatProvider postingsFormatProvider = fieldMapper.postingsFormatProvider();
            if (postingsFormatProvider != null) {
                mapper.put("postingsFormatProvider", postingsFormatProvider.name());
                mapper.put("postingsFormat", postingsFormatProvider.get().getName());
            }

            m.put("mapper", mapper);
        }
        return m;
    }

    public void getStoreMetadata(Map<String, Object> response, ImmutableMap<String, StoreFileMetaData> metadata) {
        List<Map<String, Object>> result = new ArrayList();
        for (String name : metadata.keySet()) {
            StoreFileMetaData metaData = metadata.get(name);
            Map<String, Object> info = new HashMap();
            info.put("name", name);
            info.put("length", metaData.length());
            info.put("checksum", metaData.checksum() );
            info.put("function", getFileFunction(name));
            result.add(info);
        }
        response.put("store", result);
    }

    /**
     *  copied from org.elasticsearch.gateway.local.state.meta.LocalGatewayMetaState
     *
     * @return the meta data from file
     * @throws Exception
     */

    public static MetaData.Builder loadState(List<File> files, NodeEnvironment nodeEnv) throws ElasticSearchException {
        MetaData.Builder metaDataBuilder;
        try {
            MetaData globalMetaData = loadGlobalState(files, nodeEnv);
            if (globalMetaData != null) {
                metaDataBuilder = MetaData.builder(globalMetaData);
            } else {
                metaDataBuilder = MetaData.builder();
            }
            Set<String> indices = nodeEnv.findAllIndices();
            for (String index : indices) {
                IndexMetaData indexMetaData = loadIndex(files, index, nodeEnv);
                if (indexMetaData == null) {
                    continue;
                } else {
                    metaDataBuilder.put(indexMetaData, false);
                }
            }
        } catch (Exception e) {
            throw new ElasticSearchException(e.getMessage());
        }
        return metaDataBuilder;
    }

    @Nullable
    private static IndexMetaData loadIndex(List<File> files, String index, NodeEnvironment nodeEnv) {
        long highestVersion = -1;
        IndexMetaData indexMetaData = null;
        for (File indexLocation : nodeEnv.indexLocations(new Index(index))) {
            File stateDir = new File(indexLocation, "_state");
            if (!stateDir.exists() || !stateDir.isDirectory()) {
                continue;
            }
            // now, iterate over the current versions, and find latest one
            File[] stateFiles = stateDir.listFiles();
            if (stateFiles == null) {
                continue;
            }
            for (File stateFile : stateFiles) {
                if (!stateFile.getName().startsWith("state-")) {
                    continue;
                }
                files.add(stateFile);
                try {
                    long version = Long.parseLong(stateFile.getName().substring("state-".length()));
                    if (version > highestVersion) {
                        byte[] data = Streams.copyToByteArray(new FileInputStream(stateFile));
                        if (data.length == 0) {
                            continue;
                        }
                        XContentParser parser = null;
                        try {
                            parser = XContentHelper.createParser(data, 0, data.length);
                            parser.nextToken(); // move to START_OBJECT
                            indexMetaData = IndexMetaData.Builder.fromXContent(parser);
                            highestVersion = version;
                        } finally {
                            if (parser != null) {
                                parser.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return indexMetaData;
    }

    private static MetaData loadGlobalState(List<File> files, NodeEnvironment nodeEnv) {
        long highestVersion = -1;
        MetaData metaData = null;
        for (File dataLocation : nodeEnv.nodeDataLocations()) {
            File stateLocation = new File(dataLocation, "_state");
            if (!stateLocation.exists()) {
                continue;
            }
            File[] stateFiles = stateLocation.listFiles();
            if (stateFiles == null) {
                continue;
            }
            for (File stateFile : stateFiles) {
                if (!stateFile.getName().startsWith("global-")) {
                    continue;
                }
                files.add(stateFile);
                try {
                    long version = Long.parseLong(stateFile.getName().substring("global-".length()));
                    if (version > highestVersion) {
                        byte[] data = Streams.copyToByteArray(new FileInputStream(stateFile));
                        if (data.length == 0) {
                            continue;
                        }
                        XContentParser parser = null;
                        try {
                            parser = XContentHelper.createParser(data, 0, data.length);
                            metaData = MetaData.Builder.fromXContent(parser);
                            highestVersion = version;
                        } finally {
                            if (parser != null) {
                                parser.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return metaData;
    }

    public Set<FieldTermCount> getFieldTermCounts() throws IOException {
        Set<FieldTermCount> termCounts = new TreeSet<FieldTermCount>();
        numTerms = 0;
        Fields fields = MultiFields.getFields(reader);
        if (fields != null) {
            Iterator<String> fe = fields.iterator();
            String fld;
            TermsEnum te = null;
            while (fe.hasNext()) {
                fld = fe.next();
                long termCount = 0L;
                Terms terms = fields.terms(fld);
                if (terms != null) {
                    te = terms.iterator(te);
                    while (te.next() != null) {
                        termCount++;
                        numTerms++;
                    }
                }
                termCounts.add(new FieldTermCount(fld, termCount));
            }
        }
        return termCounts;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public TermStats[] getTopTerms(int num) {
        if (topTerms == null) {
            topTerms = getHighFreqTerms(num, null);
        }
        return topTerms;
    }

    private static final TermStats[] EMPTY_STATS = new TermStats[0];

    public TermStats[] getHighFreqTerms(int numTerms, String[] fieldNames) {
        TermStatsQueue tiq = new TermStatsQueue(numTerms);
        TermsEnum te = null;
        try {
            if (fieldNames != null) {
                Fields fields = MultiFields.getFields(reader);
                if (fields == null) {
                    return EMPTY_STATS;
                }
                for (String field : fieldNames) {
                    Terms terms = fields.terms(field);
                    if (terms != null) {
                        te = terms.iterator(te);
                        fillQueue(te, tiq, field);
                    }
                }
            } else {
                Fields fields = MultiFields.getFields(reader);
                if (fields == null) {
                    return EMPTY_STATS;
                }
                for (String field : fields) {
                    Terms terms = fields.terms(field);
                    te = terms.iterator(te);
                    fillQueue(te, tiq, field);
                }
            }
        } catch (IOException e) {
            // ignore
        }
        TermStats[] result = new TermStats[tiq.size()];
        // we want highest first so we read the queue and populate the array
        // starting at the end and work backwards
        int count = tiq.size() - 1;
        while (tiq.size() != 0) {
            result[count] = tiq.pop();
            count--;
        }
        return result;
    }

    public static String bytesToHex(BytesRef bytes, boolean wrap) {
        return bytesToHex(bytes.bytes, bytes.offset, bytes.length, wrap);
    }

    public static String bytesToHex(byte bytes[], int offset, int length, boolean wrap) {
        StringBuilder sb = new StringBuilder();
        boolean newLine = false;
        for (int i = offset; i < offset + length; ++i) {
            if (i > offset && !newLine) {
                sb.append(" ");
            }
            sb.append(Integer.toHexString(0x0100 + (bytes[i] & 0x00FF))
                    .substring(1));
            if (i > 0 && (i + 1) % 16 == 0 && wrap) {
                sb.append("\n");
                newLine = true;
            } else {
                newLine = false;
            }
        }
        return sb.toString();
    }

    private void fillQueue(TermsEnum termsEnum, TermStatsQueue tiq, String field) {
        while (true) {
            try {
                BytesRef term = termsEnum.next();
                if (term != null) {
                    BytesRef text = new BytesRef();
                    text.copyBytes(term);
                    TermStats ts = new TermStats();
                    ts.field(field).text(text).docFreq(termsEnum.docFreq());
                    tiq.insertWithOverflow(ts);
                } else {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
    }

}
