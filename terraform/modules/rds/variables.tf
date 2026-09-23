variable "app_name" {
  type        = string
  description = "Application name"
}

variable "environment" {
  type        = string
  description = "Target deployment environment"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnets for RDS placement"
}

variable "rds_sg_id" {
  type        = string
  description = "Security Group ID for RDS"
}

variable "db_name" {
  type        = string
  description = "PostgreSQL Database Name"
  default     = "frauddetection"
}

variable "db_username" {
  type        = string
  description = "PostgreSQL Master Username"
  default     = "postgres"
}

variable "db_password" {
  type        = string
  description = "PostgreSQL Master Password"
  sensitive   = true
}

variable "instance_class" {
  type        = string
  description = "RDS instance class"
  default     = "db.t4g.micro"
}

variable "multi_az" {
  type        = bool
  description = "Enable Multi-AZ failover deployment"
  default     = false
}
