package com.hospital;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WebUtilTest {
    @Test
    void escapesHtmlSpecialCharacters() {
        assertEquals("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt; &amp; &#x27;test&#x27;",
            WebUtil.escape("<script>alert(\"x\")</script> & 'test'"));
    }

    @Test
    void returnsEmptyStringForNull() {
        assertEquals("", WebUtil.escape(null));
    }

    @Test
    void preservesOrdinaryText() {
        assertEquals("Demo Patient 123", WebUtil.escape("Demo Patient 123"));
    }

    @Test
    void escapesAmpersandsBeforeOtherCharactersWithoutDoubleEscapingGeneratedEntities() {
        assertEquals("&amp;lt; &amp; &quot;quoted&quot;",
            WebUtil.escape("&lt; & \"quoted\""));
    }

    @Test
    void safelyEscapesUnicodeAndLineBreaksWithoutLosingContent() {
        assertEquals("Divya — Demo&#x27;s\nRecord", WebUtil.escape("Divya — Demo's\nRecord"));
    }
}
