/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.save;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.service.configuration.ConfigurationPersisterService;
import org.jd.gui.service.sourcesaver.SourceSaverService;
import org.jd.gui.spi.ContainerFactory;
import org.jd.gui.spi.SourceSaver;
import org.jd.gui.util.exception.ExceptionUtil;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Headless utility that exports decompiled sources, equivalent to the GUI "Save All Sources" action.
 */
public class SaveAllSourcesUtil {
    protected static final String[] ARCHIVE_EXTENSIONS = {
        "jar", "war", "ear", "zip", "kar", "aar", "jmod"
    };

    protected SaveAllSourcesUtil() {}

    public static Result save(File inputFile) throws IOException {
        return save(inputFile, null, SaveAllSourcesOptions.defaults(), null);
    }

    public static Result save(File inputFile, File outputFile) throws IOException {
        return save(inputFile, outputFile, SaveAllSourcesOptions.defaults(), null);
    }

    public static Result save(File inputFile, SaveAllSourcesOptions options) throws IOException {
        return save(inputFile, null, options, null);
    }

    public static Result save(File inputFile, File outputFile, SaveAllSourcesOptions options) throws IOException {
        return save(inputFile, outputFile, options, null);
    }

    public static Result save(File inputFile, File outputFile, SaveAllSourcesOptions options, Listener listener) throws IOException {
        Map<String, String> basePreferences = ConfigurationPersisterService.getInstance().get().load().getPreferences();
        Map<String, String> preferences = (options != null) ? options.applyTo(basePreferences) : basePreferences;
        return save(inputFile, outputFile, preferences, listener);
    }

    public static Result save(File inputFile, File outputFile, Map<String, String> preferences, Listener listener) throws IOException {
        if (inputFile == null) {
            throw new IllegalArgumentException("Input file must not be null");
        }
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Input file not found: " + inputFile.getAbsolutePath());
        }
        if (!inputFile.canRead()) {
            throw new IOException("Cannot read input file: " + inputFile.getAbsolutePath());
        }

        Map<String, String> prefs = (preferences != null)
            ? preferences
            : ConfigurationPersisterService.getInstance().get().load().getPreferences();

        HeadlessApi api = new HeadlessApi(prefs);

