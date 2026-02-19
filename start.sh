#!/bin/bash

################# Build java projects #################
echo "-------> Building projects"
CURRENT_DIR="$(pwd)"
echo "Current dir: $CURRENT_DIR"

CUSTOMERS_PROJECT_DIR="$CURRENT_DIR/customers-management"
echo "Dir to build customer microservice: $CUSTOMERS_PROJECT_DIR"

ACCOUNTS_PROJECT_DIR="$CURRENT_DIR/accounts-management"
echo "Dir to build customer microservice: $ACCOUNTS_PROJECT_DIR"

echo "Building customer microservice..."
cd $CUSTOMERS_PROJECT_DIR
./mvnw clean package

if [ $? -eq 0 ]; then
    echo "Customer Microservice compilation were successful!!!"
else
    echo "Error to build microservices."
    exit 1
fi

echo "Building accounts microservice..."
cd $ACCOUNTS_PROJECT_DIR
./mvnw clean package

if [ $? -eq 0 ]; then
    echo "Accounts Microservice compilation were successful!!!"
else
    echo "Error to build microservices."
    exit 1
fi

echo "-------> Copying compiled artifacts to docker directory"
DOCKER_DIR="$CURRENT_DIR/docker"

mkdir -p "$DOCKER_DIR/tmp"

echo "-------> Copying customer artifact"
cp "$CUSTOMERS_PROJECT_DIR/target/"*.jar "$DOCKER_DIR/tmp"

echo "-------> Copying account artifact"
cp "$ACCOUNTS_PROJECT_DIR/target/"*.jar "$DOCKER_DIR/tmp"

################# Build docker images #################
echo "-------> Building microservices docker images"

cd $DOCKER_DIR


echo "-------> Building customers microservice image"

docker build --build-arg APP_NAME=customers-management-1.0.0-SNAPSHOT.jar -t customers-microservice .

echo "-------> Building accounts microservice image"

docker build --build-arg APP_NAME=accounts-management-1.0.0-SNAPSHOT.jar -t accounts-microservice .

################# Starting docker containers #################

echo "-------> Stating docker containers"

docker compose up -d --build

if [ $? -eq 0 ]; then
    echo "-------> Apps are ready to use!!!"
else
    echo "Error to get up Docker compose"
fi