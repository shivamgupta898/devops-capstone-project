output "jenkins_public_ip" {
  description = "Fixed Elastic IP for Jenkins Server"
  value       = aws_eip.jenkins_eip.public_ip
}

output "k8s_master_public_ip" {
  description = "Fixed Elastic IP for K8s Master"
  value       = aws_eip.master_eip.public_ip
}

output "k8s_worker_public_ip" {
  description = "Fixed Elastic IP for K8s Worker"
  value       = aws_eip.worker_eip.public_ip
}

output "rds_endpoint" {
  description = "RDS MySQL Hostname"
  value       = aws_db_instance.app_db.endpoint
}

output "s3_bucket_name" {
  description = "Maven Artifacts Bucket Name"
  value       = aws_s3_bucket.artifacts_bucket.id
}