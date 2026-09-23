terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "fraud-detection-api"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# 1. Networking Layer (VPC, Public & Private Subnets, IGW)
module "vpc" {
  source = "./modules/vpc"

  app_name             = var.app_name
  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.availability_zones
}

# 2. Security Layer (Security Groups, Ingress/Egress Isolation, IAM Roles)
module "security" {
  source = "./modules/security"

  app_name       = var.app_name
  environment    = var.environment
  vpc_id         = module.vpc.vpc_id
  container_port = var.container_port
}

# 3. Relational Persistence Layer (RDS PostgreSQL Multi-AZ)
module "rds" {
  source = "./modules/rds"

  app_name           = var.app_name
  environment        = var.environment
  private_subnet_ids = module.vpc.private_subnet_ids
  rds_sg_id          = module.security.rds_sg_id
  db_name            = var.db_name
  db_username        = var.db_username
  db_password        = var.db_password
  multi_az           = var.rds_multi_az
}

# 4. In-Memory Velocity Tracker Cache (Amazon ElastiCache Redis)
module "redis" {
  source = "./modules/redis"

  app_name           = var.app_name
  environment        = var.environment
  private_subnet_ids = module.vpc.private_subnet_ids
  redis_sg_id        = module.security.redis_sg_id
}

# 5. Asynchronous Messaging Broker (Amazon MQ RabbitMQ)
module "rabbitmq" {
  source = "./modules/rabbitmq"

  app_name           = var.app_name
  environment        = var.environment
  private_subnet_ids = module.vpc.private_subnet_ids
  rabbitmq_sg_id     = module.security.rabbitmq_sg_id
  mq_username        = var.rabbitmq_username
  mq_password        = var.rabbitmq_password
}

# 6. Container Compute Layer (AWS ECS Fargate + ALB)
module "ecs" {
  source = "./modules/ecs"

  app_name               = var.app_name
  environment            = var.environment
  aws_region             = var.aws_region
  vpc_id                 = module.vpc.vpc_id
  public_subnet_ids      = module.vpc.public_subnet_ids
  private_subnet_ids     = module.vpc.private_subnet_ids
  alb_sg_id              = module.security.alb_sg_id
  ecs_sg_id              = module.security.ecs_sg_id
  ecs_execution_role_arn = module.security.ecs_execution_role_arn
  ecs_task_role_arn      = module.security.ecs_task_role_arn
  container_image        = var.container_image
  container_port         = var.container_port
  desired_count          = var.ecs_desired_count
  db_host                = module.rds.endpoint
  db_port                = module.rds.port
  db_name                = module.rds.db_name
  db_username            = var.db_username
  db_password            = var.db_password
  redis_host             = module.redis.endpoint
  redis_port             = module.redis.port
  rabbitmq_host          = module.rabbitmq.endpoint
  rabbitmq_user          = var.rabbitmq_username
  rabbitmq_password      = var.rabbitmq_password
  api_key                = var.api_key
}
