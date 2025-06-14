pipeline {
    agent none
    
    environment {
        DOCKER_IMAGE = "anuopp/java-ecommerce"
        BUILD_TAG = "v2.${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout Code') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          containers:
                          - name: git
                            image: alpine/git:latest
                            command: [cat]
                            tty: true
                            workingDir: /home/jenkins/agent
                    """
                }
            }
            steps {
                container('git') {
                    echo "🔄 Checking out main branch..."
                    git branch: 'main',
                        url: 'https://github.com/Anu-Opp/P1-Java-E-Commerce.git',
                        credentialsId: 'github-credentials'
                    stash includes: '**', name: 'source-code'
                }
            }
        }
        
        stage('Build with Maven') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          containers:
                          - name: maven
                            image: maven:3.8.6-eclipse-temurin-17
                            command: [cat]
                            tty: true
                            workingDir: /home/jenkins/agent
                    """
                }
            }
            steps {
                container('maven') {
                    echo "🔨 Building Java application..."
                    unstash 'source-code'
                    sh 'mvn clean package -DskipTests'
                    stash includes: 'target/*.jar,Dockerfile', name: 'build-artifacts'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Build & Push Docker Image') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          containers:
                          - name: kaniko
                            image: gcr.io/kaniko-project/executor:debug
                            command: ["/busybox/cat"]
                            tty: true
                            workingDir: /kaniko/workspace
                    """
                }
            }
            steps {
                container('kaniko') {
                    echo "🐳 Building and pushing Docker image..."
                    unstash 'build-artifacts'
                    
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', 
                                                   passwordVariable: 'DOCKER_PASS', 
                                                   usernameVariable: 'DOCKER_USER')]) {
                        sh '''
                            mkdir -p /kaniko/.docker
                            echo "{\\"auths\\":{\\"https://index.docker.io/v1/\\":{\\"username\\":\\"$DOCKER_USER\\",\\"password\\":\\"$DOCKER_PASS\\"}}}" > /kaniko/.docker/config.json
                            
                            /kaniko/executor \
                              --dockerfile=Dockerfile \
                              --context=. \
                              --destination=${DOCKER_IMAGE}:${BUILD_TAG} \
                              --destination=${DOCKER_IMAGE}:latest \
                              --cache=true
                            
                            echo "✅ Successfully built and pushed:"
                            echo "   - ${DOCKER_IMAGE}:${BUILD_TAG}"
                            echo "   - ${DOCKER_IMAGE}:latest"
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Dev Environment') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          serviceAccountName: jenkins
                          containers:
                          - name: kubectl
                            image: bitnami/kubectl:1.28
                            command: [cat]
                            tty: true
                            workingDir: /home/jenkins/agent
                            resources:
                              requests:
                                memory: "64Mi"
                                cpu: "50m"
                              limits:
                                memory: "128Mi"
                                cpu: "100m"
                    """
                }
            }
            steps {
                container('kubectl') {
                    echo "🚀 Deploying to dev environment..."
                    
                    script {
                        sh """
                            echo "📦 Deploying to dev namespace..."
                            
                            # Create namespace if it doesn't exist
                            kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -
                            
                            # Update the deployment with new image
                            kubectl set image deployment/ecommerce-deployment java-ecommerce=${DOCKER_IMAGE}:${BUILD_TAG} -n dev || \\
                            kubectl create deployment ecommerce-deployment --image=${DOCKER_IMAGE}:${BUILD_TAG} -n dev
                            
                            # Ensure service exists
                            kubectl expose deployment ecommerce-deployment --port=80 --target-port=8080 --name=ecommerce-service -n dev --dry-run=client -o yaml | kubectl apply -f -
                            
                            # Wait for rollout with shorter timeout
                            kubectl rollout status deployment/ecommerce-deployment -n dev --timeout=300s
                            
                            echo ""
                            echo "📊 Deployment Status:"
                            kubectl get deployment ecommerce-deployment -n dev -o wide
                            kubectl get pods -n dev -l app=ecommerce-deployment -o wide
                            kubectl get service ecommerce-service -n dev
                            
                            echo ""
                            echo "✅ Deployment completed successfully!"
                            echo "🎯 Application deployed with image: ${DOCKER_IMAGE}:${BUILD_TAG}"
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Pipeline execution completed"
        }
        success {
            echo "🎉 SUCCESS: Pipeline completed successfully!"
            echo "📋 Build Summary:"
            echo "   ✅ Code checked out from main branch"
            echo "   ✅ Java application built with Maven"
            echo "   ✅ Docker image built and pushed: ${BUILD_TAG}"
            echo "   ✅ Application deployed to dev environment"
            echo ""
            echo "🚀 Your application is now running!"
        }
        failure {
            echo "❌ FAILURE: Pipeline failed!"
            echo "🔧 Check logs above for details"
        }
    }
}
