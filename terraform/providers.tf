# Provider configuration for create_ia_profiles Terraform

terraform {
  required_version = ">= 1.0.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  # Uncomment this block to use Terraform Cloud for state management
  # backend "remote" {
  #   organization = "your-organization"
  #
  #   workspaces {
  #     name = "create-ia-profiles-${var.environment}"
  #   }
  # }
  
  # Uncomment this block to use S3 for state management
  # backend "s3" {
  #   bucket         = "terraform-state-bucket"
  #   key            = "create-ia-profiles/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-locks"
  # }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Environment = var.environment
      Application = var.app_name
      ManagedBy   = "Terraform"
    }
  }
}