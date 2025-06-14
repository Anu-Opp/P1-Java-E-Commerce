# Jenkins Configuration & Monitoring Complete Setup

## 🎯 Phase 1: Complete Jenkins Configuration

### Step 1: Access and Initial Setup
```bash
# Your Jenkins is accessible at:
http://k8s-jenkins-jenkinsi-af489020e9-1330725483.us-east-1.elb.amazonaws.com/

# Login credentials you already have:
Username: admin
Password: admin123
```

### Step 2: Install Required Jenkins Plugins
Navigate to **Manage Jenkins > Manage Plugins > Available** and install:

**Essential Plugins:**
- Docker Pipeline
- Kubernetes Plugin
- AWS Steps Plugin
- Pipeline: AWS Steps
- GitHub Integration Plugin
- Prometheus metrics plugin
- Blue Ocean (for better pipeline visualization)

**Installation Commands:**
1. Go to Jenkins Dashboard
2. Manage Jenkins → Manage Plugins
3. Click "Available" tab
4. Search and select each plugin
5. Click "Install without restart"

### Step 3: Configure Jenkins Global Tools
**Manage Jenkins > Global Tool Configuration:**

**Maven Configuration:**
- Name: `Maven-3.8`
- Install automatically: ✅
- Version: 3.8.6

**Docker Configuration:**
- Name: `Docker`
- Install automatically: ✅

**kubectl Configuration:**
- Name: `kubectl`
- Install automatically: ✅

### Step 4: Add Jenkins Credentials
**Manage Jenkins > Manage Credentials > System > Global credentials:**

**AWS Credentials:**
```
Kind: AWS Credentials
ID: aws-credentials
Description: AWS ECR Access
Access Key ID: [Your AWS Access Key]
Secret Access Key: [Your AWS Secret Key]
```

**GitHub Credentials:**
```
Kind: Username with password
ID: github-credentials
Description: GitHub Access
Username: [Your GitHub username]
Password: [Your GitHub personal access token]
```

**Kubeconfig Credential:**
```bash
# First, get your kubeconfig
kubectl config view --raw > /tmp/kubeconfig

# Then in Jenkins:
Kind: Secret file
ID: kubeconfig
Description: EKS Cluster Access
File: Upload the kubeconfig file
```

## 🎯 Phase 2: Create Complete CI/CD Pipeline

### Step 5: Prepare Your Application Repository
```bash
# Create a new directory for your enhanced Java app
mkdir ceeyit-ecommerce-cicd
cd ceeyit-ecommerce-cicd

# Initialize Git repository
git init

# Create enhanced Java application structure
mkdir -p src/main/java/com/cyat/ecommerce
mkdir -p src/main/resources
mkdir -p k8s

# Create enhanced AppController.java
cat <<EOF > src/main/java/com/cyat/ecommerce/AppController.java
package com.cyat.ecommerce;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class AppController {
    
    @GetMapping("/")
    public String home() {
        return "<html><head><style>body { font-family: Arial; background: #f4f4f9; color: #333; text-align: center; padding: 50px; }</style></head>" +
               "<body><h1>CEEYIT E-Commerce Backend</h1>" +
               "<p>Build: " + System.getenv().getOrDefault("BUILD_NUMBER", "local") + "</p>" +
               "<p>Deployed: " + LocalDateTime.now() + "</p>" +
               "<p>Status: Running with Jenkins CI/CD</p></body></html>";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    @GetMapping("/info")
    public String info() {
        return "CEEYIT E-Commerce API v1.0 - Build: " + System.getenv().getOrDefault("BUILD_NUMBER", "local");
    }
}
EOF

# Create Main.java
cat <<EOF > src/main/java/com/cyat/ecommerce/Main.java
package com.cyat.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
EOF

# Create enhanced pom.xml
cat <<EOF > pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.cyat</groupId>
    <artifactId>ecommerce</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <name>CEEYIT E-Commerce</name>
    <description>E-Commerce Backend with Jenkins CI/CD</description>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
        <relativePath/>
    </parent>
    
    <properties>
        <java.version>11</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# Create Dockerfile
cat <<EOF > Dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

# Create Kubernetes deployment
cat <<EOF > k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ecommerce-deployment
  labels:
    app: ecommerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: ecommerce
  template:
    metadata:
      labels:
        app: ecommerce
    spec:
      containers:
      - name: ecommerce
        image: \${ECR_REPOSITORY}:\${IMAGE_TAG}
        ports:
        - containerPort: 8080
        env:
        - name: BUILD_NUMBER
          value: "\${BUILD_NUMBER}"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ecommerce-service
spec:
  selector:
    app: ecommerce
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: ClusterIP
EOF
```

