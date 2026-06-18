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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class SaveAllSourcesOptionsView {
    protected SaveAllSourcesOptions result;

    public SaveAllSourcesOptions show(JFrame parent) {
        result = null;

        JDialog dialog = new JDialog(parent, I18n.get("dialog.saveAllSources.options"), true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        dialog.add(content);

        JLabel hintLabel = new JLabel(I18n.get("dialog.saveAllSources.options.hint"));
        content.add(hintLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        JCheckBox omitMetadataCheckBox = new JCheckBox(I18n.get("saveAllSources.omitMetadata"));
        JCheckBox omitLineNumbersCheckBox = new JCheckBox(I18n.get("saveAllSources.omitLineNumbers"));
        JCheckBox keepNestedArchivesCheckBox = new JCheckBox(I18n.get("saveAllSources.keepNestedArchives"));
        optionsPanel.add(omitMetadataCheckBox);
        optionsPanel.add(omitLineNumbersCheckBox);
        optionsPanel.add(keepNestedArchivesCheckBox);
        content.add(optionsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton okButton = new JButton(I18n.get("button.ok"));
        JButton cancelButton = new JButton(I18n.get("button.cancel"));

        Action okAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                result = SaveAllSourcesOptions.defaults()
                    .omitMetadata(omitMetadataCheckBox.isSelected())
                    .omitLineNumbers(omitLineNumbersCheckBox.isSelected())
                    .keepNestedArchives(keepNestedArchivesCheckBox.isSelected());
                dialog.dispose();
            }
        };
        Action cancelAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                result = null;
                dialog.dispose();
            }
        };

        okButton.addActionListener(okAction);
        cancelButton.addActionListener(cancelAction);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        content.add(buttonsPanel, BorderLayout.SOUTH);

        JRootPane rootPane = dialog.getRootPane();
        rootPane.setDefaultButton(okButton);
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "SaveAllSourcesOptionsView.cancel");
        rootPane.getActionMap().put("SaveAllSourcesOptionsView.cancel", cancelAction);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result;
    }
}
