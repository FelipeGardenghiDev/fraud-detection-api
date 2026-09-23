output "alb_dns_name" {
  value       = module.ecs.alb_dns_name
  description = "Public DNS URL of the Application Load Balancer"
}

output "ecs_cluster_name" {
  value       = module.ecs.ecs_cluster_name
  description = "Name of the ECS Cluster"
}

output "ecs_service_name" {
  value       = module.ecs.ecs_service_name
  description = "Name of the ECS Service"
}

output "rds_endpoint" {
  value       = module.rds.endpoint
  description = "Address of the RDS PostgreSQL instance"
}

output "redis_endpoint" {
  value       = module.redis.endpoint
  description = "Primary endpoint of the ElastiCache Redis cluster"
}

output "rabbitmq_endpoint" {
  value       = module.rabbitmq.endpoint
  description = "Endpoint of the Amazon MQ RabbitMQ broker"
}
