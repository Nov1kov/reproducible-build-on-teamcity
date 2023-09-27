import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.buildSteps.PythonBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
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
}


object WhatsappBusinessJavaApi : Project({
    name = "Whatsapp Business Java Api"

    vcsRoot(WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain)

    buildType(WhatsappBusinessJavaApi_CheckReleaseNotes)
    buildType(WhatsappBusinessJavaApi_Build)

    features {
        buildReportTab {
            id = "PROJECT_EXT_3"
            title = "Documentation"
            startPage = "javadoc.zip!/index.html"
        }
        buildReportTab {
            id = "PROJECT_EXT_4"
            title = "Release notes"
            startPage = "javadoc.zip!/release_notes.txt"
        }
    }
})

object WhatsappBusinessJavaApi_Build : BuildType({
    name = "Generate documentation"

    artifactRules = """
        javadoc => javadoc.zip
        release_notes.txt => javadoc.zip
        target/whatsapp-business-java-api-javadoc.jar => whatsapp-business-java-api-javadoc.jar
    """.trimIndent()
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain)
    }

    steps {
        python {
            name = "Get release notes"
            command = file {
                filename = "ci.py"
            }
            dockerImage = "python:3.10"
            dockerImagePlatform = PythonBuildStep.ImagePlatform.Linux
            dockerRunParameters = """--network="host""""
        }
        script {
            name = "Build Javadoc"
            scriptContent = "mvn clean javadoc:javadoc javadoc:jar"
            dockerImage = "maven:3-eclipse-temurin-17"
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
        }
    }

    features {
        perfmon {
        }
        swabra {
        }
        pullRequests {
            vcsRootExtId = "${WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain.id}"
            provider = github {
                authType = token {
                    token = "credentialsJSON:99a9421e-b846-4c57-b0bd-e2f5ba86ac6b"
                }
                filterTargetBranch = "main"
                filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
            }
        }
    }
})

object WhatsappBusinessJavaApi_CheckReleaseNotes : BuildType({
    name = "Check release notes"

    vcs {
        root(WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain)
    }

    steps {
        python {
            name = "Get release notes"
            command = file {
                filename = "ci.py"
            }
            dockerImage = "python:3.10"
            dockerImagePlatform = PythonBuildStep.ImagePlatform.Linux
            dockerRunParameters = """--network="host""""
        }
    }

    features {
        perfmon {
        }
        pullRequests {
            vcsRootExtId = "${WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain.id}"
            provider = github {
                authType = token {
                    token = "credentialsJSON:99a9421e-b846-4c57-b0bd-e2f5ba86ac6b"
                }
                filterTargetBranch = "main"
                filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
            }
        }
    }
})

object WhatsappBusinessJavaApi_HttpsGithubComBindambcWhatsappBusinessJavaApiGitRefsHeadsMain : GitVcsRoot({
    name = "Javadoc example"
    url = "https://github.com/Nov1kov/javadoc-example.git"
    branch = "refs/heads/main"
    branchSpec = "+:refs/heads/main"
})
