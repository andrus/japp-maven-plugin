package org.objectstyle.japp;

import java.util.Collection;
import java.util.Collections;

import org.apache.maven.artifact.Artifact;

class ArtifactMatchPattern {

    private Collection<String> strings;

    ArtifactMatchPattern(Collection<String> patterns) {
        strings = (patterns != null) ? patterns : Collections.<String> emptyList();
    }

    boolean matchInclude(Artifact artifact) {
        if (strings.isEmpty()) {
            return true;
        }

        return match(artifact);
    }

    boolean matchExclude(Artifact artifact) {
        if (strings.isEmpty()) {
            return false;
        }

        return match(artifact);
    }

    private boolean match(Artifact artifact) {
        String key = artifact.getId();

        for (String pattern : strings) {
            if (key.startsWith(pattern)) {
                return true;
            }
        }

        return false;
    }
}
