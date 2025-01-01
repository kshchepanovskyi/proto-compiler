package io.protostuff.generator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.multibindings.MapBinder.newMapBinder;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import io.protostuff.generator.dummy.DummyGenerator;
import io.protostuff.generator.html.HtmlCompiler;
import io.protostuff.generator.html.HtmlGenerator;
import io.protostuff.generator.html.json.enumeration.JsonEnumGenerator;
import io.protostuff.generator.html.json.index.JsonIndexGenerator;
import io.protostuff.generator.html.json.message.JsonMessageGenerator;
import io.protostuff.generator.html.json.pages.JsonPageGenerator;
import io.protostuff.generator.html.json.pages.JsonPagesIndexGenerator;
import io.protostuff.generator.html.json.proto.JsonProtoGenerator;
import io.protostuff.generator.html.json.service.JsonServiceGenerator;
import io.protostuff.generator.html.markdown.FlexmarkMarkdownProcessor;
import io.protostuff.generator.html.markdown.MarkdownProcessor;
import io.protostuff.generator.java.JavaExtensionProvider;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;

/**
 * Guice module for code generators.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class CompilerModule extends AbstractModule {

    public static final String JAVA_COMPILER_TEMPLATE = "io/protostuff/generator/java/main.stg";

    public static final String TEMPLATES_OPTION = "templates";
    public static final String EXTENSIONS_OPTION = "extensions";

    public static final String JAVA_COMPILER = "java";
    public static final String ST4_COMPILER = "st4";
    public static final String HTML_COMPILER = "html";
    public static final String DUMMY_COMPILER = "dummy";

    @Override
    protected void configure() {
        bind(CompilerRegistry.class);
        bind(CompilerUtils.class);
        bind(MarkdownProcessor.class).to(FlexmarkMarkdownProcessor.class).in(Scopes.SINGLETON);
        install(new FactoryModuleBuilder()
                .implement(ProtoCompiler.class, StCompiler.class)
                .build(StCompilerFactory.class));
        install(new FactoryModuleBuilder()
                .implement(ProtoCompiler.class, ExtensibleStCompiler.class)
                .build(ExtensibleStCompilerFactory.class));
        Multibinder<HtmlCompiler> htmlCompilerBinder = Multibinder.newSetBinder(binder(), HtmlCompiler.class);
        htmlCompilerBinder.addBinding().to(JsonIndexGenerator.class);
        htmlCompilerBinder.addBinding().to(JsonEnumGenerator.class);
        htmlCompilerBinder.addBinding().to(JsonMessageGenerator.class);
        htmlCompilerBinder.addBinding().to(JsonServiceGenerator.class);
        htmlCompilerBinder.addBinding().to(JsonProtoGenerator.class);
        htmlCompilerBinder.addBinding().to(JsonPagesIndexGenerator.class);
        htmlCompilerBinder.addBinding().to(JsonPageGenerator.class);
        MapBinder<String, ProtoCompiler> compilers = newMapBinder(binder(), String.class, ProtoCompiler.class);
        compilers.addBinding(HTML_COMPILER).to(HtmlGenerator.class);
        compilers.addBinding(JAVA_COMPILER).toProvider(JavaCompilerProvider.class);
        compilers.addBinding(ST4_COMPILER).toProvider(St4CompilerProvider.class);
        compilers.addBinding(DUMMY_COMPILER).to(DummyGenerator.class).in(Scopes.SINGLETON);
    }

    @Provides
    OutputStreamFactory outputStreamFactory() {
        return location -> {
            try {
                Path path = Paths.get(location);
                Path dir = path.getParent();
                Files.createDirectories(dir);
                return new FileOutputStream(location);
            } catch (IOException e) {
                throw new GeneratorException("Could not create file: %s", e, location);
            }
        };
    }

    public static class JavaCompilerProvider implements Provider<ProtoCompiler> {

        private final ExtensibleStCompilerFactory factory;

        @Inject
        public JavaCompilerProvider(ExtensibleStCompilerFactory factory) {
            this.factory = factory;
        }

        @Override
        public ProtoCompiler get() {
            ExtensionProvider extensionProvider = new JavaExtensionProvider();
            return factory.create(Collections.singletonList(JAVA_COMPILER_TEMPLATE), extensionProvider);
        }
    }

    public static class St4CompilerProvider implements Provider<ProtoCompiler> {

        private final ExtensibleStCompilerFactory factory;

        @Inject
        public St4CompilerProvider(ExtensibleStCompilerFactory factory) {
            this.factory = factory;
        }

        @Override
        public ProtoCompiler get() {
            // deffer compiler instantiation until wrapper is called with
            // configured template and extension provider class
            return module -> {
                try {
                    Map<String, Object> options = module.getOptions();
                    @SuppressWarnings("unchecked")
                    Collection<String> templates = checkNotNull((Collection<String>) options.get(TEMPLATES_OPTION),
                            TEMPLATES_OPTION + " is not set");
                    String extProviderClass = checkNotNull((String) options.get(EXTENSIONS_OPTION),
                            EXTENSIONS_OPTION + " is not set");
                    ExtensionProvider extensionProvider = instantiate(extProviderClass, ExtensionProvider.class);
                    ProtoCompiler compiler = factory.create(templates, extensionProvider);
                    compiler.compile(module);
                } catch (Exception e) {
                    throw new GeneratorException("Could not compile module: %s, module=%s", e, e.getMessage(), module);
                }
            };
        }

        private <T> T instantiate(final String className, final Class<T> type) {
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                return type.cast(instance);
            } catch (Exception e) {
                throw new GeneratorException("Could not instantiate " + className, e);
            }
        }


    }
}
