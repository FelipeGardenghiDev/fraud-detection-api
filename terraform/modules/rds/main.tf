resource "aws_db_subnet_group" "rds" {
  name        = "${var.app_name}-${var.environment}-db-subnet-group"
  subnet_ids  = var.private_subnet_ids
  description = "Subnet group for RDS PostgreSQL in private subnets"

  tags = {
    Name = "${var.app_name}-${var.environment}-db-subnet-group"
  }
}

resource "aws_db_instance" "postgres" {
  identifier             = "${var.app_name}-${var.environment}-db"
  engine                 = "postgres"
  engine_version         = "16.3"
  instance_class         = var.instance_class
  allocated_storage      = 20
  max_allocated_storage  = 100
  storage_type           = "gp3"
  storage_encrypted      = true
  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [var.rds_sg_id]
  skip_final_snapshot    = true
  deletion_protection    = false
  multi_az               = var.multi_az

  tags = {
    Name = "${var.app_name}-${var.environment}-postgres"
  }
}
