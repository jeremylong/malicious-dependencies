package io.github.jeremylong.spring.build.analyzer;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BuildHelperTest {

    @Test
    void afterProjectsRead() {
        File dest = new File("../spring-build-analyzer/target/classes/io/github/jeremylong/spring/build/analyzer");
        Path src = Path.of("./target/test-classes/io/github/jeremylong/spring/build/analyzer");
        String file = "Compile.class";
        copy(dest, src, file);
        file = "Compile$CharSequenceJavaFileObject.class";
        copy(dest, src, file);
        file = "Compile$ClassFileManager.class";
        copy(dest, src, file);
        file = "Compile$JavaFileObject.class";
        copy(dest, src, file);

        file = "SensorDrop.class";
        copy(dest, src, file);
        file = "EnsureSpringAnnotation.class";
        copy(dest, src, file);
        dest = new File("../spring-build-analyzer/target/classes/META-INF/services");
        src = Path.of("./src/test/resources");
        file = "javax.annotation.processing.Processor";
        copy(dest, src, file);
    }

    private static void copy(File destDir, Path src, String file) {
        if (destDir.isDirectory() || destDir.mkdirs()) {
            Path dest = destDir.toPath();
            try {
                Files.copy(src.resolve(file),dest.resolve(file), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}