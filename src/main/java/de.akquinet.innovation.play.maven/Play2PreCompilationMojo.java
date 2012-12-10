/*
 * Copyright 2012 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.akquinet.innovation.play.maven;

import de.akquinet.innovation.play.util.Test;
import de.akquinet.innovation.play.util.Test$;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Compile the Play application
 *
 * @author blake
 * @goal precompile
 * @requiresDependencyResolution compile+runtime
 */
public class Play2PreCompilationMojo
    extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project;

    /**
     * The default language to use.
     *
     * @parameter
     * @required
     */
    String defaultLang;

    @SuppressWarnings("unchecked")
    public void execute()
        // @configurator include-project-dependencies
        throws MojoExecutionException {
        List<String> compileSourceRoots = project.getCompileSourceRoots();
        if (compileSourceRoots == null || compileSourceRoots.isEmpty()) {
            throw new MojoExecutionException("Compile source root is not set!");
        }

        if (compileSourceRoots.size() > 1) {
            throw new MojoExecutionException("There can only be one compile source root!");
        }

        getLog().info("Default lang: " + defaultLang);

        final Test$ test = Test$.MODULE$;
        test.compileRoutes(project.getBasedir(), new File(project.getBuild().getOutputDirectory()), defaultLang);
        test.compileTemplates(new File(compileSourceRoots.get(0)), new File(project.getBuild().getOutputDirectory()), defaultLang);
    }
}
