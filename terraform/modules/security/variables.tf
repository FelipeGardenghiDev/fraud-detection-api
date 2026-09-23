variable "app_name" {
  type        = string
  description = "Application name"
}

variable "environment" {
  type        = string
  description = "Target deployment environment"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID where security groups will be created"
}

variable "container_port" {
  type        = number
  description = "Application container port"
  default     = 8080
}
