pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package install -DskipTests'
                sh 'chmod +x gradlew && ./gradlew reobfuscate'
                archiveArtifacts artifacts: 'build/*', fingerprint: true 
            }
        }
    }
}
