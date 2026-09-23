output "alb_sg_id" {
  value       = aws_security_group.alb.id
  description = "Security Group ID of the ALB"
}

output "ecs_sg_id" {
  value       = aws_security_group.ecs.id
  description = "Security Group ID of the ECS tasks"
}

output "rds_sg_id" {
  value       = aws_security_group.rds.id
  description = "Security Group ID of the RDS instance"
}

output "redis_sg_id" {
  value       = aws_security_group.redis.id
  description = "Security Group ID of the Redis cluster"
}

output "rabbitmq_sg_id" {
  value       = aws_security_group.rabbitmq.id
  description = "Security Group ID of the RabbitMQ broker"
}

output "ecs_execution_role_arn" {
  value       = aws_iam_role.ecs_execution_role.arn
  description = "ARN of the ECS Execution IAM Role"
}

output "ecs_task_role_arn" {
  value       = aws_iam_role.ecs_task_role.arn
  description = "ARN of the ECS Task IAM Role"
}
