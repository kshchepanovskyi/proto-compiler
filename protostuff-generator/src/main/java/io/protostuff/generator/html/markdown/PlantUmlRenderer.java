package io.protostuff.generator.html.markdown;

import io.protostuff.generator.GeneratorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.code.Base64Coder;

public class PlantUmlRenderer {

    /**
     * Register an instance of {@link PlantUmlRenderer} in the given serializer's map.
     */
    public static void addToMap(final Map<String, PlantUmlRenderer> serializerMap) {
        PlantUmlRenderer serializer = new PlantUmlRenderer();
        for (Type type : Type.values()) {
            String name = type.getName();
            serializerMap.put(name, serializer);
        }
    }

    /**
     * Serialize given plantuml block text to string (embedded image).
     */
    public String serialize(String nodeType, String nodeText) {
        Type type = Type.getByName(nodeType);

        String formatted = type.wrap(nodeText);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SourceStringReader reader = new SourceStringReader(formatted);
        String desc;
        try {
            desc = reader.generateImage(baos, type.getFormatOption());
        } catch (IOException e) {
            throw new GeneratorException("Could not generate uml for node " + nodeType + ": " + nodeText, e);
        }
        return type.render(baos.toByteArray(), desc);
    }

    /**
     * Check if fenced block type is supported.
     */
    public boolean supports(String type) {
        for (Type value : Type.values()) {
            if (value.getName().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    enum Type {
        UML(OutputType.SVG),
        PLANTUML(OutputType.SVG),
        DOT(OutputType.SVG),
        JCCKIT(OutputType.PNG),
        DITAA(OutputType.PNG);

        private final OutputType outputType;

        Type(final OutputType outputType) {
            this.outputType = outputType;
        }

        public static Type getByName(String name) {
            return valueOf(name.toUpperCase());
        }

        public String wrap(final String originalText) {
            return "@start" + getName() + "\n" + originalText + "@end" + getName() + "\n";
        }

        public String getName() {
            return name().toLowerCase();
        }

        public FileFormatOption getFormatOption() {
            return this.outputType.getFormatOption();
        }

        public String render(byte[] bytes, final String desc) {
            return this.outputType.render(bytes, desc);
        }
    }

    enum OutputType {

        SVG {
            @Override
            public FileFormatOption getFormatOption() {
                return new FileFormatOption(FileFormat.SVG);
            }

            @Override
            public String render(byte[] bytes, final String desc) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        },

        PNG {
            @Override
            public FileFormatOption getFormatOption() {
                return new FileFormatOption(FileFormat.PNG);
            }

            @Override
            public String render(final byte[] bytes, final String desc) {
                var base64 = new String(Base64Coder.encode(bytes));
                return String.format("<img alt=\"%s\" src=\"data:image/png;base64,%s\"/>", desc, base64);
            }
        };

        public abstract FileFormatOption getFormatOption();

        public abstract String render(byte[] bytes, final String desc);
    }
}