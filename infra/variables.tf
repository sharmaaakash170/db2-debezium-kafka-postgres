variable "project_id" { 
    type = string 
    default = "cra-lz-dev-v10"
}
variable "region"     { 
    type = string 
    default = "asia-south1" 
}
variable "cluster_name" { 
    type = string
    default = "db2-debezium-dev" 
}

variable "network"   { 
    type = string
    default = "projects/cra-lz-dev-v10/global/networks/cra-lz-dev-v10-vpc" 
}
variable "subnetwork"{ 
    type = string
    default = "projects/cra-lz-dev-v10/regions/asia-south1/subnetworks/dry-run-subnet" 
}

variable "pods_secondary_range"    { 
    type = string
    default = "dry-run-subnet-pods" 
}

variable "services_secondary_range"{ 
    type = string
    default = "dry-run-subnet-services" 
}

variable "node_count" { 
    type = number
    default = 2 
}

variable "machine_type" { 
    type = string
    default = "e2-standard-4"
}
