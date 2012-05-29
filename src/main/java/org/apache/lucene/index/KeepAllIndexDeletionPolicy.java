package org.apache.lucene.index;

import java.io.IOException;
import java.util.List;

public class KeepAllIndexDeletionPolicy implements IndexDeletionPolicy {

    @Override
    public void onCommit(List commits) throws IOException {
    }

    @Override
    public void onInit(List commits) throws IOException {
    }
}
