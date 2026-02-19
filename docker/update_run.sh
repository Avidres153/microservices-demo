#!/bin/bash
echo "Running kafka-storage format..."
kafka-storage format --ignore-formatted -t $(kafka-storage random-uuid) -c /etc/confluent/docker/kafka.properties