### Step 6: Create Advanced Jenkinsfile
```groovy
cat <<EOF > Jenkinsfile
pipeline {
    agent any
    
    environment {
        AWS_DEFAULT_REGION = 'us-east-1'
        ECR_REPOSITORY = '${ECR_REPOSITORY_URI}' // Replace with your ECR URI
        EKS_CLUSTER_NAME = '${EKS_CLUSTER_NAME}' // Replace with your cluster name
        IMAGE_TAG = "v1.0.\${BUILD_NUMBER}"
        APP_NAME = 'ecommerce'
    }
    
    tools {
        maven 'Maven-3.8'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        
        stage('Build Application') {
            steps {
                echo 'Building Maven application...'
                sh 'mvn clean compile package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Run Tests') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                script {
                    sh "docker build -t \${ECR_REPOSITORY}:\${IMAGE_TAG} ."
                    sh "docker tag \${ECR_REPOSITORY}:\${IMAGE_TAG} \${ECR_REPOSITORY}:latest"
                }
            }
        }
        
        stage('Push to ECR') {
            steps {
                echo 'Pushing to Amazon ECR...'
                withAWS(credentials: 'aws-credentials', region: env.AWS_DEFAULT_REGION) {
                    script {
                        sh "aws ecr get-login-password --region \${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin \${ECR_REPOSITORY}"
                        sh "docker push \${ECR_REPOSITORY}:\${IMAGE_TAG}"
                        sh "docker push \${ECR_REPOSITORY}:latest"
                    }
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to EKS cluster...'
                withKubeConfig([credentialsId: 'kubeconfig']) {
                    script {
                        // Update deployment with new image
                        sh """
                            envsubst < k8s/deployment.yaml | kubectl apply -f -
                            kubectl rollout status deployment/ecommerce-deployment --timeout=300s
                            kubectl get pods -l app=ecommerce
                        """
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'Performing health check...'
                script {
                    sleep(30) // Wait for deployment to stabilize
                    sh 'kubectl get service ecommerce-service'
                    echo 'Deployment successful!'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            sh 'docker system prune -f'
        }
        success {
            echo '✅ Pipeline completed successfully!'
            emailext (
                subject: "✅ Jenkins Build Success - \${JOB_NAME} #\${BUILD_NUMBER}",
                body: "Build completed successfully. Image: \${ECR_REPOSITORY}:\${IMAGE_TAG}",
                to: "your-email@example.com"
            )
        }
        failure {
            echo '❌ Pipeline failed!'
            emailext (
                subject: "❌ Jenkins Build Failed - \${JOB_NAME} #\${BUILD_NUMBER}",
                body: "Build failed. Please check the console output.",
                to: "your-email@example.com"
            )
        }
    }
}
EOF
```

### Step 7: Create Jenkins Pipeline Job
```bash
# Push your code to GitHub first
git add .
git commit -m "Initial Jenkins CI/CD setup"
git remote add origin https://github.com/yourusername/ceeyit-ecommerce-cicd.git
git push -u origin main
```

**In Jenkins Dashboard:**
1. **New Item** → Enter name: `ceeyit-ecommerce-pipeline`
2. Select **Pipeline** → OK
3. **Pipeline Configuration:**
   - **Build Triggers**: ✅ GitHub hook trigger for GITScm polling
   - **Pipeline**:
     - Definition: `Pipeline script from SCM`
     - SCM: `Git`
     - Repository URL: `https://github.com/yourusername/ceeyit-ecommerce-cicd.git`
     - Credentials: `github-credentials`
     - Branch: `*/main`
     - Script Path: `Jenkinsfile`

## 🎯 Phase 3: Enhanced Monitoring with Prometheus & Grafana

### Step 8: Configure Jenkins Metrics for Prometheus
```bash
# Install Prometheus plugin in Jenkins (if not already done)
# Go to Manage Jenkins > Manage Plugins > Available
# Search for "Prometheus metrics plugin" and install

# After installation, Jenkins will expose metrics at:
# http://your-jenkins-url/prometheus/
```

### Step 9: Update Prometheus Configuration
```bash
# Get current Prometheus config
kubectl get configmap prometheus-config -n monitoring -o yaml > prometheus-config.yaml

# Edit the config to include Jenkins metrics
cat <<EOF >> prometheus-additional-config.yaml
- job_name: 'jenkins'
  static_configs:
    - targets: ['jenkins.jenkins.svc.cluster.local:8080']
  metrics_path: '/prometheus'
  scrape_interval: 30s

- job_name: 'ecommerce-app'
  static_configs:
    - targets: ['ecommerce-service.default.svc.cluster.local:80']
  metrics_path: '/actuator/prometheus'
  scrape_interval: 30s
EOF

# Apply the updated configuration
kubectl patch configmap prometheus-config -n monitoring --patch "$(cat prometheus-additional-config.yaml)"

# Restart Prometheus to pick up new config
kubectl rollout restart deployment/prometheus-deployment -n monitoring
```

