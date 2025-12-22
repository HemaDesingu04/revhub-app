output "ec2_public_ip" {
  description = "Public IP of the EC2 instance"
  value       = aws_instance.revhub_ec2.public_ip
}

output "ec2_public_dns" {
  description = "Public DNS of the EC2 instance"
  value       = aws_instance.revhub_ec2.public_dns
}

output "ssh_command" {
  description = "SSH command to connect to EC2"
  value       = "ssh -i ~/.ssh/id_rsa ubuntu@${aws_instance.revhub_ec2.public_ip}"
}