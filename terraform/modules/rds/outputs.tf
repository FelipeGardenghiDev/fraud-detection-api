output "endpoint" {
  value       = aws_db_instance.postgres.address
  description = "RDS PostgreSQL Host address"
}

output "port" {
  value       = aws_db_instance.postgres.port
  description = "RDS PostgreSQL Port"
}

output "db_name" {
  value       = aws_db_instance.postgres.db_name
  description = "Database name"
}
