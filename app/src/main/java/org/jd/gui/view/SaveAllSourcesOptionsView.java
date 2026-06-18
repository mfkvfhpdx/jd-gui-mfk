/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.util.I18n;
import org.jd.gui.util.save.SaveAllSourcesOptions;

import javax.swing.*;
import java.awt.*;

public class SaveAllSourcesOptionsView {
    protected JCheckBox omitMetadataCheckBox;
    protected JCheckBox omitLineNumbersCheckBox;
    protected JCheckBox keepNestedArchivesCheckBox;

    public SaveAllSourcesOptions show(JFrame parent) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        omitMetadataCheckBox = new JCheckBox(I18n.get("saveAllSources.omitMetadata"));
        omitLineNumbersCheckBox = new JCheckBox(I18n.get("saveAllSources.omitLineNumbers"));
        keepNestedArchivesCheckBox = new JCheckBox(I18n.get("saveAllSources.keepNestedArchives"));

        panel.add(omitMetadataCheckBox);
        panel.add(omitLineNumbersCheckBox);
        panel.add(keepNestedArchivesCheckBox);

        int choice = JOptionPane.showConfirmDialog(
            parent,
            panel,
            I18n.get("dialog.saveAllSources.options"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return null;
        }

        return SaveAllSourcesOptions.defaults()
            .omitMetadata(omitMetadataCheckBox.isSelected())
            .omitLineNumbers(omitLineNumbersCheckBox.isSelected())
            .keepNestedArchives(keepNestedArchivesCheckBox.isSelected());
    }
}
