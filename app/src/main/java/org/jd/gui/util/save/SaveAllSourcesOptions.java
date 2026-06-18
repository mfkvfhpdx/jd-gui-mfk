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
    public static final String WRITE_LINE_NUMBERS = "ClassFileSaverPreferences.writeLineNumbers";
    public static final String KEEP_NESTED_ARCHIVES = "ClassFileSaverPreferences.keepNestedArchives";

    protected boolean omitMetadata;
    protected boolean omitLineNumbers;
    protected boolean keepNestedArchives;

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

    /**
     * Omit per-line decompilation comments such as {@code /*    *\/} and {@code /* 22 *\/}.
     * Default is {@code false} (line number comments are written).
     */
    public SaveAllSourcesOptions omitLineNumbers(boolean omitLineNumbers) {
        this.omitLineNumbers = omitLineNumbers;
        return this;
    }

    /**
     * Omit both metadata block comments and per-line line-number comments.
     */
    public SaveAllSourcesOptions omitComments(boolean omitComments) {
        this.omitMetadata = omitComments;
        this.omitLineNumbers = omitComments;
        return this;
    }

    /**
     * Keep nested archive files (jar, war, zip, etc.) as binary copies instead of decompiling them.
     * Default is {@code false} (nested archives are decompiled into .src.zip files).
     */
    public SaveAllSourcesOptions keepNestedArchives(boolean keepNestedArchives) {
        this.keepNestedArchives = keepNestedArchives;
        return this;
    }

    public boolean isKeepNestedArchives() {
        return keepNestedArchives;
    }

    public boolean isOmitMetadata() {
        return omitMetadata;
    }

    public boolean isOmitLineNumbers() {
        return omitLineNumbers;
    }

    public Map<String, String> applyTo(Map<String, String> preferences) {
        Map<String, String> merged = new HashMap<>(preferences);

        if (omitMetadata) {
            merged.put(WRITE_METADATA, "false");
        }
        if (omitLineNumbers) {
            merged.put(WRITE_LINE_NUMBERS, "false");
        }
        if (keepNestedArchives) {
            merged.put(KEEP_NESTED_ARCHIVES, "true");
        }

        return merged;
    }
}
