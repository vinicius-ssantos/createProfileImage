# Infrastructure as Code for create_ia_profiles

This directory contains Terraform configuration for deploying the create_ia_profiles application to AWS.

## Architecture

The infrastructure consists of:

- **AWS ECS Fargate** for running the containerized application
- **Application Load Balancer** for routing traffic to the application
- **AWS Secrets Manager** for securely storing the OpenAI API key
- **CloudWatch Logs** for centralized logging
- **Auto Scaling** based on CPU and memory utilization
- **IAM Roles and Policies** for secure access to AWS resources

## Prerequisites

- [Terraform](https://www.terraform.io/downloads.html) v1.0.0 or newer
- AWS CLI configured with appropriate credentials
- Docker for building and pushing the application image
- An ECR repository for storing the application image
- An ACM certificate for HTTPS

## Usage

### 1. Initialize Terraform

```bash
cd terraform
terraform init
```

### 2. Create a terraform.tfvars file

Create a `terraform.tfvars` file with your specific configuration:

```hcl
# Required variables
vpc_id             = "vpc-0123456789abcdef0"
public_subnet_ids  = ["subnet-0123456789abcdef0", "subnet-0123456789abcdef1"]
private_subnet_ids = ["subnet-0123456789abcdef2", "subnet-0123456789abcdef3"]
ecr_repository_url = "123456789012.dkr.ecr.us-east-1.amazonaws.com/create-ia-profiles"
certificate_arn    = "arn:aws:acm:us-east-1:123456789012:certificate/abcdef01-2345-6789-abcd-ef0123456789"

# Optional variables
environment        = "prod"
aws_region         = "us-east-1"
app_version        = "1.0.0"
task_cpu           = 1024
task_memory        = 2048
service_desired_count = 2
```

### 3. Plan the deployment

```bash
terraform plan -out=tfplan
```

Review the plan to ensure it will create the expected resources.

### 4. Apply the configuration

```bash
terraform apply tfplan
```

### 5. Store the OpenAI API key in Secrets Manager

After the infrastructure is deployed, you need to store your OpenAI API key in the created Secrets Manager secret:

```bash
aws secretsmanager put-secret-value \
  --secret-id "create-ia-profiles/prod/openai-api-key" \
  --secret-string "your-openai-api-key"
```

### 6. Access the application

After deployment, the application will be available at the URL provided in the outputs:

```bash
terraform output app_url
```

## Environments

The configuration supports multiple environments (dev, test, prod) through the `environment` variable. You can create separate `.tfvars` files for each environment:

- `terraform.tfvars.dev`
- `terraform.tfvars.test`
- `terraform.tfvars.prod`

And apply them using:

```bash
terraform apply -var-file=terraform.tfvars.prod
```

## State Management

For production use, it's recommended to use remote state storage. Uncomment and configure one of the backend configurations in `providers.tf`:

- **Terraform Cloud**: For team collaboration and managed state
- **S3 + DynamoDB**: For self-managed remote state

## Cleanup

To destroy the infrastructure when no longer needed:

```bash
terraform destroy
```

**Note**: This will remove all resources created by Terraform, including the ECS service, ALB, and CloudWatch logs. Make sure to backup any important data before running this command.

## Security Considerations

- The OpenAI API key is stored in AWS Secrets Manager and accessed securely by the application
- The application runs in private subnets with no direct internet access
- All traffic to the application is routed through the ALB with HTTPS
- IAM roles follow the principle of least privilege

## Customization

You can customize the deployment by modifying the variables in `terraform.tfvars` or by editing the Terraform configuration files directly.

Common customizations:

- Adjust CPU and memory allocations in `terraform.tfvars`
- Modify auto-scaling settings in `main.tf`
- Add additional environment variables in the container definition in `main.tf`