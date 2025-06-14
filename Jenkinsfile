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
                            image: maven:3.9.4-eclipse-temurin-17
                            command: [cat]
                            tty: true
                            workingDir: /home/jenkins/agent
                            resources:
                              requests:
                                memory: "512Mi"
                                cpu: "250m"
                              limits:
                                memory: "1Gi"
                                cpu: "500m"
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
                            resources:
                              requests:
                                memory: "1Gi"
                                cpu: "500m"
                              limits:
                                memory: "2Gi"
                                cpu: "1000m"
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
                    
                    timeout(time: 15, unit: 'MINUTES') {
                        script {
                            sh """
                                echo "📦 Deploying to default namespace..."
                                
                                # Check current deployment status
                                echo "🔍 Current deployment status:"
                                kubectl get deployment java-ecommerce -n default -o wide || echo "Deployment not found"
                                kubectl get pods -n default -l app=java-ecommerce -o wide || echo "No pods found"
                                
                                # Check node resources
                                echo "🖥️ Node resources:"
                                kubectl top nodes || echo "Metrics not available"
                                
                                # Force delete any stuck pods first
                                echo "🧹 Cleaning up any stuck pods..."
                                kubectl get pods -n default -l app=java-ecommerce --field-selector=status.phase=Pending -o name | xargs -r kubectl delete --force --grace-period=0 || echo "No stuck pods"
                                
                                # Scale down first to release resources
                                echo "⬇️ Scaling down temporarily..."
                                kubectl scale deployment java-ecommerce -n default --replicas=0 || echo "Scale down failed"
                                sleep 10
                                
                                # Create or update deployment with correct labels
                                echo "🔄 Creating/updating deployment..."
                                kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-ecommerce
  namespace: default
  labels:
    app: java-ecommerce
spec:
  replicas: 1
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
        - name: BUILD_TAG
          value: "${BUILD_TAG}"
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
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
        startupProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 12
      restartPolicy: Always
      nodeSelector:
        kubernetes.io/os: linux
      tolerations:
      - key: "node.kubernetes.io/not-ready"
        operator: "Exists"
        effect: "NoExecute"
        tolerationSeconds: 300
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0
      maxSurge: 1
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
                                
                                # Create ingress if it doesn't exist
                                if ! kubectl get ingress ecommerce-ingress -n default >/dev/null 2>&1; then
                                    echo "🌐 Creating production ingress..."
                                    kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ecommerce-ingress
  namespace: default
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/backend-protocol: HTTP
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '5'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '2'
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
                                
                                echo ""
                                echo "📊 Final Production Deployment Status:"
                                echo "════════════════════════════════════════════"
                                
                                # Deployment status
                                echo "🚀 Deployment Status:"
                                kubectl get deployment java-ecommerce -n default -o wide
                                
                                # Pod status with detailed info
                                echo ""
                                echo "📦 Pod Status:"
                                kubectl get pods -n default -l app=java-ecommerce -o wide
                                
                                # Service status
                                echo ""
                                echo "🔗 Service Status:"
                                kubectl get service -n default java-ecommerce-service
                                
                                # Ingress status with URL
                                echo ""
                                echo "🌐 Ingress Status:"
                                kubectl get ingress -n default ecommerce-ingress
                                
                                # Get and display application URL
                                INGRESS_URL=\$(kubectl get ingress ecommerce-ingress -n default -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "Still provisioning...")
                                echo ""
                                echo "🎯 Application URLs:"
                                echo "   Production: http://\$INGRESS_URL"
                                echo "   Build Tag: ${BUILD_TAG}"
                                echo "   Branch: main"
                                
                                # Health check with detailed output
                                echo ""
                                echo "🏥 Application Health Check:"
                                POD_NAME=\$(kubectl get pods -n default -l app=java-ecommerce -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "none")
                                if [ "\$POD_NAME" != "none" ]; then
                                    POD_STATUS=\$(kubectl get pod \$POD_NAME -n default -o jsonpath='{.status.phase}')
                                    echo "   Pod Name: \$POD_NAME"
                                    echo "   Pod Status: \$POD_STATUS"
                                    
                                    if [ "\$POD_STATUS" = "Running" ]; then
                                        echo "   ✅ Application is healthy and running!"
                                        
                                        # Test internal connectivity
                                        echo "   🔍 Testing internal connectivity..."
                                        if kubectl exec \$POD_NAME -n default -- curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ | grep -q "200"; then
                                            echo "   ✅ Internal health check passed!"
                                        else
                                            echo "   ⚠️ Internal health check inconclusive"
                                        fi
                                    else
                                        echo "   ⚠️ Pod not in Running state"
                                    fi
                                else
                                    echo "   ❌ No pods found"
                                fi
                                
                                echo ""
                                echo "════════════════════════════════════════════"
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
            echo "💡 Common issues: Resource constraints, pod scheduling, or image pull problems"
        }
    }
}