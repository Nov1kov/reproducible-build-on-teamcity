import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.PythonBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.python
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
        python {
            command = script {
                content = """
                    import subprocess
                    import os
                    
                    def get_nearest_tag(branch_name, commit_hash):
                      	print(os.environ)
                        print('%teamcity.build.checkoutDir%')
                        result = subprocess.run(["git", "describe", "--tags", "--abbrev=0", branch_name, commit_hash],
                                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=False)
                        print(result.stderr.strip())
                        nearest_tag = result.stdout.strip()
                    
                        if not nearest_tag:
                            raise Exception("Tag isn't found!")
                    
                    if __name__ == "__main__":
                        branch_name = "main"
                        commit_hash = "HEAD"
                        
                        nearest_tag = get_nearest_tag(branch_name, commit_hash)
                        print(nearest_tag)
                """.trimIndent()
            }
            dockerImage = "python:3.10"
            dockerImagePlatform = PythonBuildStep.ImagePlatform.Linux
        }
        script {
            name = "get release notes"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            scriptContent = """
                branch_name="main"
                
                git config --global --add safe.directory %teamcity.build.checkoutDir%
                nearest_tag=${'$'}(git describe --tags --abbrev=0 "${'$'}branch_name" HEAD)
                
                latest_tag=${'$'}(echo "${'$'}nearest_tag" | tail -n 1)
                
                echo ${'$'}latest_tag
            """.trimIndent()
            dockerImage = "python:3.10"
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
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
