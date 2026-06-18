/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui;

import org.jd.gui.controller.MainController;
import org.jd.gui.model.configuration.Configuration;
import org.jd.gui.service.configuration.ConfigurationPersister;
import org.jd.gui.service.configuration.ConfigurationPersisterService;
import org.jd.gui.util.I18n;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.net.InterProcessCommunicationUtil;
import org.jd.gui.util.save.SaveAllSourcesOptions;
import org.jd.gui.util.save.SaveAllSourcesUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App {
    protected static final String SINGLE_INSTANCE = "UIMainWindowPreferencesProvider.singleInstance";

    protected static MainController controller;

    public static void main(String[] args) {
        I18n.init();

        SaveAllSourcesCommand saveAllSourcesCommand = parseSaveAllSourcesCommand(args);
        if (saveAllSourcesCommand != null) {
            if (saveAllSourcesCommand.help) {
                printUsage();
                return;
            }
            runSaveAllSources(saveAllSourcesCommand);
            return;
        }

		if (checkHelpFlag(args)) {
            printUsageInGui();
		} else {
            // Load preferences
            ConfigurationPersister persister = ConfigurationPersisterService.getInstance().get();
            Configuration configuration = persister.load();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> persister.save(configuration)));

            if ("true".equals(configuration.getPreferences().get(SINGLE_INSTANCE))) {
                InterProcessCommunicationUtil ipc = new InterProcessCommunicationUtil();
                try {
                    ipc.listen(receivedArgs -> controller.openFiles(newList(receivedArgs)));
                } catch (Exception notTheFirstInstanceException) {
                    // Send args to main windows and exit
                    ipc.send(args);
                    System.exit(0);
                }
            }

            // Create SwingBuilder, set look and feel
            try {
                UIManager.setLookAndFeel(configuration.getLookAndFeel());
            } catch (Exception e) {
                configuration.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                try {
                    UIManager.setLookAndFeel(configuration.getLookAndFeel());
                } catch (Exception ee) {
                    assert ExceptionUtil.printStackTrace(ee);
                }
           }

            // Create main controller and show main frame
            controller = new MainController(configuration);
            controller.show(newList(args));
		}
	}

    protected static boolean checkHelpFlag(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if ("-h".equals(arg) || "--help".equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static void printUsage() {
        System.out.println(I18n.get("help.usage"));
    }

    protected static void printUsageInGui() {
        JOptionPane.showMessageDialog(null, I18n.get("help.usage"), Constants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    protected static void runSaveAllSources(SaveAllSourcesCommand command) {
        try {
            SaveAllSourcesOptions options = SaveAllSourcesOptions.defaults().omitMetadata(command.omitMetadata);
            SaveAllSourcesUtil.Result result = SaveAllSourcesUtil.save(command.inputFile, command.outputFile, options);
            System.out.println(I18n.get("saveAllSources.success", result.getOutputFile().getAbsolutePath(), result.getFileCount()));
        } catch (Exception e) {
            System.err.println(I18n.get("saveAllSources.failed", e.getMessage()));
            System.exit(1);
        }
    }

    protected static SaveAllSourcesCommand parseSaveAllSourcesCommand(String[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        int index = 0;
        while (index < args.length) {
            String arg = args[index];
            if ("--save-all-sources".equals(arg) || "-s".equals(arg)) {
                SaveAllSourcesCommand command = new SaveAllSourcesCommand();
                index++;

                while (index < args.length && args[index].startsWith("-")) {
                    if ("-h".equals(args[index]) || "--help".equals(args[index])) {
                        command.help = true;
                        return command;
                    }
                    if ("--no-metadata".equals(args[index])) {
                        command.omitMetadata = true;
                        index++;
                        continue;
                    }
                    System.err.println(I18n.get("saveAllSources.unknownOption", args[index]));
                    System.exit(1);
                }

                if (index >= args.length) {
                    System.err.println(I18n.get("saveAllSources.missingInput"));
                    System.exit(1);
                }

                command.inputFile = new File(args[index++]);

                if (index < args.length && !args[index].startsWith("-")) {
                    command.outputFile = new File(args[index++]);
                }

                return command;
            }
            index++;
        }

        return null;
    }

    protected static class SaveAllSourcesCommand {
        protected boolean help;
        protected boolean omitMetadata;
        protected File inputFile;
        protected File outputFile;
    }

    protected static List<File> newList(String[] paths) {
        if (paths == null) {
            return Collections.emptyList();
        } else {
            ArrayList<File> files = new ArrayList<>(paths.length);
            for (String path : paths) {
                files.add(new File(path));
            }
            return files;
        }
    }
}
