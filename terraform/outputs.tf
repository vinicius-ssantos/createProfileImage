# Outputs for create_ia_profiles Terraform configuration

# Application Load Balancer
output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.app_lb.dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = aws_lb.app_lb.zone_id
}

# ECS Cluster
output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.app_cluster.name
}

output "ecs_cluster_arn" {
  description = "ARN of the ECS cluster"
  value       = aws_ecs_cluster.app_cluster.arn
}

# ECS Service
output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = aws_ecs_service.app_service.name
}

# CloudWatch Log Group
output "cloudwatch_log_group" {
  description = "Name of the CloudWatch log group"
  value       = aws_cloudwatch_log_group.app_logs.name
}

# Secrets Manager
output "openai_api_key_secret_arn" {
  description = "ARN of the Secrets Manager secret for OpenAI API key"
  value       = aws_secretsmanager_secret.openai_api_key.arn
}

# Security Groups
output "alb_security_group_id" {
  description = "ID of the ALB security group"
  value       = aws_security_group.alb_sg.id
}

output "app_security_group_id" {
  description = "ID of the application security group"
  value       = aws_security_group.app_sg.id
}

# Application URL
output "app_url" {
  description = "URL to access the application"
  value       = "https://${aws_lb.app_lb.dns_name}/api"
}

# Deployment Information
output "deployment_environment" {
  description = "Deployment environment"
  value       = var.environment
}

output "app_version" {
  description = "Deployed application version"
  value       = var.app_version
}