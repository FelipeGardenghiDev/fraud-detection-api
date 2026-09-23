output "endpoint" {
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
  description = "Primary Redis Endpoint Address"
}

output "port" {
  value       = aws_elasticache_replication_group.redis.port
  description = "Redis Port"
}
