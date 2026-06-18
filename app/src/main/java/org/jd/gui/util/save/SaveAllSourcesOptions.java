/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.save;

import java.util.HashMap;
import java.util.Map;

/**
 * Options for {@link SaveAllSourcesUtil}.
 */
public class SaveAllSourcesOptions {
    public static final String WRITE_METADATA = "ClassFileSaverPreferences.writeMetadata";

    protected boolean omitMetadata;

    protected SaveAllSourcesOptions() {}

    public static SaveAllSourcesOptions defaults() {
        return new SaveAllSourcesOptions();
    }

    /**
     * Omit decompilation metadata comments appended to each source file
     * (Location, Java compiler version, JD-Core Version).
     * Default is {@code false} (metadata comments are written).
     */
    public SaveAllSourcesOptions omitMetadata(boolean omitMetadata) {
        this.omitMetadata = omitMetadata;
        return this;
    }

    public boolean isOmitMetadata() {
        return omitMetadata;
    }

    public Map<String, String> applyTo(Map<String, String> preferences) {
        Map<String, String> merged = new HashMap<>(preferences);

        if (omitMetadata) {
            merged.put(WRITE_METADATA, "false");
        }

        return merged;
    }
}
