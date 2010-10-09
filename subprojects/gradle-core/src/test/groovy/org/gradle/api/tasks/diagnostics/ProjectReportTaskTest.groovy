/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.tasks.diagnostics

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.logging.internal.TestStyledTextOutput
import org.gradle.util.HelperUtil
import spock.lang.Specification

class ProjectReportTaskTest extends Specification {
    private final ProjectInternal project = HelperUtil.createRootProject()

    def rendersReportForRootProjectWithChildren() {
        ProjectReportTask task = HelperUtil.createTask(ProjectReportTask, project)
        project.description = 'this is the root project'
        Project child1 = HelperUtil.createChildProject(project, "child1")
        child1.description = 'this is a subproject'
        HelperUtil.createChildProject(child1, "child1")
        HelperUtil.createChildProject(project, "child2")
        task.textOutput = new TestStyledTextOutput()

        when:
        task.listProjects()

        then:
        task.textOutput.value == toNative('''
Root project 'test' - this is the root project
+--- Project ':child1' - this is a subproject
|    \\--- Project ':child1:child1'
\\--- Project ':child2'

To see a list of the tasks of a particular project, run gradle <project-path>:tasks
For example, try running gradle :child1:tasks
''')
    }

    def rendersReportForRootProjectWithNoChildren() {
        ProjectReportTask task = HelperUtil.createTask(ProjectReportTask, project)
        project.description = 'this is the root project'
        task.textOutput = new TestStyledTextOutput()

        when:
        task.listProjects()

        then:
        task.textOutput.value == toNative('''
Root project 'test' - this is the root project
No sub-projects

To see a list of the tasks of a particular project, run gradle <project-path>:tasks
For example, try running gradle :tasks
''')
    }

    def rendersReportForNonRootProjectWithNoChildren() {
        Project child1 = HelperUtil.createChildProject(project, "child1")
        ProjectReportTask task = HelperUtil.createTask(ProjectReportTask, child1)
        task.textOutput = new TestStyledTextOutput()

        when:
        task.listProjects()

        then:
        task.textOutput.value == toNative('''
Project ':child1'
No sub-projects

To see a list of the tasks of a particular project, run gradle <project-path>:tasks
For example, try running gradle :child1:tasks

To see a list of all the projects in this build, run gradle :projects
''')
    }

    def String toNative(String value) {
        return value.replaceAll('\n', System.getProperty('line.separator'))
    }
}
