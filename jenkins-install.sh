#!/bin/bash
# Update system
sudo yum update -y

# Install Java 17 (required for Jenkins)
sudo yum install -y java-17-amazon-corretto

# Add Jenkins repository
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key

# Install Jenkins
sudo yum install -y jenkins

# Install Docker
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker jenkins
sudo usermod -a -G docker ec2-user

# Install kubectl
curl -o kubectl https://amazon-eks.s3.us-west-2.amazonaws.com/1.21.2/2021-07-05/bin/linux/amd64/kubectl
chmod +x ./kubectl
sudo mv ./kubectl /usr/local/bin

# Install AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo yum install -y unzip
unzip awscliv2.zip
sudo ./aws/install

# Install Git
sudo yum install -y git

# Start and enable Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Install Maven (for building Java apps)
sudo yum install -y maven

# Set proper permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins

echo "=== Jenkins Installation Complete ==="
echo "Initial admin password:"
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
