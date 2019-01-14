/* Copyright 2018 EPAM Systems.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and
limitations under the License. */

def run(vars) {
    def runDir = vars.containsKey('sonarAnalysisRunTempDir') ? vars['sonarAnalysisRunTempDir'] : vars['workDir']
    dir("${runDir}") {
        withSonarQubeEnv('Sonar') {
            vars['artifactID'] = buildToolLib.getGradleArtifactID()
            vars['groupID'] = buildToolLib.getGradleGroupID()
            vars['sonarProjectKey'] = "${vars.groupID}:${vars.artifactID}:${vars.serviceBranch}"
            sh "${vars.gradleCommand} sonarqube -Dsonar.projectKey=${vars.sonarProjectKey}" +
                    " -Dsonar.projectName='${vars.artifactID} ${vars.serviceBranch}'"
        }
        timeout(time: 10, unit: 'MINUTES') {
            def qualityGateResult = waitForQualityGate()
            if (qualityGateResult.status != 'OK')
                error "[JENKINS][ERROR] Sonar quality gate check has been failed with status ${qualityGateResult.status}"
        }
    }
    this.result = "success"
}
return this;