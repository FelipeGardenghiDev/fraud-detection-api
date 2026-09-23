variable "app_name" {
  type        = string
  description = "Application name"
}

variable "environment" {
  type        = string
  description = "Target deployment environment"
}

variable "aws_region" {
  type        = string
  description = "AWS Region"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID"
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet IDs for ALB placement"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs for ECS tasks"
}

variable "alb_sg_id" {
  type        = string
  description = "Security Group ID of the ALB"
}

variable "ecs_sg_id" {
  type        = string
  description = "Security Group ID of the ECS tasks"
}

variable "ecs_execution_role_arn" {
  type        = string
  description = "IAM Role ARN for ECS execution"
}

variable "ecs_task_role_arn" {
  type        = string
  description = "IAM Role ARN for ECS task"
}

variable "container_image" {
  type        = string
  description = "Docker image URI"
}

variable "container_port" {
  type        = number
  description = "Application port"
  default     = 8080
}

variable "cpu" {
  type        = number
  description = "Fargate CPU units"
  default     = 512
}

variable "memory" {
  type        = number
  description = "Fargate Memory (MB)"
  default     = 1024
}

variable "desired_count" {
  type        = number
  description = "Desired number of ECS task instances"
  default     = 2
}

variable "db_host" {
  type        = string
  description = "RDS DB Host"
}

variable "db_port" {
  type        = number
  description = "RDS DB Port"
  default     = 5432
}

variable "db_name" {
  type        = string
  description = "RDS Database name"
}

variable "db_username" {
  type        = string
  description = "RDS Database user"
}

variable "db_password" {
  type        = string
  description = "RDS Database password"
  sensitive   = true
}

variable "redis_host" {
  type        = string
  description = "ElastiCache Redis Host"
}

variable "redis_port" {
  type        = number
  description = "ElastiCache Redis Port"
  default     = 6379
}

variable "rabbitmq_host" {
  type        = string
  description = "Amazon MQ RabbitMQ Host"
}

variable "rabbitmq_user" {
  type        = string
  description = "RabbitMQ user"
}

variable "rabbitmq_password" {
  type        = string
  description = "RabbitMQ password"
  sensitive   = true
}

variable "api_key" {
  type        = string
  description = "API Key secret for microservice authentication"
  sensitive   = true
}
