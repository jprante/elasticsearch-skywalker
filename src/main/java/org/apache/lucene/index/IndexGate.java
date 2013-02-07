package org.apache.lucene.index;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IndexGate {

    private static Field deletable = null;
    private static Field hasChanges = null;
    private static PrintStream infoStream = IndexWriter.getDefaultInfoStream();
    private static final HashMap<String, String> knownExtensions = new HashMap<String, String>() {

        {
            put(IndexFileNames.COMPOUND_FILE_EXTENSION, "compound file with various index data");
            put(IndexFileNames.COMPOUND_FILE_STORE_EXTENSION, "compound shared doc store file");
            put(IndexFileNames.DELETABLE, "list of deletable files (pre-lockless index)");
            put(IndexFileNames.DELETES_EXTENSION, "list of deleted documents");
            put(IndexFileNames.FIELD_INFOS_EXTENSION, "field names / infos");
            put(IndexFileNames.FIELDS_EXTENSION, "stored fields data");
            put(IndexFileNames.FIELDS_INDEX_EXTENSION, "stored fields index data");
            put(IndexFileNames.FREQ_EXTENSION, "term frequency postings data");
            put(IndexFileNames.GEN_EXTENSION, "generation number - global file");
            put(IndexFileNames.NORMS_EXTENSION, "norms data for all fields");
            put(IndexFileNames.PLAIN_NORMS_EXTENSION, "per-field norms data");
            put(IndexFileNames.PROX_EXTENSION, "term position postings data");
            put(IndexFileNames.SEGMENTS, "per-commit list of segments");
            put(IndexFileNames.SEPARATE_NORMS_EXTENSION, "separate per-field norms data");
            put(IndexFileNames.TERMS_EXTENSION, "terms dictionary");
            put(IndexFileNames.TERMS_INDEX_EXTENSION, "terms dictionary index");
            put(IndexFileNames.VECTORS_DOCUMENTS_EXTENSION, "term vectors document data");
            put(IndexFileNames.VECTORS_FIELDS_EXTENSION, "term vector field data");
            put(IndexFileNames.VECTORS_INDEX_EXTENSION, "term vectors index");
        }
    };

    static {
        try {
            deletable = IndexFileDeleter.class.getDeclaredField("deletable");
            deletable.setAccessible(true);
            hasChanges = IndexReader.class.getDeclaredField("hasChanges");
            hasChanges.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static String getFileFunction(String file) {
        if (file == null || file.trim().length() == 0) {
            return file;
        }
        String res = null;
        file = file.trim();
        int idx = file.indexOf('.');
        String suffix = null;
        if (idx != -1) {
            suffix = file.substring(idx + 1);
        }
        if (suffix == null) {
            if (file.startsWith("segments_")) {
                return knownExtensions.get(IndexFileNames.SEGMENTS);
            }
        } else {
            res = knownExtensions.get(suffix);
            if (res != null) {
                return res;
            }
            // perhaps per-field norms?
            if (suffix.length() == 2) {
                res = knownExtensions.get(suffix.substring(0, 1));
            }
        }
        return res;
    }

    public static int getIndexFormat(final Directory dir) throws CorruptIndexException, IOException {
        SegmentInfos.FindSegmentsFile fsf = new SegmentInfos.FindSegmentsFile(dir) {

            @Override
            protected Object doBody(String segmentsFile) throws CorruptIndexException,
                    IOException {
                Integer indexFormat;
                IndexInput in = null;
                try {
                    in = dir.openInput(segmentsFile);
                    indexFormat = new Integer(in.readInt());
                } finally {
                    in.close();
                }
                return indexFormat;
            }
        };
        Integer indexFormat = (Integer) fsf.run();
        return indexFormat.intValue();
    }

    public static int getCurrentIndexFormat() {
        return SegmentInfos.CURRENT_FORMAT;
    }

    public static FormatDetails getFormatDetails(int format) {
        FormatDetails res = new FormatDetails();
        switch (format) {
            case SegmentInfos.FORMAT:
                res.capabilities = "old plain";
                res.genericName = "Lucene Pre-2.1";
                break;
            case SegmentInfos.FORMAT_LOCKLESS:
                res.capabilities = "lock-less";
                res.genericName = "Lucene 2.1";
                break;
            case SegmentInfos.FORMAT_SINGLE_NORM_FILE:
                res.capabilities = "lock-less, single norms file";
                res.genericName = "Lucene 2.2";
                break;
            case SegmentInfos.FORMAT_SHARED_DOC_STORE:
                res.capabilities = "lock-less, single norms file, shared doc store";
                res.genericName = "Lucene 2.3";
                break;
            case SegmentInfos.FORMAT_CHECKSUM:
                res.capabilities = "lock-less, single norms, shared doc store, checksum";
                res.genericName = "Lucene 2.4";
                break;
            case SegmentInfos.FORMAT_DEL_COUNT:
                res.capabilities = "lock-less, single norms, shared doc store, checksum, del count";
                res.genericName = "Lucene 2.4";
                break;
            case SegmentInfos.FORMAT_HAS_PROX:
                res.capabilities = "lock-less, single norms, shared doc store, checksum, del count, omitTf";
                res.genericName = "Lucene 2.4";
                break;
            case SegmentInfos.FORMAT_USER_DATA:
                res.capabilities = "lock-less, single norms, shared doc store, checksum, del count, omitTf, user data";
                res.genericName = "Lucene 2.9-dev";
                break;
            case SegmentInfos.FORMAT_DIAGNOSTICS:
                res.capabilities = "lock-less, single norms, shared doc store, checksum, del count, omitTf, user data, diagnostics";
                res.genericName = "Lucene 2.9";
                break;
            case SegmentInfos.FORMAT_HAS_VECTORS:
                res.capabilities = "lock-less, single norms, shared doc store, checksum, del count, omitTf, user data, diagnostics, hasVectors";
                res.genericName = "Lucene 2.9";
                break;
            case SegmentInfos.FORMAT_3_1:
                res.capabilities = "lock-less, single norms, shared doc store, checksum, del count, omitTf, user data, diagnostics, hasVectors";
                res.genericName = "Lucene 3.1";
                break;
            default:
                res.capabilities = "unknown";
                res.genericName = "Lucene 1.3 or prior";
                break;
        }
        if (SegmentInfos.CURRENT_FORMAT > format) {
            res.capabilities = "(WARNING: newer version of Lucene that this tool)";
            res.genericName = "UNKNOWN";
        }
        return res;
    }

    public static boolean preferCompoundFormat(Directory dir) throws Exception {
        SegmentInfos infos = new SegmentInfos();
        infos.read(dir);
        int compound = 0, nonCompound = 0;
        for (int i = 0; i < infos.size(); i++) {
            if (infos.info(i).getUseCompoundFile()) {
                compound++;
            } else {
                nonCompound++;
            }
        }
        return compound > nonCompound;
    }

    public static void deletePendingFiles(Directory dir, IndexDeletionPolicy policy) throws Exception {
        SegmentInfos infos = new SegmentInfos();
        infos.read(dir);
        IndexFileDeleter deleter = new IndexFileDeleter(dir, policy, infos, infoStream, null);
        deleter.close();
    }

    public static List<String> getDeletableFiles(Directory dir) throws Exception {
        SegmentInfos infos = new SegmentInfos();
        infos.read(dir);
        IndexFileDeleter deleter = new IndexFileDeleter(dir, new KeepAllIndexDeletionPolicy(), infos, infoStream, null);
        return (List<String>) deletable.get(deleter);
    }

    public static List<String> getIndexFiles(Directory dir) throws Exception {
        SegmentInfos infos = new SegmentInfos();
        infos.read(dir);
        ArrayList<String> names = new ArrayList();
        for (int i = 0; i < infos.size(); i++) {
            SegmentInfo info = infos.info(i);
            names.addAll(info.files());
            names.add(info.getDelFileName());
        }
        names.add(infos.getSegmentsFileName());
        names.add(IndexFileNames.SEGMENTS_GEN);
        return names;
    }

    public static boolean hasChanges(IndexReader ir) {
        if (ir == null) {
            return false;
        }
        try {
            return hasChanges.getBoolean(ir);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
}
