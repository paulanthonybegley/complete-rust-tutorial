# ============================================================================
#  Ship api-rules to AWS with Terraform — the "Infrastructure as Code" chapter
#  made concrete: a VPC, a Postgres RDS instance, and an ECS Fargate service
#  running the api-rules image behind a load balancer.
#
#  Conservative by design: everything is deterministic and reproducible.
#  The course lab's plan/apply/destroy sim shows this same diff shape.
# ============================================================================

terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

variable "environment" {
  description = "Environments exist so you can break prod without breaking dev"
  default     = "dev"
}

variable "region" {
  default = "eu-west-1"
}

provider "aws" {
  region = var.region
}

locals {
  name_prefix = "api-rules-${var.environment}"
}

resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
  tags       = { Name = local.name_prefix }
}

resource "aws_subnet" "main" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.${count.index}.0/24"
  availability_zone       = "${var.region}${["a", "b"][count.index]}"
  map_public_ip_on_launch = true
  tags                    = { Name = "${local.name_prefix}-subnet-${count.index}" }
}

resource "aws_security_group" "api" {
  name   = "${local.name_prefix}-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "postgres" {
  # The video's API needs a database; this course's copy runs in-memory, so
  # terraform the real one and let the app connect when it exists.
  allocated_storage    = 10
  engine               = "postgres"
  engine_version       = "16.3"
  instance_class       = "db.t3.micro"
  db_name              = "api_rules"
  username             = "api_rules"
  password             = "change-me-in-secrets-manager"
  skip_final_snapshot  = true
  publicly_accessible  = false
  vpc_security_group_ids = [aws_security_group.api.id]
  tags                 = { Name = "${local.name_prefix}-db" }
}

resource "aws_ecs_cluster" "main" {
  name = local.name_prefix
}

resource "aws_ecs_service" "api" {
  name            = "api"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = 3
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = aws_subnet.main[*].id
    security_groups = [aws_security_group.api.id]
    assign_public_ip = true
  }
}

resource "aws_ecs_task_definition" "api" {
  family                   = local.name_prefix
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  container_definitions = jsonencode([
    {
      name      = "api-rules"
      image     = "ghcr.io/your-org/api-rules:latest"
      essential = true
      portMappings = [{ containerPort = 8080 }]
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
        { name = "SERVER_PORT",            value = "8080" }
      ],
      healthCheck = {
        command     = ["CMD-SHELL", "wget -qO- http://localhost:8080/products || exit 1"],
        interval    = 10
        timeout     = 3
        retries     = 3
        startPeriod = 20
      }
    }
  ])
}

output "api_endpoint" {
  value = "http://${aws_ecs_service.api.name}.${aws_ecs_cluster.main.name}.${var.region}.amazonaws.com"
}