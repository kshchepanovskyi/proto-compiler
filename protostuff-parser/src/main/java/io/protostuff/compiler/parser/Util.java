package io.protostuff.compiler.parser;

import com.google.common.base.Preconditions;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utility functions for parser.
 *
 * @author Kostiantyn Shchepanovskyi
 */
@ParametersAreNonnullByDefault
public class Util {

    public static final char QUOTE = '"';
    // path delimiters
    private static final char WINDOWS_DELIMITER = '\\';
    private static final char LINUX_DELIMITER = '/';

    private Util() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Remove first and last character from given string and return result.
     *
     * @param text given string
     *
     * @return substring of given string - without first and last characters
     * @deprecated replaced by {@link #trimStringName(String)}
     */
    public static String removeFirstAndLastChar(String text) {
        return trimStringName(text);
    }

    /**
     * Remove first and last character from given string name and return result.
     *
     * @param text given string
     *
     * @return substring of given string - without first and last characters
     */
    public static String trimStringName(String text) {
        Preconditions.checkNotNull(text, "text can not be null");
        int n = text.length();
        return text.substring(1, n - 1);
    }

    /**
     * Remove first and last character from given string value and return result.
     *
     * A string value can be wrapped by either a backtick, `, or triple double
     * quote, """.
     *
     * @param text given string value
     *
     * @return substring of given string - without first and last characters
     */
    public static String trimStringValue(String text) {
        Preconditions.checkNotNull(text, "text can not be null");
        int size = text.startsWith("`") ? 1 : 3;
        int n = text.length();
        return text.substring(size, n - size);
    }

    /**
     * Returns file name by given absolute or relative file location.
     * TODO: remove unused?
     */
    public static String getFileName(String fullPath) {
        Preconditions.checkNotNull(fullPath, "path can not be null");
        int winDelimiterPos = fullPath.lastIndexOf(WINDOWS_DELIMITER);
        int linDelimiterPos = fullPath.lastIndexOf(LINUX_DELIMITER);
        int pos = Math.max(winDelimiterPos, linDelimiterPos);
        return fullPath.substring(pos + 1, fullPath.length());
    }
}
