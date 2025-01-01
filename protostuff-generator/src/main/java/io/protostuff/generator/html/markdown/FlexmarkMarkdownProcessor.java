package io.protostuff.generator.html.markdown;

import static com.vladsch.flexmark.parser.PegdownExtensions.ABBREVIATIONS;
import static com.vladsch.flexmark.parser.PegdownExtensions.ATXHEADERSPACE;
import static com.vladsch.flexmark.parser.PegdownExtensions.AUTOLINKS;
import static com.vladsch.flexmark.parser.PegdownExtensions.DEFINITIONS;
import static com.vladsch.flexmark.parser.PegdownExtensions.FENCED_CODE_BLOCKS;
import static com.vladsch.flexmark.parser.PegdownExtensions.FORCELISTITEMPARA;
import static com.vladsch.flexmark.parser.PegdownExtensions.RELAXEDHRULES;
import static com.vladsch.flexmark.parser.PegdownExtensions.SMARTYPANTS;
import static com.vladsch.flexmark.parser.PegdownExtensions.STRIKETHROUGH;
import static com.vladsch.flexmark.parser.PegdownExtensions.SUPPRESS_ALL_HTML;
import static com.vladsch.flexmark.parser.PegdownExtensions.TABLES;
import static com.vladsch.flexmark.parser.PegdownExtensions.TASKLISTITEMS;
import static com.vladsch.flexmark.parser.PegdownExtensions.WIKILINKS;

import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class FlexmarkMarkdownProcessor implements MarkdownProcessor {


    public static final MutableDataSet OPTIONS1 = new MutableDataSet()
            .setAll(PegdownOptionsAdapter.flexmarkOptions(SMARTYPANTS
                    | ABBREVIATIONS
                    | AUTOLINKS
                    | TABLES
                    | FENCED_CODE_BLOCKS
                    | DEFINITIONS
                    | SUPPRESS_ALL_HTML
                    | STRIKETHROUGH
                    | ATXHEADERSPACE
                    | FORCELISTITEMPARA
                    | RELAXEDHRULES
                    | TASKLISTITEMS
                    | WIKILINKS))
            .set(WikiLinkExtension.LINK_ESCAPE_CHARS, " +<>")
            .set(WikiLinkExtension.LINK_REPLACE_CHARS, "----");

    private final Parser parser;
    private final HtmlRenderer renderer;

    /**
     * Markdown renderer based on flexmark-java.
     */
    public FlexmarkMarkdownProcessor() {
        parser = Parser.builder(OPTIONS1).build();
        //options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
        renderer = HtmlRenderer.builder(OPTIONS1)
                .nodeRendererFactory(new TypeLinkAndPlantUmlRenderer.Factory())
                .build();
    }

    @Override
    public String toHtml(String source) {
        Node document = parser.parse(source);
        String result = renderer.render(document);
        // Workaround for https://github.com/vsch/flexmark-java/issues/26
        // We do not need extra trailing newline added here
        if (!source.endsWith("\n") && result.endsWith("\n")) {
            return result.substring(0, result.length() - 1);
        }
        return result;
    }
}