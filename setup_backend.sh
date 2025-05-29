#!/bin/bash

# Set your actual values here
AWS_REGION="us-east-1"
S3_BUCKET_NAME="ceeyit-terraform-state-bucket"
DYNAMODB_TABLE_NAME="terraform-locks"

# Create S3 Bucket (only if it doesn't exist)
if [ "$AWS_REGION" = "us-east-1" ]; then
  aws s3api create-bucket --bucket $S3_BUCKET_NAME || echo "Bucket may already exist"
else
  aws s3api create-bucket --bucket $S3_BUCKET_NAME --region $AWS_REGION --create-bucket-configuration LocationConstraint=$AWS_REGION || echo "Bucket may already exist"
fi

# Enable versioning on the bucket
aws s3api put-bucket-versioning --bucket $S3_BUCKET_NAME --versioning-configuration Status=Enabled

# Create DynamoDB Table for state locking
aws dynamodb create-table \
  --table-name $DYNAMODB_TABLE_NAME \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST || echo "DynamoDB table may already exist"

echo "✅ S3 and DynamoDB setup completed."
