package io.github.jeremylong.spring.build.analyzer;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * Modified version of the Compile class found at https://blog.jooq.org/how-to-compile-a-class-at-runtime-with-java-8-and-9/.
 */
public class Compile {


    static void compile(String className, String content, OutputStream out)
            throws Exception {
        Lookup lookup = MethodHandles.lookup();
        compile0(className, content, lookup, out);
    }

    static void compile0(
            String className, String content, Lookup lookup, OutputStream out)
            throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        ClassFileManager manager = new ClassFileManager(
                compiler.getStandardFileManager(null, null, null), out);

        List<CharSequenceJavaFileObject> files = new ArrayList<>();
        files.add(new CharSequenceJavaFileObject(className, content));

        Class appListenter = Class.forName("org.springframework.context.ApplicationListener");
        String springCP = appListenter.getProtectionDomain().getCodeSource().getLocation().toString().substring(5);
        List<String> options = new ArrayList<String>();
        options.add("-classpath");
        String currentDir = new File(".").getAbsolutePath();
        String classpath = currentDir + File.separator + "target" + File.separator + "classes"
                + System.getProperty("path.separator") + System.getProperty("java.class.path")
                + System.getProperty("path.separator") + springCP;

        options.add(classpath);

        compiler.getTask(null, manager, null, options, null, files)
                .call();
    }

    // These are some utility classes needed for the JavaCompiler
    // ----------------------------------------------------------

    static final class JavaFileObject extends SimpleJavaFileObject {
        private OutputStream os = null;

        JavaFileObject(String name, JavaFileObject.Kind kind, OutputStream out) {
            super(URI.create(
                            "string:///"
                                    + name.replace('.', '/')
                                    + kind.extension),
                    kind);
            os = out;
        }


        @Override
        public OutputStream openOutputStream() {
            return os;
        }
    }

    static final class ClassFileManager
            extends ForwardingJavaFileManager<StandardJavaFileManager> {
        JavaFileObject o;
        OutputStream out;

        ClassFileManager(StandardJavaFileManager m, OutputStream out) {
            super(m);
            this.out = out;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
                JavaFileManager.Location location,
                String className,
                JavaFileObject.Kind kind,
                FileObject sibling
        ) {
            return o = new JavaFileObject(className, kind, out);
        }
    }

    static final class CharSequenceJavaFileObject
            extends SimpleJavaFileObject {
        final CharSequence content;

        public CharSequenceJavaFileObject(
                String className,
                CharSequence content
        ) {
            super(URI.create(
                            "string:///"
                                    + className.replace('.', '/')
                                    + JavaFileObject.Kind.SOURCE.extension),
                    JavaFileObject.Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(
                boolean ignoreEncodingErrors
        ) {
            return content;
        }
    }
}