variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "ap-south-1"
}

variable "project_name" {
  description = "Prefix for all capstone resources"
  type        = string
  default     = "capstone-prod"
}

variable "key_name" {
  description = "EC2 Key Pair name"
  type        = string
  default     = "Master-key"
}

variable "jenkins_instance_type" {
  description = "Instance type for Jenkins & SonarQube"
  type        = string
  default     = "m7i-flex.large"
}

variable "k8s_instance_type" {
  description = "Instance type for Kubernetes Control Plane and Worker"
  type        = string
  default     = "c7i-flex.large"
}

variable "s3_bucket_name" {
  description = "S3 bucket for Maven build artifacts"
  type        = string
  default     = "shivam-capstone-artifacts-2026"
}

variable "db_username" {
  description = "RDS master username"
  type        = string
  default     = "admin"
}

variable "db_password" {
  description = "RDS master password"
  type        = string
  default     = "DevOpsPass2026!"
  sensitive   = true
}