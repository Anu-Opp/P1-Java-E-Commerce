pipeline {
    agent none  // Don't use default agent
    
    environment {
        DOCKER_IMAGE = "anuopp/java-ecommerce"
        BUILD_TAG = "dev-${env.BUILD_NUMBER}"
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
                    echo "🔨 Building Java application..."
                    sh 'mvn clean package -DskipTests'
                    stash includes: 'target/*.jar', name: 'jar-artifact'
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
                            image: docker:dind
                            securityContext:
                              privileged: true
                            command:
                            - cat
                            tty: true
                          - name: docker-client
                            image: docker:cli
                            command:
                            - cat
                            tty: true
                            env:
                            - name: DOCKER_HOST
                              value: tcp://localhost:2375
                    """
                }
            }
            steps {
                container('docker-client') {
                    echo "🐳 Building Docker image..."
                    unstash 'jar-artifact'
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_TAG} ."
                    sh "docker tag ${DOCKER_IMAGE}:${BUILD_TAG} ${DOCKER_IMAGE}:dev-latest"
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
                            image: docker:dind
                            securityContext:
                              privileged: true
                            command:
                            - cat
                            tty: true
                          - name: docker-client
                            image: docker:cli
                            command:
                            - cat
                            tty: true
                            env:
                            - name: DOCKER_HOST
                              value: tcp://localhost:2375
                    """
                }
            }
            steps {
                container('docker-client') {
                    echo "📤 Pushing Docker image..."
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
        }
        
        stage('Deploy to Kubernetes') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          serviceAccountName: jenkins
                          containers:
                          - name: kubectl
                            image: bitnami/kubectl:latest
                            command:
                            - cat
                            tty: true
                    """
                }
            }
            steps {
                container('kubectl') {
                    echo "🚀 Deploying to dev environment..."
                    sh """
                        # Create dev namespace if it doesn't exist
                        kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -
                        
                        # Update deployment with new image
                        sed -i 's|image: .*|image: ${DOCKER_IMAGE}:${BUILD_TAG}|g' deployment.yaml
                        
                        # Apply to dev namespace
                        kubectl apply -f deployment.yaml -n dev
                        kubectl apply -f service.yaml -n dev
                        
                        # Wait for rollout
                        kubectl rollout status deployment/ecommerce-deployment -n dev --timeout=300s
                        
                        # Show status
                        kubectl get pods -n dev -l app=ecommerce
                    """
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Pipeline completed"
        }
        success {
            echo "✅ Dev pipeline successful!"
        }
        failure {
            echo "❌ Dev pipeline failed!"
        }
    }
}
