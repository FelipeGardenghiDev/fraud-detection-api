variable "aws_region" {
  type        = string
  description = "AWS deployment region"
  default     = "us-east-1"
}

variable "environment" {
  type        = string
  description = "Environment name (production, staging, dev)"
  default     = "production"
}

variable "app_name" {
  type        = string
  description = "Application name identifier"
  default     = "fraud-detection-api"
}

variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the VPC"
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  type        = list(string)
  description = "CIDR blocks for public subnets"
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  type        = list(string)
  description = "CIDR blocks for private subnets"
  default     = ["10.0.10.0/24", "10.0.20.0/24"]
}

variable "availability_zones" {
  type        = list(string)
  description = "Availability zones to span"
  default     = ["us-east-1a", "us-east-1b"]
}

variable "container_image" {
  type        = string
  description = "Docker image repository and tag"
  default     = "ghcr.io/felipegardenghidev/fraud-detection-api:latest"
}

variable "container_port" {
  type        = number
  description = "Port exposed by the Spring Boot container"
  default     = 8080
}

variable "ecs_desired_count" {
  type        = number
  description = "Number of ECS tasks to maintain"
  default     = 2
}

variable "db_name" {
  type        = string
  description = "Postgres database name"
  default     = "frauddetection"
}

variable "db_username" {
  type        = string
  description = "Postgres master username"
  default     = "postgres"
}

variable "db_password" {
  type        = string
  description = "Postgres master password"
  sensitive   = true
  default     = "ChangeMeInProduction123!"
}

variable "rds_multi_az" {
  type        = bool
  description = "Deploy RDS in Multi-AZ mode for automated failover"
  default     = true
}

variable "rabbitmq_username" {
  type        = string
  description = "Amazon MQ RabbitMQ master user"
  default     = "fraud_admin"
}

variable "rabbitmq_password" {
  type        = string
  description = "Amazon MQ RabbitMQ master password"
  sensitive   = true
  default     = "RabbitMqSecret2026!"
}

variable "api_key" {
  type        = string
  description = "API Key secret for microservice HTTP header authorization (X-API-KEY)"
  sensitive   = true
  default     = "fraud-secret-key-prod-2026"
}
