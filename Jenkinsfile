pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package install javadoc:javadoc'
                sh 'chmod +x gradlew && ./gradlew reobfuscate'
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'build/localCache/*.jar', fingerprint: true 
            }
        }
    }
}
