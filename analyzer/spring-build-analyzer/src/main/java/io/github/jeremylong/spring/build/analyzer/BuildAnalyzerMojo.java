package io.github.jeremylong.spring.build.analyzer;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "analyze-spring-build", defaultPhase = LifecyclePhase.COMPILE)
public class BuildAnalyzerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        List<Dependency> dependencies = project.getDependencies();
        long numDependencies = dependencies.size();
        getLog().info("\n--------------------------------\n--------------------------------\nNumber of dependencies: " + numDependencies + "\n--------------------------------\n--------------------------------");
    }
}
