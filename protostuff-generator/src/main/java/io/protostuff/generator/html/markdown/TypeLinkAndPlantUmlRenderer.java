package io.protostuff.generator.html.markdown;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ext.wikilink.WikiLink;
import com.vladsch.flexmark.ext.wikilink.internal.WikiLinkOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.DelegatingNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.CharSubSequence;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

class TypeLinkAndPlantUmlRenderer implements NodeRenderer {

    private static void renderWikiLink(@NotNull WikiLink node, @NotNull NodeRendererContext context, @NotNull HtmlWriter html) {
        String source = node.getLink().toString();
        String url = source;
        String text = source;
        int pos;
        if ((pos = source.indexOf('|')) >= 0) {
            url = source.substring(0, pos);
            text = source.substring(pos + 1);
        }
        DataHolder options = context.getOptions();
        WikiLinkOptions wlo = new WikiLinkOptions(options);
        node.setLink(CharSubSequence.of("#/types/" + url), wlo.allowAnchors, wlo.allowAnchorEscape);
        node.setText(CharSubSequence.of(text));
        context.delegateRender();
    }

    public static class Factory implements DelegatingNodeRendererFactory {

        @Override
        public @NotNull NodeRenderer apply(@NotNull DataHolder dataHolder) {
            return new TypeLinkAndPlantUmlRenderer();
        }

        @Override
        public Set<Class<?>> getDelegates() {
            ///Set<Class<? extends NodeRendererFactory>> set = new HashSet<Class<? extends NodeRendererFactory>>();
            // add node renderer factory classes to which this renderer will delegate some of its rendering
            // core node renderer is assumed to have all depend it so there is no need to add it
            //set.add(WikiLinkNodeRenderer.Factory.class);
            //return set;

            // return null if renderer does not delegate or delegates only to core node renderer
            return null;
        }

    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(WikiLink.class, TypeLinkAndPlantUmlRenderer::renderWikiLink));
        set.add(new NodeRenderingHandler<>(FencedCodeBlock.class, TypeLinkAndPlantUmlRenderer::renderFencedCodeBlock));
        return set;
    }

    private static void renderFencedCodeBlock(@NotNull FencedCodeBlock node, @NotNull NodeRendererContext context, @NotNull HtmlWriter html) {
        // test the node to see if it needs overriding
        String type = node.getInfo().toString();
        PlantUmlRenderer plantUmlRenderer = new PlantUmlRenderer();
        if (plantUmlRenderer.supports(type)) {
            html.raw(plantUmlRenderer.serialize(type, node.getContentChars().normalizeEOL()));
        } else {
            context.delegateRender();
        }
    }
}