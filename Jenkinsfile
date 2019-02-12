pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package install -DskipTests'
            }
        }
        stage('Test') {
            steps {
                chmod +x gradlew
                ./gradlew reobfuscate
            }
        }
        stage('Deploy') {
            steps {
                archiveArtifacts artifacts: 'build\*', fingerprint: true
            }
        }
    }
}
