pipeline{
    agent any
    tools{
        maven "Maven"
    }
    stages{
        stage("Build JAR File"){
            steps{
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/GabrielCabreraQ/Tingeso1-kartingrm-backend']])
                
                    bat "mvn clean install"
                }
            }
        stage("Test"){
            steps{
                 bat "mvn test"
            }
        }        
        stage("Build and Push Docker Image"){
            steps{
                    script{
                            withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'docker-user', passwordVariable: 'docker-pass')]) {
                            bat "docker build -t gabrielcq/kartingrm-backend ."
                            bat 'docker login -u %docker-user% -p %docker-pass%'
                            bat "docker push gabrielcq/kartingrm-backend"
                                
                     }
                 }                 
            }
        }
    }
}
