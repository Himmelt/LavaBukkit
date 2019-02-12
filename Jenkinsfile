pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package install deploy:deploy javadoc:javadoc'
                sh 'chmod +x gradlew && ./gradlew reobfuscate'

            }
        }
    }
    stages {
        stage('Archive') {
            steps {


                archiveArtifacts artifacts: 'build/localCache/*.jar', fingerprint: true 
            }
        }
    }
}
