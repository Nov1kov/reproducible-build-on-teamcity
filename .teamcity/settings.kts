import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.script
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

    subProject(WhatsappBusinessJavaApi)
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
            name = "docker clean"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            mavenVersion = bundled_3_8()
        }
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


object WhatsappBusinessJavaApi : Project({
    name = "Whatsapp Business Java Api"

    vcsRoot(WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain)

    buildType(WhatsappBusinessJavaApi_Build)
})

object WhatsappBusinessJavaApi_Build : BuildType({
    name = "Generate documentation"

    artifactRules = """
        javadoc => javadoc.zip
        target/whatsapp-business-java-api-javadoc.jar => whatsapp-business-java-api-javadoc.jar
    """.trimIndent()
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain)
    }

    steps {
        script {
            name = "get release notes"
            scriptContent = """
                branch_name="main"
                
                nearest_tag=${'$'}(git describe --tags --abbrev=0 "${'$'}branch_name" HEAD)
                
                echo ${'$'}nearest_tag
                
                latest_tag=${'$'}(echo "${'$'}nearest_tag" | rev | cut -d ' ' -f 1 | rev)
                
                echo ${'$'}latest_tag
            """.trimIndent()
        }
        script {
            name = "javadoc"
            scriptContent = "mvn clean javadoc:javadoc javadoc:jar"
            dockerImage = "maven:3-eclipse-temurin-17"
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
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

object WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain : GitVcsRoot({
    name = "https://github.com/Bindambc/whatsapp-business-java-api.git#refs/heads/main"
    url = "https://github.com/Bindambc/whatsapp-business-java-api.git"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
})