        try (OpenedContainer opened = openContainer(api, inputFile)) {
            Container.Entry entry = opened.container.getRoot().getParent();
            SourceSaver saver = SourceSaverService.getInstance().get(entry);

            if (saver == null) {
                throw new IOException("No source saver available for: " + inputFile.getAbsolutePath());
            }

            File targetFile = (outputFile != null) ? outputFile : new File(saver.getSourcePath(entry));
            int fileCount = saver.getFileCount(api, entry);

            Path targetPath = targetFile.toPath();
            Path parentPath = targetPath.getParent();

            if ((parentPath != null) && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            Files.deleteIfExists(targetPath);

            boolean cancelled = false;
            SourceSaver.Controller controller = () -> cancelled;
            SourceSaver.Listener saveListener = (path) -> {
                if (listener != null) {
                    listener.pathSaved(path);
                }
            };

            try {
                URI uri = targetPath.toUri();
                URI archiveUri = new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath() + "!/", null);

                try (FileSystem archiveFs = FileSystems.newFileSystem(archiveUri, Collections.singletonMap("create", "true"))) {
                    Path archiveRootPath = archiveFs.getPath("/");
                    saver.saveContent(api, controller, saveListener, archiveRootPath, archiveRootPath, entry);
                }
            } catch (URISyntaxException e) {
                throw new IOException("Failed to create output archive: " + targetFile.getAbsolutePath(), e);
            }

            if (cancelled) {
                Files.deleteIfExists(targetPath);
                throw new IOException("Save all sources was cancelled");
            }

            Result result = new Result(targetFile, fileCount);
            if (listener != null) {
                listener.finished(result);
            }
            return result;
        }
    }

    protected static OpenedContainer openContainer(API api, File file) throws IOException {
        if (file.isDirectory()) {
            return openDirectoryContainer(api, file);
        }

        String extension = getExtension(file.getName());
        if (isArchiveExtension(extension)) {
            return openArchiveContainer(api, file);
        }

        throw new IOException("Unsupported input file type: " + file.getAbsolutePath());
    }

    protected static OpenedContainer openDirectoryContainer(API api, File directory) throws IOException {
        Path rootPath = directory.toPath();
        FileContainerEntry parentEntry = new FileContainerEntry(directory);
        ContainerFactory containerFactory = api.getContainerFactory(rootPath);

        if (containerFactory == null) {
            throw new IOException("No container factory available for: " + directory.getAbsolutePath());
        }

        Container container = containerFactory.make(api, parentEntry, rootPath);
        if (container == null) {
            throw new IOException("Failed to open directory: " + directory.getAbsolutePath());
        }

        parentEntry.setChildren(container.getRoot().getChildren());
        return new OpenedContainer(container, null);
    }

    protected static OpenedContainer openArchiveContainer(API api, File file) throws IOException {
        try {
            URI fileUri = file.toURI();
            URI uri = new URI("jar:" + fileUri.getScheme(), fileUri.getHost(), fileUri.getPath() + "!/", null);
            FileSystem fileSystem;

            try {
                fileSystem = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }

            Path rootPath = fileSystem.getRootDirectories().iterator().next();
            FileContainerEntry parentEntry = new FileContainerEntry(file);
            ContainerFactory containerFactory = api.getContainerFactory(rootPath);

            if (containerFactory == null) {
                fileSystem.close();
                throw new IOException("No container factory available for: " + file.getAbsolutePath());
            }

            Container container = containerFactory.make(api, parentEntry, rootPath);
            if (container == null) {
                fileSystem.close();
                throw new IOException("Failed to open archive: " + file.getAbsolutePath());
            }

            parentEntry.setChildren(container.getRoot().getChildren());
            return new OpenedContainer(container, fileSystem);
        } catch (URISyntaxException e) {
            throw new IOException("Failed to open archive: " + file.getAbsolutePath(), e);
        }
    }

    protected static boolean isArchiveExtension(String extension) {
        for (String archiveExtension : ARCHIVE_EXTENSIONS) {
            if (archiveExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    protected static String getExtension(String name) {
        int lastDot = name.lastIndexOf('.');
        return (lastDot == -1) ? "" : name.substring(lastDot + 1).toLowerCase();
    }

    public static class Result {
        protected final File outputFile;
        protected final int fileCount;

        public Result(File outputFile, int fileCount) {
            this.outputFile = outputFile;
            this.fileCount = fileCount;
        }

        public File getOutputFile() { return outputFile; }

        public int getFileCount() { return fileCount; }
    }

    public interface Listener {
        void pathSaved(Path path);

        default void finished(Result result) {}
    }

    protected static class OpenedContainer implements AutoCloseable {
        protected final Container container;
        protected final FileSystem fileSystem;

        protected OpenedContainer(Container container, FileSystem fileSystem) {
            this.container = container;
            this.fileSystem = fileSystem;
        }

        @Override
        public void close() throws IOException {
            if (fileSystem != null) {
                fileSystem.close();
            }
        }
    }

    protected static class FileContainerEntry implements Container.Entry {
        protected static final Container PARENT_CONTAINER = new Container() {
            @Override public String getType() { return "generic"; }
            @Override public Container.Entry getRoot() { return null; }
        };

        protected Collection<Container.Entry> children = Collections.emptyList();
        protected final File file;
        protected final URI uri;
        protected final String path;

        protected FileContainerEntry(File file) {
            this.file = file;
            this.uri = file.toURI();
            this.path = normalizePath(uri.getPath());
        }

        protected static String normalizePath(String path) {
            if (path.endsWith("/")) {
                return path.substring(0, path.length() - 1);
            }
            return path;
        }

        public void setChildren(Collection<Container.Entry> children) {
            this.children = children;
        }

        @Override public Container getContainer() { return PARENT_CONTAINER; }
        @Override public Container.Entry getParent() { return null; }
        @Override public URI getUri() { return uri; }
        @Override public String getPath() { return path; }
        @Override public boolean isDirectory() { return file.isDirectory(); }
        @Override public long length() { return file.length(); }
        @Override public Collection<Container.Entry> getChildren() { return children; }

        @Override
        public InputStream getInputStream() {
            try {
                return new BufferedInputStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                assert ExceptionUtil.printStackTrace(e);
                return null;
            }
        }
    }
}
