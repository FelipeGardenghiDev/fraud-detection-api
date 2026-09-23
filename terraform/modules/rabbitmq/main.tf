resource "aws_mq_broker" "rabbitmq" {
  broker_name        = "${var.app_name}-${var.environment}-rabbitmq"
  engine_type        = "RabbitMQ"
  engine_version     = "3.13"
  host_instance_type = var.instance_type
  deployment_mode    = "SINGLE_INSTANCE"

  subnet_ids          = [var.private_subnet_ids[0]]
  security_groups     = [var.rabbitmq_sg_id]
  publicly_accessible = false

  user {
    username = var.mq_username
    password = var.mq_password
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-rabbitmq"
  }
}
