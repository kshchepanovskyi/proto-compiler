pipeline {
    agent any
    tools {
        maven 'M35'
        jdk 'JDK 11'
    }
    options {
        ansiColor('xterm')
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }
    stages {
        stage('Set Version') {
            steps {
                sh '''                    
                    if [[ "$BRANCH_NAME" =~ ^(master|release/).*$ ]]; then
                      VERSION=$(cat release.txt).${BUILD_ID}                                                              
                    else
                      VERSION=$(cat release.txt).dev.${BUILD_ID}
                    fi                    
                    mvn versions:set versions:commit -DnewVersion=${VERSION}                    
                '''
            }
        }
        stage('Build') {
            steps {
                sshagent(['GITHUB_SSH_KEY']) {
                    sh '''
                        if [[ "$BRANCH_NAME" =~ ^(master|release/).*$ ]]; then
                          mvn clean deploy scm:tag -P release                                                              
                        else
                          mvn clean package -P release
                        fi                    
                    '''
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**/*.xml'
                }
            }
        }
    }
    post {
        failure {
            emailext(
                    mimeType: 'text/html',
                    body: '${JELLY_SCRIPT,template="html"}',
                    recipientProviders: [
                            [$class: 'CulpritsRecipientProvider'],
                            [$class: 'DevelopersRecipientProvider'],
                            [$class: 'RequesterRecipientProvider']
                    ],
                    subject: 'Build failed - ${JOB_NAME}'
            )
        }
        unstable {
            emailext(
                    mimeType: 'text/html',
                    body: '${JELLY_SCRIPT,template="html"}',
                    recipientProviders: [
                            [$class: 'CulpritsRecipientProvider'],
                            [$class: 'DevelopersRecipientProvider'],
                            [$class: 'RequesterRecipientProvider']
                    ],
                    subject: 'Build unstable - ${JOB_NAME}'
            )
        }
    }
}