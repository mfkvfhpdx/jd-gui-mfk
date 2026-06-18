/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.save;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.service.container.ContainerFactoryService;
import org.jd.gui.service.fileloader.FileLoaderService;
import org.jd.gui.service.indexer.IndexerService;
import org.jd.gui.service.mainpanel.PanelFactoryService;
import org.jd.gui.service.sourceloader.SourceLoaderService;
import org.jd.gui.service.sourcesaver.SourceSaverService;
import org.jd.gui.service.treenode.TreeNodeFactoryService;
import org.jd.gui.service.type.TypeFactoryService;
import org.jd.gui.service.uriloader.UriLoaderService;
import org.jd.gui.spi.*;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Minimal {@link API} implementation for headless operations such as saving all sources.
 */
public class HeadlessApi implements API {
    protected final Map<String, String> preferences;
    protected final SourceLoaderService sourceLoaderService;

    public HeadlessApi(Map<String, String> preferences) {
        this.preferences = preferences;
        this.sourceLoaderService = new SourceLoaderService();
    }

    @Override public boolean openURI(URI uri) { return false; }

    @Override public boolean openURI(int x, int y, Collection<Container.Entry> entries, String query, String fragment) { return false; }

    @Override public void addURI(URI uri) {}

    @Override public <T extends JComponent & UriGettable> void addPanel(String title, Icon icon, String tip, T component) {}

    @Override public Collection<Action> getContextualActions(Container.Entry entry, String fragment) { return Collections.emptyList(); }

    @Override public UriLoader getUriLoader(URI uri) { return UriLoaderService.getInstance().get(this, uri); }

    @Override public FileLoader getFileLoader(File file) { return FileLoaderService.getInstance().get(this, file); }

    @Override public ContainerFactory getContainerFactory(Path rootPath) { return ContainerFactoryService.getInstance().get(this, rootPath); }

    @Override public PanelFactory getMainPanelFactory(Container container) { return PanelFactoryService.getInstance().get(container); }

    @Override public TreeNodeFactory getTreeNodeFactory(Container.Entry entry) { return TreeNodeFactoryService.getInstance().get(entry); }

    @Override public TypeFactory getTypeFactory(Container.Entry entry) { return TypeFactoryService.getInstance().get(entry); }

    @Override public Indexer getIndexer(Container.Entry entry) { return IndexerService.getInstance().get(entry); }

    @Override public SourceSaver getSourceSaver(Container.Entry entry) { return SourceSaverService.getInstance().get(entry); }

    @Override public Map<String, String> getPreferences() { return preferences; }

    @Override public Collection<Future<Indexes>> getCollectionOfFutureIndexes() { return Collections.emptyList(); }

    @Override public String getSource(Container.Entry entry) { return sourceLoaderService.getSource(this, entry); }

    @Override public void loadSource(Container.Entry entry, LoadSourceListener listener) {
        String source = sourceLoaderService.loadSource(this, entry);
        if ((source != null) && !source.isEmpty()) {
            listener.sourceLoaded(source);
        }
    }

    @Override public File loadSourceFile(Container.Entry entry) { return sourceLoaderService.getSourceFile(this, entry); }
}
