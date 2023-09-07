import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.PythonBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.ScriptBuildStep
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
}


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
                    import http.client
                    import json
                    import os
                    import subprocess
                    
                    MARKETING_WEBSITE_URL = 'marketing'
                    RELEASE_NOTES_FILENAME = 'release_notes.txt'
                    DEFAULT_BRANCH_NAME = 'main'
                    
                    def get_latest_tag():
                        subprocess.run(['git', 'config', '--global', '--add', 'safe.directory', '%teamcity.build.checkoutDir%'],
                                       stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=True)
                        result = subprocess.run(["git", "describe", "--tags", "--abbrev=0", DEFAULT_BRANCH_NAME, 'HEAD'],
                                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=True)
                    
                        nearest_tag = result.stdout.strip()
                    
                        if not nearest_tag:
                            raise Exception("Tag not found!")
                    
                        latest_tag = nearest_tag.split('\n')[-1]
                    
                        return latest_tag
                    
                    def get_release_notes(version):
                        try:
                            conn = http.client.HTTPConnection(MARKETING_WEBSITE_URL, port=80)
                            conn.request("GET", "/releasenotes")
                            response = conn.getresponse()
                    
                            if response.status == 200:
                                response_data = response.read()
                                json_data = json.loads(response_data.decode("utf-8"))
                    
                                return json_data[version]
                            else:
                                print(f"HTTP GET request failed with status code {response.status}")
                                return None
                    
                        except Exception as e:
                            print(f"An error occurred: {str(e)}")
                            return None
                    
                    def write_notes(notes):
                        with open(RELEASE_NOTES_FILENAME, 'w+') as f:
                            f.write(notes)
                    
                    if __name__ == "__main__":
                        latest_tag = get_latest_tag()
                        release_notes = get_release_notes(latest_tag)
                        if release_notes:
                            write_notes(release_notes)
                """.trimIndent()
            }
            dockerImage = "python:3.10"
            dockerImagePlatform = PythonBuildStep.ImagePlatform.Linux
            dockerRunParameters = """--network="host""""
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