### Step 10: Create Custom Grafana Dashboards

**Jenkins Dashboard JSON:**
```json
{
  "dashboard": {
    "title": "CEEYIT Jenkins CI/CD Metrics",
    "panels": [
      {
        "title": "Build Success Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(jenkins_builds_success_total[5m]) / rate(jenkins_builds_total[5m]) * 100"
          }
        ]
      },
      {
        "title": "Pipeline Duration",
        "type": "graph",
        "targets": [
          {
            "expr": "jenkins_builds_duration_milliseconds_summary"
          }
        ]
      },
      {
        "title": "Queue Length",
        "type": "graph",
        "targets": [
          {
            "expr": "jenkins_queue_size_value"
          }
        ]
      }
    ]
  }
}
```

### Step 11: Create Application Performance Dashboard
```bash
# Access Grafana
http://k8s-monitori-grafanao-fe0fadbbd1-69729975.us-east-1.elb.amazonaws.com/

# Login: admin / admin123

# Create new dashboard with these panels:
```

**Application Metrics:**
1. **HTTP Request Rate**: `rate(http_requests_total[5m])`
2. **Response Time**: `histogram_quantile(0.95, http_request_duration_seconds_bucket)`
3. **Error Rate**: `rate(http_requests_total{status=~"5.."}[5m])`
4. **Pod CPU Usage**: `rate(container_cpu_usage_seconds_total{pod=~"ecommerce-.*"}[5m])`
5. **Pod Memory Usage**: `container_memory_usage_bytes{pod=~"ecommerce-.*"}`

### Step 12: Set Up Alerting Rules
```yaml
# Create alerting rules for critical metrics
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: alerting-rules
  namespace: monitoring
data:
  alert-rules.yml: |
    groups:
    - name: ceeyit-alerts
      rules:
      - alert: JenkinsBuildFailed
        expr: increase(jenkins_builds_failure_total[5m]) > 0
        for: 0m
        labels:
          severity: warning
        annotations:
          summary: "Jenkins build failed"
          description: "Jenkins build has failed in the last 5 minutes"
      
      - alert: HighCPUUsage
        expr: rate(container_cpu_usage_seconds_total{pod=~"ecommerce-.*"}[5m]) > 0.8
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High CPU usage on ecommerce pods"
          description: "CPU usage is above 80% for more than 2 minutes"
      
      - alert: ApplicationDown
        expr: up{job="ecommerce-app"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "E-commerce application is down"
          description: "The e-commerce application has been down for more than 1 minute"
EOF
```

## 🎯 Phase 4: Testing & Verification

### Step 13: Test Complete Pipeline
```bash
# Make a change to your application
echo "// Updated $(date)" >> src/main/java/com/cyat/ecommerce/AppController.java

# Commit and push
git add .
git commit -m "Test automated pipeline trigger"
git push origin main

# Watch Jenkins build automatically trigger
# Monitor in Jenkins Blue Ocean for better visualization
```

### Step 14: Verify Monitoring
```bash
# Check Prometheus targets
curl http://k8s-monitori-promethe-c32ca382ba-1378643524.us-east-1.elb.amazonaws.com/targets

# Verify Jenkins metrics
curl http://k8s-jenkins-jenkinsi-af489020e9-1330725483.us-east-1.elb.amazonaws.com/prometheus/

# Check application health
kubectl get pods
kubectl logs -l app=ecommerce
```

## 🎉 Success Verification Checklist

### Jenkins Configuration Complete:
- [ ] All required plugins installed
- [ ] Credentials configured (AWS, GitHub, Kubeconfig)
- [ ] Pipeline job created and configured
- [ ] Automatic builds triggered by GitHub webhooks
- [ ] Docker images building and pushing to ECR
- [ ] Kubernetes deployments updating automatically

### Monitoring Complete:
- [ ] Prometheus scraping Jenkins metrics
- [ ] Prometheus scraping application metrics
- [ ] Grafana dashboards showing CI/CD metrics
- [ ] Grafana dashboards showing application performance
- [ ] Alerting rules configured for critical failures
- [ ] Build notifications working

### End-to-End Flow Working:
- [ ] Code change → GitHub push → Jenkins build → ECR push → K8s deployment → Monitoring update

**Your complete DevOps pipeline is now production-ready with full observability!**
