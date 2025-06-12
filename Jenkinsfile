pipeline {
    agent none

    environment {
        DOCKER_IMAGE   = "anuopp/java-ecommerce"
        BUILD_TAG      = "v2.${env.BUILD_NUMBER}"
        KUBE_NAMESPACE = "dev"
        // KUBE_CONTEXT not needed for in-cluster kubectl
        MAVEN_OPTS     = "-Duser.home=/root"  // use default home; PVC mounted at /root/.m2
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
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
    securityContext:
      runAsUser: 1000
      fsGroup: 1000
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
      limits:
        cpu: "100m"
        memory: "128Mi"
    volumeMounts:
    - name: workspace
      mountPath: /workspace
  - name: jnlp
    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
    volumeMounts:
    - name: workspace
      mountPath: /home/jenkins/agent
  volumes:
  - name: workspace
    emptyDir: {}
  nodeSelector:
    kubernetes.io/os: "linux"
  restartPolicy: "Never"
"""
                }
            }
            steps {
                container('git') {
                    echo "🔄 Checking out dev branch..."
                    // checkout into 'src' directory
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/dev']],
                        extensions: [
                            [$class: 'CleanBeforeCheckout'],
                            [$class: 'RelativeTargetDirectory', relativeTargetDir: 'src'],
                            [$class: 'CloneOption', depth: 1, shallow: true]
                        ],
                        userRemoteConfigs: [[
                            url: 'https://github.com/Anu-Opp/P1-Java-E-Commerce.git',
                            credentialsId: 'github-credentials'
                        ]]
                    ])
                    dir('src') {
                        stash includes: '**', name: 'source-code', useDefaultExcludes: false
                    }
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
    securityContext:
      runAsUser: 1000
      fsGroup: 1000
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
    - name: workspace
      mountPath: /workspace
    resources:
      requests:
        cpu: "300m"
        memory: "512Mi"
      limits:
        cpu: "500m"
        memory: "1Gi"
  - name: jnlp
    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
    volumeMounts:
    - name: workspace
      mountPath: /home/jenkins/agent
  volumes:
  - name: maven-cache
    persistentVolumeClaim:
      claimName: jenkins-maven-cache
  - name: workspace
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
                    dir('src') {
                        sh 'mvn clean package -DskipTests'
                        stash includes: 'target/*.jar,Dockerfile,deployment.yaml,service.yaml,ingress.yaml', name: 'build-artifacts'
                        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    }
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
    securityContext:
      runAsUser: 1000
      fsGroup: 1000
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
    - name: workspace
      mountPath: /workspace
    resources:
      requests:
        cpu: "300m"
        memory: "512Mi"
      limits:
        cpu: "500m"
        memory: "1Gi"
  - name: jnlp
    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
    volumeMounts:
    - name: workspace
      mountPath: /home/jenkins/agent
  volumes:
  - name: maven-cache
    persistentVolumeClaim:
      claimName: jenkins-maven-cache
  - name: workspace
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
                    dir('src') {
                        sh 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit 'src/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'src/target/surefire-reports/*.xml', allowEmptyArchive: true
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
    securityContext:
      runAsUser: 1000
      fsGroup: 1000
    volumeMounts:
    - name: kaniko-config
      mountPath: /kaniko/.docker
    - name: workspace
      mountPath: /workspace
    resources:
      requests:
        cpu: "300m"
        memory: "800Mi"
      limits:
        cpu: "600m"
        memory: "1.2Gi"
  - name: jnlp
    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
    volumeMounts:
    - name: workspace
      mountPath: /home/jenkins/agent
  volumes:
  - name: kaniko-config
    emptyDir: {}
  - name: workspace
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
                            cat <<EOF > /kaniko/.docker/config.json
{"auths":{"https://index.docker.io/v1/":{"username":"$DOCKER_USER","password":"$DOCKER_PASS"}}}
EOF
                            /kaniko/executor \
                                --dockerfile=Dockerfile \
                                --context="/workspace/src" \
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
    securityContext:
      runAsUser: 1000
      fsGroup: 1000
    volumeMounts:
    - name: workspace
      mountPath: /workspace
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
      limits:
        cpu: "100m"
        memory: "128Mi"
  - name: jnlp
    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
    volumeMounts:
    - name: workspace
      mountPath: /home/jenkins/agent
  volumes:
  - name: workspace
    emptyDir: {}
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
                    dir('src') {
                        script {
                            try {
                                sh """
                                    # In-cluster kubectl uses Pod's serviceAccount, no need use-context
                                    kubectl create namespace ${KUBE_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                                    sed -i 's|image: .*|image: ${DOCKER_IMAGE}:${BUILD_TAG}|g' deployment.yaml
                                    kubectl apply -f deployment.yaml -n ${KUBE_NAMESPACE}
                                    kubectl apply -f service.yaml -n ${KUBE_NAMESPACE}
                                    kubectl apply -f ingress.yaml -n ${KUBE_NAMESPACE} || true
                                    kubectl rollout status deployment/ecommerce-deployment -n ${KUBE_NAMESPACE} --timeout=300s
                                """
                            } catch (err) {
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
    securityContext:
      runAsUser: 1000
      fsGroup: 1000
    volumeMounts:
    - name: workspace
      mountPath: /workspace
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
      limits:
        cpu: "100m"
        memory: "128Mi"
  - name: jnlp
    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
    resources:
      requests:
        cpu: "50m"
        memory: "64Mi"
    volumeMounts:
    - name: workspace
      mountPath: /home/jenkins/agent
  volumes:
  - name: workspace
    emptyDir: {}
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
                            error("Ingress host not available")
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "🧹 Pipeline finished - Status: ${currentBuild.currentResult}"
            cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, cleanWhenUnstable: true)
        }
        success {
            echo "🎉 SUCCESS: Image ${DOCKER_IMAGE}:${BUILD_TAG} pushed and deployed to ${KUBE_NAMESPACE}"
        }
        failure {
            echo "❌ FAILURE: Check above logs for details"
        }
        unstable {
            echo "⚠️ UNSTABLE: Tests failed but deployment succeeded"
        }
    }
}
