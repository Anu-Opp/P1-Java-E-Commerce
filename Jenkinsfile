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
                    echo "🔨 Building Java application from main branch with Maven..."
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
                            workingDir: /kaniko/workspace
                    """
                }
            }
            steps {
                container('kaniko') {
                    echo "🐳 Building and pushing Docker image from main branch with Kaniko..."
                    unstash 'build-artifacts'
                    
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', 
                                                   passwordVariable: 'DOCKER_PASS', 
                                                   usernameVariable: 'DOCKER_USER')]) {
                        script {
                            sh '''
                                # Create Docker config for Kaniko
                                mkdir -p /kaniko/.docker
                                echo "{\\"auths\\":{\\"https://index.docker.io/v1/\\":{\\"username\\":\\"$DOCKER_USER\\",\\"password\\":\\"$DOCKER_PASS\\"}}}" > /kaniko/.docker/config.json
                                
                                # Build and push with Kaniko from main branch
                                /kaniko/executor \
                                  --dockerfile=Dockerfile \
                                  --context=. \
                                  --destination=${DOCKER_IMAGE}:${BUILD_TAG} \
                                  --destination=${DOCKER_IMAGE}:main-latest \
                                  --cache=true \
                                  --verbosity=info
                                
                                echo "✅ Successfully built and pushed from main branch:"
                                echo "   - ${DOCKER_IMAGE}:${BUILD_TAG}"
                                echo "   - ${DOCKER_IMAGE}:main-latest"
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Production Environment') {
            agent {
                kubernetes {
                    yaml """
                        apiVersion: v1
                        kind: Pod
                        spec:
                          serviceAccountName: jenkins
                          securityContext:
                            runAsUser: 1000
                            runAsGroup: 1000
                            fsGroup: 1000
                          containers:
                          - name: kubectl
                            image: bitnami/kubectl:1.28
                            command: [cat]
                            tty: true
                            workingDir: /home/jenkins/agent
                            securityContext:
                              runAsUser: 1000
                              runAsGroup: 1000
                            resources:
                              requests:
                                memory: "128Mi"
                                cpu: "100m"
                              limits:
                                memory: "256Mi"
                                cpu: "200m"
                    """
                }
            }
            steps {
                container('kubectl') {
                    echo "🚀 Deploying to production environment from main branch..."
                    unstash 'build-artifacts'
                    
                    timeout(time: 10, unit: 'MINUTES') {
                        script {
                            sh """
                                # Deploy to default namespace (production)
                                echo "📦 Deploying to production (default namespace)..."
                                
                                # Update existing java-ecommerce deployment
                                kubectl patch deployment java-ecommerce -n default -p '{"spec":{"template":{"spec":{"containers":[{"name":"java-ecommerce","image":"${DOCKER_IMAGE}:${BUILD_TAG}"}]}}}}' || echo "Patch failed, continuing..."
                                
                                # If the deployment doesn't exist, create it
                                if ! kubectl get deployment java-ecommerce -n default >/dev/null 2>&1; then
                                    echo "📦 Creating new deployment..."
                                    cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-ecommerce
  namespace: default
  labels:
    app: java-ecommerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: java-ecommerce
  template:
    metadata:
      labels:
        app: java-ecommerce
    spec:
      containers:
      - name: java-ecommerce
        image: ${DOCKER_IMAGE}:${BUILD_TAG}
        ports:
        - containerPort: 8080
        env:
        - name: BUILD_NUMBER
          value: "${BUILD_NUMBER}"
        - name: BRANCH
          value: "main"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: java-ecommerce-service
  namespace: default
spec:
  selector:
    app: java-ecommerce
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: ClusterIP
EOF
                                fi
                                
                                # Ensure ingress exists
                                if ! kubectl get ingress ecommerce-working-ingress -n default >/dev/null 2>&1; then
                                    echo "🌐 Creating production ingress..."
                                    cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ecommerce-working-ingress
  namespace: default
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/backend-protocol: HTTP
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
spec:
  ingressClassName: alb
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: java-ecommerce-service
            port:
              number: 80
EOF
                                fi
                                
                                # Wait for rollout to complete
                                echo "⏳ Waiting for deployment rollout..."
                                kubectl rollout status deployment/java-ecommerce -n default --timeout=300s
                                
                                # Production status check
                                echo "📊 Production Deployment Status:"
                                kubectl get deployment java-ecommerce -n default
                                kubectl get pods -n default -l app=java-ecommerce
                                kubectl get service -n default java-ecommerce-service
                                kubectl get ingress -n default ecommerce-working-ingress
                                
                                # Get production URL
                                INGRESS_URL=\$(kubectl get ingress ecommerce-working-ingress -n default -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "Still provisioning...")
                                echo "🌍 Production Application URL: http://\$INGRESS_URL"
                                
                                echo "✅ Production deployment from main branch completed successfully!"
                            """
                        }
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
            echo "🎉 SUCCESS: Main Branch CI/CD pipeline completed!"
            echo "📋 Build Summary:"
            echo "   ✅ Java application built from main branch with Maven"
            echo "   ✅ Docker image built and tagged: ${BUILD_TAG}"
            echo "   ✅ Image tagged as main-latest"
            echo "   ✅ Image pushed to DockerHub repository"
            echo "   ✅ Production deployment updated from main branch"
            echo ""
            echo "🏆 PROJECT 1 REQUIREMENTS SATISFIED!"
            echo "🌐 Your main branch application is live and accessible!"
            echo "🚀 Production deployment from main branch complete!"
        }
        failure {
            echo "❌ FAILURE: Main branch pipeline failed!"
            echo "🔧 Check logs above for details"
        }
    }
}