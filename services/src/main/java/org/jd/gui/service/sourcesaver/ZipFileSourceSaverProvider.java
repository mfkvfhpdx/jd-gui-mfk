/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourcesaver;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.spi.SourceSaver;
import org.jd.gui.util.exception.ExceptionUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ZipFileSourceSaverProvider extends DirectorySourceSaverProvider {
    protected static final String KEEP_NESTED_ARCHIVES = "ClassFileSaverPreferences.keepNestedArchives";

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.zip", "*:file:*.jar", "*:file:*.war", "*:file:*.ear", "*:file:*.aar", "*:file:*.jmod", "*:file:*.kar"); }

    @Override
    public int getFileCount(API api, Container.Entry entry) {
        if (shouldKeepNestedArchive(api, entry)) {
            return 1;
        }
        return super.getFileCount(api, entry);
    }

    @Override
    public void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Container.Entry entry) {
        if (shouldKeepNestedArchive(api, entry)) {
            copyNestedArchive(listener, rootPath, entry);
            return;
        }

        try {
            String sourcePath = getSourcePath(entry);
            Path path = rootPath.resolve(sourcePath);
            Path parentPath = path.getParent();

            if ((parentPath != null) && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            File tmpSourceFile = api.loadSourceFile(entry);

            if (tmpSourceFile != null) {
                Files.copy(tmpSourceFile.toPath(), path);
            } else {
                File tmpFile = File.createTempFile("jd-gui.", ".tmp.zip");

                tmpFile.delete();
                tmpFile.deleteOnExit();

                URI tmpFileUri = tmpFile.toURI();
                URI tmpArchiveUri = new URI("jar:" + tmpFileUri.getScheme(), tmpFileUri.getHost(), tmpFileUri.getPath() + "!/", null);

                HashMap<String, String> env = new HashMap<>();
                env.put("create", "true");

                FileSystem tmpArchiveFs = FileSystems.newFileSystem(tmpArchiveUri, env);
                Path tmpArchiveRootPath = tmpArchiveFs.getPath("/");

                saveContent(api, controller, listener, tmpArchiveRootPath, tmpArchiveRootPath, entry);

                tmpArchiveFs.close();

                Files.move(tmpFile.toPath(), path);
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    protected void copyNestedArchive(SourceSaver.Listener listener, Path rootPath, Container.Entry entry) {
        Path path = rootPath.resolve(entry.getPath());
        Path parentPath = path.getParent();

        try {
            if ((parentPath != null) && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            listener.pathSaved(path);

            try (InputStream is = entry.getInputStream()) {
                Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }

    protected boolean shouldKeepNestedArchive(API api, Container.Entry entry) {
        if (!getPreferenceValue(api.getPreferences(), KEEP_NESTED_ARCHIVES, false)) {
            return false;
        }

        Container.Entry parent = entry.getParent();
        if (parent == null) {
            return false;
        }

        Container container = entry.getContainer();
        if (container == null) {
            return false;
        }

        String type = container.getType();
        return "jar".equals(type) || "war".equals(type) || "ear".equals(type) || "kar".equals(type);
    }

    protected static boolean getPreferenceValue(Map<String, String> preferences, String key, boolean defaultValue) {
        String value = preferences.get(key);
        return (value == null) ? defaultValue : Boolean.valueOf(value);
    }
}
