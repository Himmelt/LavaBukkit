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
    
    discordSend description: 'LavaBukkit, Forge + Spigot together again', footer: 'Matrix Development (c) 2019', image: '', link: 'env.BUILD_URL', result: '', thumbnail: '', title: 'env.JOB_NAME', webhookURL: 'https://discordapp.com/api/webhooks/544701529832161282/YtGhWFmJJEeHS_I0kcLvPcyqSsYOQf6wgNVjJSQJEfevMthJ8xDxELOynIZe96X640eD'
}
