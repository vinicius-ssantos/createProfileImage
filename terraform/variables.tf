# Variables for create_ia_profiles Terraform configuration

# Application Configuration
variable "app_name" {
  description = "Name of the application"
  type        = string
  default     = "create-ia-profiles"
}

variable "app_version" {
  description = "Version of the application to deploy"
  type        = string
  default     = "latest"
}

variable "environment" {
  description = "Deployment environment (dev, test, prod)"
  type        = string
  default     = "dev"
  
  validation {
    condition     = contains(["dev", "test", "prod"], var.environment)
    error_message = "Environment must be one of: dev, test, prod."
  }
}

# AWS Configuration
variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

# Network Configuration
variable "vpc_id" {
  description = "ID of the VPC to deploy resources"
  type        = string
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs for the ALB"
  type        = list(string)
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs for the ECS tasks"
  type        = list(string)
}

# ECS Configuration
variable "task_cpu" {
  description = "CPU units for the ECS task (1 vCPU = 1024 CPU units)"
  type        = number
  default     = 1024
}

variable "task_memory" {
  description = "Memory for the ECS task in MiB"
  type        = number
  default     = 2048
}

variable "service_desired_count" {
  description = "Desired number of instances of the ECS service"
  type        = number
  default     = 2
}

variable "service_min_count" {
  description = "Minimum number of instances of the ECS service for auto-scaling"
  type        = number
  default     = 1
}

variable "service_max_count" {
  description = "Maximum number of instances of the ECS service for auto-scaling"
  type        = number
  default     = 5
}

# Container Registry
variable "ecr_repository_url" {
  description = "URL of the ECR repository containing the application image"
  type        = string
}

# Application Configuration
variable "openai_base_url" {
  description = "Base URL for OpenAI API"
  type        = string
  default     = "https://api.openai.com/v1/images/generations"
}

# SSL/TLS Configuration
variable "certificate_arn" {
  description = "ARN of the ACM certificate for HTTPS"
  type        = string
}

# Logging Configuration
variable "log_retention_days" {
  description = "Number of days to retain CloudWatch logs"
  type        = number
  default     = 30
}

# Tagging
variable "common_tags" {
  description = "Common tags to apply to all resources"
  type        = map(string)
  default     = {
    Project     = "create-ia-profiles"
    ManagedBy   = "terraform"
  }
}