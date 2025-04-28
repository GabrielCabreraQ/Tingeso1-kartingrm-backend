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
                         withDockerRegistry(credentialsId: 'docker-credentials'){
                            bat "docker build -t gabrielcq/kartingrm-backend ."
                            bat "docker push gabrielcq/kartingrm-backend"
                        }
                 }                 
            }
        }
    }
}
