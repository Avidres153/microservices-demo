# microservices-demo
Repository to manage a demo of banking microservices

## 📋 Prerequisites
- Java JDK 21
- Maven
- Docker
- PostMan
- Git

## 📋 About the project

This project contains two microservices (customers-management and accounts-management) that use Kafka to communicate with each other and share the information necessary for their proper functioning.

If you need to adjust the ports and database access credentials, you must modify the `docker-compose.yaml` file located in the `/docker` folder.

## 📋 Instructions to start up the environment

- Clone the repository
```bash
git clone https://github.com/Avidres153/microservices-demo.git

cd microservices-demo
```

- Give execution permissions to the start.sh file
```bash
chmod +x start.sh
```

- Execute the script start.sh
```bash
./start.sh
```
## 📋 Instructions for validating endpoints

- Open the Postman app
- Load the shared JSON into the app
