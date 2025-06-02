terraform {
  backend "s3" {
    bucket         = "ceeyit-tf-state-anu2025"
    key            = "ecommerce/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "ceeyit-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["us-east-1a", "us-east-1b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.10.0/24", "10.0.11.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true

  tags = {
    Project     = "ecommerce"
    Environment = "dev"
  }
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "18.31.2"

  cluster_name    = "ceeyit-ecommerce-cluster"
  cluster_version = "1.27"

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  cluster_endpoint_public_access = true
  enable_irsa                    = true

  eks_managed_node_groups = {
    ecommerce_nodes = {
      desired_size   = 4
      max_size       = 5
      min_size       = 1
      instance_types = ["t2.micro"]

      update_config = {
        max_unavailable_percentage = 50
      }

      tags = {
        Name        = "ecommerce_nodes"
        Environment = "dev"
        Project     = "ecommerce"
      }
    }
  }

  aws_auth_users = [
    {
      userarn  = "arn:aws:iam::911337540350:user/terraform-user"
      username = "terraform-user"
      groups   = ["system:masters"]
    }
  ]

  tags = {
    Environment = "dev"
    Project     = "ecommerce"
  }

  cluster_addons = {}
}

data "aws_eks_cluster" "cluster" {
  name = module.eks.cluster_name
}

data "aws_eks_cluster_auth" "cluster" {
  name = module.eks.cluster_name
}

provider "kubernetes" {
  host                   = data.aws_eks_cluster.cluster.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.cluster.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.cluster.token
}
