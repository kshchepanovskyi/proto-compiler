package io.protostuff.compiler.parser;

import org.junit.jupiter.api.Test;

import static io.protostuff.compiler.parser.Util.trimStringName;
import static io.protostuff.compiler.parser.Util.trimStringValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class UtilTest {

    @Test
    public void testTrimStringName_expected() throws Exception {
        assertEquals("abc", trimStringName("\"abc\""));
    }

    @Test
    public void testTrimStringValue_expected() throws Exception {
        assertEquals("abc", trimStringValue("`abc`"));
        assertEquals("abc", trimStringValue("\"\"\"abc\"\"\""));
    }
}
