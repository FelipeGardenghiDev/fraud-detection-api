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
  description = "Private subnets for Redis placement"
}

variable "redis_sg_id" {
  type        = string
  description = "Security Group ID for Redis"
}

variable "node_type" {
  type        = string
  description = "ElastiCache Redis node type"
  default     = "cache.t4g.micro"
}
