package io.protostuff.generator.html.markdown;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FlexmarkMarkdownProcessorTest {

    FlexmarkMarkdownProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new FlexmarkMarkdownProcessor();
    }

    @Test
    @DisplayName("Simple text")
    void test() {
        assertThat(processor.toHtml("Hello, world"))
                .isEqualTo("<p>Hello, world</p>");
    }
}
