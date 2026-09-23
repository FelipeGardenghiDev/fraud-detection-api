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
  description = "Private subnets for RabbitMQ placement"
}

variable "rabbitmq_sg_id" {
  type        = string
  description = "Security Group ID for RabbitMQ"
}

variable "instance_type" {
  type        = string
  description = "Amazon MQ instance type"
  default     = "mq.t3.micro"
}

variable "mq_username" {
  type        = string
  description = "RabbitMQ broker master username"
  default     = "admin"
}

variable "mq_password" {
  type        = string
  description = "RabbitMQ broker master password"
  sensitive   = true
}
