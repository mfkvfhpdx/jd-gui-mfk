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
import org.jd.gui.spi.*;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * {@link API} decorator that supplies overridden preferences for a single save operation.
 */
public class PreferencesOverlayApi implements API {
    protected final API delegate;
    protected final Map<String, String> preferences;

    public PreferencesOverlayApi(API delegate, Map<String, String> preferences) {
        this.delegate = delegate;
        this.preferences = preferences;
    }

    @Override public boolean openURI(URI uri) { return delegate.openURI(uri); }
    @Override public boolean openURI(int x, int y, Collection<Container.Entry> entries, String query, String fragment) { return delegate.openURI(x, y, entries, query, fragment); }
    @Override public void addURI(URI uri) { delegate.addURI(uri); }
    @Override public <T extends JComponent & UriGettable> void addPanel(String title, Icon icon, String tip, T component) { delegate.addPanel(title, icon, tip, component); }
    @Override public Collection<Action> getContextualActions(Container.Entry entry, String fragment) { return delegate.getContextualActions(entry, fragment); }
    @Override public UriLoader getUriLoader(URI uri) { return delegate.getUriLoader(uri); }
    @Override public FileLoader getFileLoader(File file) { return delegate.getFileLoader(file); }
    @Override public ContainerFactory getContainerFactory(Path rootPath) { return delegate.getContainerFactory(rootPath); }
    @Override public PanelFactory getMainPanelFactory(Container container) { return delegate.getMainPanelFactory(container); }
    @Override public TreeNodeFactory getTreeNodeFactory(Container.Entry entry) { return delegate.getTreeNodeFactory(entry); }
    @Override public TypeFactory getTypeFactory(Container.Entry entry) { return delegate.getTypeFactory(entry); }
    @Override public Indexer getIndexer(Container.Entry entry) { return delegate.getIndexer(entry); }
    @Override public SourceSaver getSourceSaver(Container.Entry entry) { return delegate.getSourceSaver(entry); }
    @Override public Map<String, String> getPreferences() { return preferences; }
    @Override public Collection<Future<Indexes>> getCollectionOfFutureIndexes() { return delegate.getCollectionOfFutureIndexes(); }
    @Override public String getSource(Container.Entry entry) { return delegate.getSource(entry); }
    @Override public void loadSource(Container.Entry entry, LoadSourceListener listener) { delegate.loadSource(entry, listener); }
    @Override public File loadSourceFile(Container.Entry entry) { return delegate.loadSourceFile(entry); }
}
