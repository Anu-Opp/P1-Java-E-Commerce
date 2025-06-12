pipeline {
    agent none

    environment {
        DOCKER_IMAGE = "anuopp/java-ecommerce"
        BUILD_TAG   = "v2.${env.BUILD_NUMBER}"
        // Optional: expose WORKSPACE_QUOTED to avoid repeated quoting
        WORKSPACE_QUOTED = '"$WORKSPACE"'
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
                // The Git stage may emit JENKINS-30600 warning :contentReference[oaicite:9]{index=9}, but if checkout works, it can be ignored.
                container('git') {
                    echo "🔄 Checking out dev branch..."
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/dev']],
                        userRemoteConfigs: [[
                            url: 'https://github.com/Anu-Opp/P1-Java-E-Commerce.git',
                            credentialsId: 'github-credentials'
                        ]]
                    ])
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
                    // Ensure Dockerfile, manifests are stashed for later stages
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
                    script {
                        if (fileExists('target/surefire-reports/*.xml')) {
                            publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
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
    resources:
      requests:
        cpu: "300m"         # Reduced requests to fit typical node sizes
        memory: "1Gi"
      limits:
        cpu: "600m"
        memory: "1.5Gi"
  volumes:
  - name: kaniko-config
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
                    // Print node resources if scheduling fails, for debugging:
                    sh '''
                      echo "===== Node resources for debugging ====="
                      kubectl get nodes -o wide
                      kubectl describe nodes | grep -A2 "Allocatable"
                      echo "======================================="
                    '''
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                       usernameVariable: 'DOCKER_USER',
                                                       passwordVariable: 'DOCKER_PASS')]) {
                        sh '''
                            mkdir -p /kaniko/.docker
                            cat <<EOF > /kaniko/.docker/config.json
{"auths":{"https://index.docker.io/v1/":{"username":"$DOCKER_USER","password":"$DOCKER_PASS"}}}
EOF
                            # Quote context so spaces in WORKSPACE are handled
                            /kaniko/executor \
                              --dockerfile=Dockerfile \
                              --context="$WORKSPACE" \
                              --destination=${DOCKER_IMAGE}:${BUILD_TAG} \
                              --destination=${DOCKER_IMAGE}:dev-latest \
                              --verbosity=info
                        '''
                    }
                }
            }
        }

        stage('Deploy to Dev Environment') {
            when {
                expression { currentBuild.currentResult == 'SUCCESS' }
            }
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
                    echo "🚀 Deploying to dev namespace..."
                    unstash 'build-artifacts'
                    sh """
                        kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -
                        # Update image in deployment.yaml; ensure sed handles paths correctly
                        sed -i 's|image: .*|image: ${DOCKER_IMAGE}:${BUILD_TAG}|g' deployment.yaml
                        kubectl apply -f deployment.yaml -n dev
                        kubectl apply -f service.yaml -n dev
                        kubectl apply -f ingress.yaml -n dev || true
                        kubectl rollout status deployment/ecommerce-deployment -n dev --timeout=300s
                    """
                }
            }
        }

        stage('Health Check & Verification') {
            when {
                expression { currentBuild.currentResult == 'SUCCESS' }
            }
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
                    echo "🏥 Performing health check..."
                    sh '''
                        sleep 30
                        RUNNING=$(kubectl get pods -n dev -l app=ecommerce --field-selector=status.phase=Running --no-headers | wc -l)
                        echo "Running pods: $RUNNING"
                        INGRESS_HOST=$(kubectl get ingress ecommerce-dev-ingress -n dev -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
                        if [ -n "$INGRESS_HOST" ]; then
                            if ! curl -sf http://$INGRESS_HOST/actuator/health; then
                                echo "Health endpoint failed"; exit 1
                            fi
                            echo "App is healthy at http://$INGRESS_HOST"
                        else
                            echo "Ingress not ready yet"
                        fi
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "🧹 Pipeline finished"
        }
        success {
            echo "🎉 SUCCESS: Image ${DOCKER_IMAGE}:${BUILD_TAG} pushed and deployed"
        }
        failure {
            echo "❌ FAILURE: Check above logs for details"
        }
    }
}
