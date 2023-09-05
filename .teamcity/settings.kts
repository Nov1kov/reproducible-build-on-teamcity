import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.05"

project {
    description = "Contains all other projects"

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }

    subProject(SimpleJavaMavenApp)
}


object SimpleJavaMavenApp : Project({
    name = "Simple Java Maven App"

    vcsRoot(SimpleJavaMavenApp_HttpsGithubComJenkinsDocsSimpleJavaMavenAppRefsHeadsMaster)

    buildType(SimpleJavaMavenApp_JavaDocs)
})

object SimpleJavaMavenApp_JavaDocs : BuildType({
    name = "JavaDocs"

    vcs {
        root(SimpleJavaMavenApp_HttpsGithubComJenkinsDocsSimpleJavaMavenAppRefsHeadsMaster)
    }

    steps {
        maven {
            name = "clean"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            mavenVersion = bundled_3_8()
            jdkHome = "%env.JDK_11_0%"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})

object SimpleJavaMavenApp_HttpsGithubComJenkinsDocsSimpleJavaMavenAppRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/jenkins-docs/simple-java-maven-app#refs/heads/master"
    url = "https://github.com/jenkins-docs/simple-java-maven-app"
    branch = "refs/heads/master"
    branchSpec = "refs/heads/*"
})
