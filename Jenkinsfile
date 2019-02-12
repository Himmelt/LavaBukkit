pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package install deploy:deploy javadoc:javadoc'
                sh 'chmod +x gradlew && ./gradlew reobfuscate'
                archiveArtifacts artifacts: 'build/localCache/*.jar', fingerprint: true 
            }
        }
    }
}
