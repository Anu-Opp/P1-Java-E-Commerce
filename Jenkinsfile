pipeline {
    agent none

    environment {
        DOCKER_IMAGE = "anuopp/java-ecommerce"
        BUILD_TAG = "v2.${env.BUILD_NUMBER}"
        KUBE_NAMESPACE = "dev"
        KUBE_CONTEXT = "your-eks-cluster-name"  // Update with your EKS cluster name
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout Code') {
            agent {
                kubernetes {
                    yaml """
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
  - name: git
    image: alpine/git:latest
    command: ["cat"]
    tty: true
    resources:
      requests:
        cpu: "100m"
        memory: "128Mi"
      limits:
        cpu: "200m"
        memory: "256Mi"
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('git') {
                    echo "🔄 Checking out dev branch..."
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/dev']],
                        extensions: [
                            [$class: 'CleanBeforeCheckout'],
                            [$class: 'CloneOption', depth: 1, shallow: true]
                        ],
                        userRemoteConfigs: [[
                            url: 'https://github.com/Anu-Opp/P1-Java-E-Commerce.git',
                            credentialsId: 'github-credentials'
                        ]]
                    ])
                    stash includes: '**', name: 'source-code', useDefaultExcludes: false
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
  serviceAccountName: jenkins
  containers:
  - name: maven
    image: maven:3.8.6-eclipse-temurin-17
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
    resources:
      requests:
        cpu: "500m"
        memory: "1Gi"
      limits:
        cpu: "1"
        memory: "2Gi"
  volumes:
  - name: maven-cache
    emptyDir: {}
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('maven') {
                    echo "🔨 Building Java application..."
                    unstash 'source-code'
                    sh 'mvn clean package -DskipTests'
                    stash includes: 'target/*.jar,Dockerfile,deployment.yaml,service.yaml,ingress.yaml', name: 'build-artifacts'
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
  serviceAccountName: jenkins
  containers:
  - name: maven
    image: maven:3.8.6-eclipse-temurin-17
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
    resources:
      requests:
        cpu: "500m"
        memory: "1Gi"
      limits:
        cpu: "1"
        memory: "2Gi"
  volumes:
  - name: maven-cache
    emptyDir: {}
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('maven') {
                    echo "🧪 Running unit tests..."
                    unstash 'source-code'
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/surefire-reports/*.xml', allowEmptyArchive: true
                }
            }
        }

        stage('Verify Cluster Access') {
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
    command: ["cat"]
    tty: true
    resources:
      requests:
        cpu: "100m"
        memory: "128Mi"
      limits:
        cpu: "200m"
        memory: "256Mi"
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('kubectl') {
                    echo "🔍 Verifying cluster access..."
                    script {
                        try {
                            sh """
                                kubectl config use-context ${KUBE_CONTEXT}
                                kubectl get nodes
                                kubectl get ns ${KUBE_NAMESPACE} || kubectl create ns ${KUBE_NAMESPACE}
                            """
                        } catch (err) {
                            error("Cluster access verification failed: ${err}")
                        }
                    }
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
  serviceAccountName: jenkins
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command: ["/busybox/cat"]
    tty: true
    volumeMounts:
    - name: kaniko-config
      mountPath: /kaniko/.docker
    - name: docker-config
      mountPath: /kaniko/.docker
    resources:
      requests:
        cpu: "500m"
        memory: "1Gi"
      limits:
        cpu: "1"
        memory: "2Gi"
  volumes:
  - name: kaniko-config
    emptyDir: {}
  - name: docker-config
    emptyDir: {}
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('kaniko') {
                    echo "🐳 Building & pushing Docker image via Kaniko..."
                    unstash 'build-artifacts'
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            mkdir -p /kaniko/.docker
                            echo "{\"auths\":{\"https://index.docker.io/v1/\":{\"username\":\"$DOCKER_USER\",\"password\":\"$DOCKER_PASS\"}}}" > /kaniko/.docker/config.json
                            /kaniko/executor \
                                --dockerfile=Dockerfile \
                                --context="$WORKSPACE" \
                                --destination=${DOCKER_IMAGE}:${BUILD_TAG} \
                                --destination=${DOCKER_IMAGE}:dev-latest \
                                --cache=true \
                                --verbosity=info
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
    command: ["cat"]
    tty: true
    resources:
      requests:
        cpu: "500m"
        memory: "512Mi"
      limits:
        cpu: "1"
        memory: "1Gi"
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('kubectl') {
                    echo "🚀 Deploying to ${KUBE_NAMESPACE} namespace..."
                    unstash 'build-artifacts'
                    script {
                        try {
                            sh """
                                kubectl config use-context ${KUBE_CONTEXT}
                                kubectl config set-context --current --namespace=${KUBE_NAMESPACE}
                                
                                # Update deployment with new image
                                sed -i 's|image: .*|image: ${DOCKER_IMAGE}:${BUILD_TAG}|g' deployment.yaml
                                
                                # Apply Kubernetes manifests
                                kubectl apply -f deployment.yaml -n ${KUBE_NAMESPACE}
                                kubectl apply -f service.yaml -n ${KUBE_NAMESPACE}
                                kubectl apply -f ingress.yaml -n ${KUBE_NAMESPACE} || true
                                
                                # Wait for rollout
                                kubectl rollout status deployment/ecommerce-deployment -n ${KUBE_NAMESPACE} --timeout=300s
                                
                                # Verify deployment
                                kubectl get pods -n ${KUBE_NAMESPACE} -l app=ecommerce
                            """
                        } catch (err) {
                            // Capture diagnostic information
                            sh """
                                kubectl describe deployment ecommerce-deployment -n ${KUBE_NAMESPACE} || true
                                kubectl get events -n ${KUBE_NAMESPACE} --sort-by='.metadata.creationTimestamp' || true
                            """
                            error("Deployment failed: ${err}")
                        }
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
  - name: curl
    image: curlimages/curl:latest
    command: ["cat"]
    tty: true
    resources:
      requests:
        cpu: "100m"
        memory: "128Mi"
      limits:
        cpu: "200m"
        memory: "256Mi"
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('curl') {
                    echo "🏥 Performing health check..."
                    script {
                        def ingressHost = sh(
                            script: """
                                kubectl get ingress ecommerce-dev-ingress -n ${KUBE_NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
                            """,
                            returnStdout: true
                        ).trim()
                        
                        if (ingressHost) {
                            echo "Testing endpoint: http://${ingressHost}/actuator/health"
                            def healthCheck = sh(
                                script: """
                                    curl -s -o /dev/null -w '%{http_code}' http://${ingressHost}/actuator/health || echo "500"
                                """,
                                returnStdout: true
                            ).trim()
                            
                            if (healthCheck != "200") {
                                error("Health check failed with status: ${healthCheck}")
                            } else {
                                echo "✅ Application is healthy!"
                            }
                        } else {
                            echo "⚠️ Ingress host not available yet"
                            sleep(time: 30, unit: 'SECONDS')
                            // Try one more time
                            ingressHost = sh(
                                script: """
                                    kubectl get ingress ecommerce-dev-ingress -n ${KUBE_NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
                                """,
                                returnStdout: true
                            ).trim()
                            if (!ingressHost) {
                                error("Ingress host not available after waiting")
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "🧹 Pipeline finished - Status: ${currentBuild.currentResult}"
            cleanWs()
        }
        success {
            echo "🎉 SUCCESS: Image ${DOCKER_IMAGE}:${BUILD_TAG} pushed and deployed to ${KUBE_NAMESPACE}"
            slackSend(
                color: 'good',
                message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})"
            )
        }
        failure {
            echo "❌ FAILURE: Check above logs for details"
            slackSend(
                color: 'danger',
                message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})"
            )
        }
        unstable {
            echo "⚠️ UNSTABLE: Tests failed but deployment succeeded"
        }
    }
}