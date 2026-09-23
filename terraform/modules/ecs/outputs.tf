output "alb_dns_name" {
  value       = aws_lb.alb.dns_name
  description = "Public URL of the Application Load Balancer"
}

output "ecs_cluster_name" {
  value       = aws_ecs_cluster.main.name
  description = "Name of the ECS Cluster"
}

output "ecs_service_name" {
  value       = aws_ecs_service.app.name
  description = "Name of the ECS Service"
}
