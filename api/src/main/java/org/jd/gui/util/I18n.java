/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;

    public static void init() {
        Locale locale = Locale.getDefault();
        if (!"zh".equals(locale.getLanguage())) {
            locale = Locale.SIMPLIFIED_CHINESE;
            Locale.setDefault(locale);
        }
        bundle = ResourceBundle.getBundle("org.jd.gui.messages", locale);
    }

    public static String get(String key) {
        if (bundle == null) {
            init();
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
