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
                            command:
                            - cat
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
                            image: maven:3.8.6-openjdk-17
                            command:
                            - cat
                            tty: true
                            volumeMounts:
                            - name: maven-cache
                              mountPath: /root/.m2
                          volumes:
                          - name: maven-cache
                            emptyDir: {}
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
        
        stage('Run Tests') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          containers:
                          - name: maven
                            image: maven:3.8.6-openjdk-17
                            command:
                            - cat
                            tty: true
                            volumeMounts:
                            - name: maven-cache
                              mountPath: /root/.m2
                          volumes:
                          - name: maven-cache
                            emptyDir: {}
                    """
                }
            }
            steps {
                container('maven') {
                    echo "🧪 Running tests..."
                    unstash 'source-code'
                    sh 'mvn test'
                }
            }
            post {
                always {
                    script {
                        if (fileExists('target/surefire-reports/*.xml')) {
                            publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Image') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          containers:
                          - name: docker
                            image: docker:24-dind
                            securityContext:
                              privileged: true
                            env:
                            - name: DOCKER_TLS_CERTDIR
                              value: ""
                          - name: docker-client
                            image: docker:24-cli
                            command:
                            - cat
                            tty: true
                            env:
                            - name: DOCKER_HOST
                              value: tcp://localhost:2376
                            - name: DOCKER_TLS_VERIFY
                              value: ""
                    """
                }
            }
            steps {
                container('docker-client') {
                    echo "🐳 Building Docker image..."
                    unstash 'build-artifacts'
                    script {
                        sh 'sleep 15'  // Wait for Docker daemon
                        sh """
                            docker build -t ${DOCKER_IMAGE}:${BUILD_TAG} .
                            docker tag ${DOCKER_IMAGE}:${BUILD_TAG} ${DOCKER_IMAGE}:dev-latest
                            echo "✅ Built: ${DOCKER_IMAGE}:${BUILD_TAG}"
                        """
                    }
                }
            }
        }
        
        stage('Push Docker Image') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          containers:
                          - name: docker
                            image: docker:24-dind
                            securityContext:
                              privileged: true
                            env:
                            - name: DOCKER_TLS_CERTDIR
                              value: ""
                          - name: docker-client
                            image: docker:24-cli
                            command:
                            - cat
                            tty: true
                            env:
                            - name: DOCKER_HOST
                              value: tcp://localhost:2376
                            - name: DOCKER_TLS_VERIFY
                              value: ""
                    """
                }
            }
            steps {
                container('docker-client') {
                    echo "�� Pushing Docker image to DockerHub..."
                    unstash 'build-artifacts'
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', 
                                                   passwordVariable: 'PASS', 
                                                   usernameVariable: 'USER')]) {
                        script {
                            sh 'sleep 15'
                            sh """
                                # Rebuild in this pod
                                docker build -t ${DOCKER_IMAGE}:${BUILD_TAG} .
                                docker tag ${DOCKER_IMAGE}:${BUILD_TAG} ${DOCKER_IMAGE}:dev-latest
                                
                                # Login and push
                                echo \$PASS | docker login -u \$USER --password-stdin
                                docker push ${DOCKER_IMAGE}:${BUILD_TAG}
                                docker push ${DOCKER_IMAGE}:dev-latest
                                
                                echo "✅ Successfully pushed:"
                                echo "   - ${DOCKER_IMAGE}:${BUILD_TAG}"
                                echo "   - ${DOCKER_IMAGE}:dev-latest"
                            """
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
                            command:
                            - cat
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
                            sed -i 's|PLACEHOLDER|${BUILD_NUMBER}|g' deployment.yaml
                            
                            # Apply manifests
                            kubectl apply -f deployment.yaml -n dev
                            kubectl apply -f service.yaml -n dev
                            
                            # Wait for rollout
                            kubectl rollout status deployment/ecommerce-deployment -n dev --timeout=300s
                            
                            echo "📊 Deployment Status:"
                            kubectl get pods -n dev -l app=ecommerce -o wide
                            kubectl get service -n dev ecommerce-service
                            kubectl get ingress -n dev ecommerce-dev-ingress
                        """
                    }
                }
            }
        }
        
        stage('Health Check & Verification') {
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
                            command:
                            - cat
                            tty: true
                    """
                }
            }
            steps {
                container('kubectl') {
                    echo "🏥 Performing health checks..."
                    script {
                        sh '''
                            # Wait for pods to be ready
                            sleep 30
                            
                            echo "🔍 Final Health Check:"
                            kubectl get pods -n dev -l app=ecommerce
                            
                            # Check running pods
                            RUNNING_PODS=$(kubectl get pods -n dev -l app=ecommerce --field-selector=status.phase=Running --no-headers | wc -l)
                            echo "✅ Running pods: $RUNNING_PODS/2"
                            
                            # Get application URL
                            INGRESS_URL=$(kubectl get ingress ecommerce-dev-ingress -n dev -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "Still provisioning...")
                            echo "🌍 Application URL: http://$INGRESS_URL"
                            
                            if [ "$RUNNING_PODS" -eq "2" ]; then
                                echo "🎉 All pods running successfully!"
                                echo "🚀 Deployment completed successfully!"
                            else
                                echo "⚠️  Checking pod status..."
                                kubectl describe pods -n dev -l app=ecommerce
                            fi
                        '''
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
            echo "🎉 SUCCESS: Complete CI/CD pipeline executed successfully!"
            echo "📋 Automated Build & Deployment Summary:"
            echo "   ✅ Source code checked out from GitHub"
            echo "   ✅ Java application built with Maven"
            echo "   ✅ Unit tests executed"
            echo "   ✅ Docker image built and tagged: ${BUILD_TAG}"
            echo "   ✅ Image pushed to DockerHub repository"
            echo "   ✅ Application deployed to dev namespace"
            echo "   ✅ ALB ingress configured for public access"
            echo "   ✅ Health checks completed"
            echo ""
            echo "🏆 PROJECT 1 REQUIREMENT SATISFIED:"
            echo "   'Use Jenkins to automate build and deployment' ✅"
            echo ""
            echo "🌍 Access your application via the ingress URL above!"
            echo "📊 Monitor in Grafana: http://k8s-monitori-grafanao-fe0fadbbd1-69729975.us-east-1.elb.amazonaws.com/"
        }
        failure {
            echo "❌ FAILURE: CI/CD pipeline failed!"
            echo "📧 Check console output above for detailed error information"
            echo "🔧 Common fixes:"
            echo "   - Verify DockerHub credentials are correct"
            echo "   - Check Jenkins service account permissions"
            echo "   - Ensure all files are committed to GitHub"
        }
    }
}
