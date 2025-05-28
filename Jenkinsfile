pipeline {
    agent any
    environment {
        DOCKER_IMAGE = "anuopp/java-ecommerce"
    }
    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'test', 
                    url: 'https://github.com/Anu-Opp/P1-Java-E-Commerce.git',
                    credentialsId: 'github-credentials'
        }
        stage('Build with Maven') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }
        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                    sh 'echo $PASS | docker login -u $USER --password-stdin'
                    sh 'docker push $DOCKER_IMAGE'
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f deployment.yaml'
                sh 'kubectl apply -f service.yaml'
            }
        }
    }
}
