pipeline {
    agent {
        docker {
            image 'maven:3.8.6-openjdk-17'
            args '-v /root/.m2:/root/.m2'
        }
    }
    
    environment {
        DOCKER_IMAGE = "anuopp/java-ecommerce"
        BRANCH_NAME = "${env.GIT_BRANCH}"
        BUILD_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                echo "🔄 Checking out ${env.GIT_BRANCH} branch..."
                git branch: 'dev',
                    url: 'https://github.com/Anu-Opp/P1-Java-E-Commerce.git',
                    credentialsId: 'github-credentials'
            }
        }
        
        stage('Build with Maven') {
            steps {
                echo "🔨 Building Java application..."
                sh 'mvn clean package -DskipTests'
                
                // Archive the built artifacts
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Run Tests') {
            steps {
                echo "🧪 Running unit tests..."
                sh 'mvn test'
            }
            post {
                always {
                    // Publish test results
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Build Docker Image') {
            agent any // Run outside Maven container
            steps {
                echo "🐳 Building Docker image..."
                script {
                    // Build with branch-specific tag for dev
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_TAG} ."
                    sh "docker build -t ${DOCKER_IMAGE}:dev-latest ."
                }
            }
        }
        
        stage('Push Docker Image') {
            agent any
            steps {
                echo "📤 Pushing Docker image to registry..."
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', 
                                               passwordVariable: 'PASS', 
                                               usernameVariable: 'USER')]) {
                    sh '''
                        echo $PASS | docker login -u $USER --password-stdin
                        docker push ${DOCKER_IMAGE}:${BUILD_TAG}
                        docker push ${DOCKER_IMAGE}:dev-latest
                    '''
                }
            }
        }
        
        stage('Setup Kubeconfig') {
            agent any
            steps {
                echo "⚙️ Setting up Kubernetes configuration..."
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                        mkdir -p ~/.kube
                        cp $KUBECONFIG_FILE ~/.kube/config
                        chmod 600 ~/.kube/config
                    '''
                }
            }
        }
        
        stage('Deploy to Dev Environment') {
            agent any
            steps {
                echo "🚀 Deploying to development environment..."
                script {
                    // Update deployment with new image
                    sh """
                        # Update deployment.yaml with new image tag
                        sed -i 's|image: .*|image: ${DOCKER_IMAGE}:${BUILD_TAG}|g' deployment.yaml
                        
                        # Apply to dev namespace
                        kubectl apply -f deployment.yaml -n dev
                        kubectl apply -f service.yaml -n dev
                        
                        # Wait for rollout to complete
                        kubectl rollout status deployment/ecommerce-deployment -n dev --timeout=300s
                        
                        # Display deployment info
                        kubectl get pods -n dev -l app=ecommerce
                        kubectl get service -n dev
                    """
                }
            }
        }
        
        stage('Health Check') {
            agent any
            steps {
                echo "🏥 Performing health check..."
                script {
                    sleep(30) // Wait for pods to be ready
                    sh '''
                        # Get service endpoint
                        kubectl get service ecommerce-service -n dev
                        
                        # Check if pods are running
                        kubectl get pods -n dev -l app=ecommerce
                        
                        echo "✅ Dev deployment completed successfully!"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Cleaning up..."
            sh 'docker system prune -f'
        }
        success {
            echo "✅ Dev pipeline completed successfully!"
            script {
                if (env.BRANCH_NAME == 'dev') {
                    echo "🎯 Ready for promotion to main branch!"
                    echo "📋 To promote to production:"
                    echo "   1. Test the dev environment thoroughly"
                    echo "   2. Create a Pull Request from dev to main"
                    echo "   3. After merge, the production pipeline will trigger"
                }
            }
        }
        failure {
            echo "❌ Dev pipeline failed!"
            emailext (
                subject: "❌ Jenkins Dev Build Failed - ${JOB_NAME} #${BUILD_NUMBER}",
                body: "Dev build failed. Please check the console output and fix issues before promoting to production.",
                to: "your-email@example.com"
            )
        }
    }
}