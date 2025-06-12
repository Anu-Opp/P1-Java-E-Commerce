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
                    """
                }
            }
            steps {
                container('git') {
                    echo "🔄 Checking out dev branch..."
                    git branch: 'dev',
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
                    """
                }
            }
            steps {
                container('maven') {
                    echo "🔨 Building Java application with Maven..."
                    unstash 'source-code'
                    sh 'mvn clean package -DskipTests'
                    stash includes: 'target/*.jar,Dockerfile,deployment.yaml,service.yaml', name: 'build-artifacts'
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
                    """
                }
            }
            steps {
                container('kaniko') {
                    echo "🐳 Building and pushing Docker image with Kaniko..."
                    unstash 'build-artifacts'
                    
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', 
                                                   passwordVariable: 'DOCKER_PASS', 
                                                   usernameVariable: 'DOCKER_USER')]) {
                        script {
                            sh '''
                                # Create Docker config for Kaniko
                                mkdir -p /kaniko/.docker
                                echo "{\\"auths\\":{\\"https://index.docker.io/v1/\\":{\\"username\\":\\"$DOCKER_USER\\",\\"password\\":\\"$DOCKER_PASS\\"}}}" > /kaniko/.docker/config.json
                                
                                # Build and push with Kaniko
                                /kaniko/executor \
                                  --dockerfile=Dockerfile \
                                  --context=. \
                                  --destination=${DOCKER_IMAGE}:${BUILD_TAG} \
                                  --destination=${DOCKER_IMAGE}:dev-latest \
                                  --cache=true \
                                  --verbosity=info
                                
                                echo "✅ Successfully built and pushed:"
                                echo "   - ${DOCKER_IMAGE}:${BUILD_TAG}"
                                echo "   - ${DOCKER_IMAGE}:dev-latest"
                            '''
                        }
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
                    """
                }
            }
            steps {
                container('kubectl') {
                    echo "🚀 Deploying to dev environment..."
                    unstash 'build-artifacts'
                    script {
                        sh """
                            # Create dev namespace
                            kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -
                            
                            # Update deployment with new image
                            sed -i 's|image: anuopp/java-ecommerce:.*|image: ${DOCKER_IMAGE}:${BUILD_TAG}|g' deployment.yaml
                            
                            # Apply manifests
                            kubectl apply -f deployment.yaml -n dev
                            kubectl apply -f service.yaml -n dev
                            
                            # Wait for rollout
                            kubectl rollout status deployment/ecommerce-deployment -n dev --timeout=300s
                            
                            echo "📊 Deployment Status:"
                            kubectl get pods -n dev -l app=ecommerce
                            kubectl get service -n dev ecommerce-service
                            kubectl get ingress -n dev ecommerce-dev-ingress
                            
                            # Get application URL
                            INGRESS_URL=\$(kubectl get ingress ecommerce-dev-ingress -n dev -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "Provisioning...")
                            echo "🌍 Application URL: http://\$INGRESS_URL"
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
            echo "🎉 SUCCESS: Complete CI/CD pipeline executed!"
            echo "📋 Build Summary:"
            echo "   ✅ Source code checked out from GitHub"
            echo "   ✅ Java application built with Maven"
            echo "   ✅ Docker image built and tagged: ${BUILD_TAG}"
            echo "   ✅ Image pushed to DockerHub repository"
            echo "   ✅ Application deployed to dev namespace"
            echo ""
            echo "🏆 PROJECT 1 REQUIREMENT SATISFIED:"
            echo "   'Use Jenkins to automate build and deployment' ✅"
        }
        failure {
            echo "❌ FAILURE: Check above logs for details"
        }
    }
}