output "broker_id" {
  value       = aws_mq_broker.rabbitmq.id
  description = "The ID of the RabbitMQ broker"
}

output "endpoint" {
  value       = aws_mq_broker.rabbitmq.instances[0].endpoints[0]
  description = "RabbitMQ broker primary endpoint"
}
