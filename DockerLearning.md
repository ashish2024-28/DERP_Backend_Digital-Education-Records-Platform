# Docker Complete Guide — DERP Backend

A practical Docker command reference, from basic commands to advanced workflows, using the DERP Spring Boot backend as an example.

## Table of Contents

1. [Docker Basics](#1-docker-basics)
2. [Check Docker Installation](#2-check-docker-installation)
3. [Docker Images](#3-docker-images)
4. [Build the DERP Backend Image](#4-build-the-derp-backend-image)
5. [Run Containers](#5-run-containers)
6. [Container Management](#6-container-management)
7. [Logs and Debugging](#7-logs-and-debugging)
8. [Environment Variables and Secrets](#8-environment-variables-and-secrets)
9. [Docker Hub](#9-docker-hub)
10. [Versioning and Latest Tag](#10-versioning-and-latest-tag)
11. [Networking](#11-networking)
12. [Volumes and Data Persistence](#12-volumes-and-data-persistence)
13. [Inspect and Monitor Containers](#13-inspect-and-monitor-containers)
14. [Update an Existing Container](#14-update-an-existing-container)
15. [Docker Compose](#15-docker-compose)
16. [Cleanup Commands](#16-cleanup-commands)
17. [Advanced Commands](#17-advanced-commands)
18. [Common Errors and Solutions](#18-common-errors-and-solutions)
19. [Complete DERP Deployment Workflow](#19-complete-derp-deployment-workflow)
20. [Docker Best Practices](#20-docker-best-practices)

---

## 1. Docker Basics

Docker packages an application and its runtime dependencies into an image. A container is a running instance of that image.

| Term | Meaning | Example |
|---|---|---|
| Image | Application package/template | `ashish9693/derp-backend:2.8` |
| Container | Running instance of an image | `derp-backend-local` |
| Dockerfile | Instructions to build an image | `Dockerfile` |
| Registry | Stores and distributes images | Docker Hub |
| Volume | Persistent storage managed by Docker | `derp-data` |
| Network | Connects containers | `derp-network` |
| Tag | Identifies an image version | `2.8`, `latest` |

Example:

```powershell
docker run hello-world
```

Docker downloads the `hello-world` image if necessary, runs a container, and prints a test message.

## 2. Check Docker Installation

### Check the version

```powershell
docker --version
docker compose version
```

### Check whether the Docker daemon is running

```powershell
docker info
```

### List running containers

```powershell
docker ps
```

### List all containers

```powershell
docker ps -a
```

### List downloaded images

```powershell
docker images
```

**If Docker is not running:** Open Docker Desktop and wait until the engine is ready.

---

## 3. Docker Images

An image is the package from which containers are created.

### List images

```powershell
docker images
```

### Find a particular image

```powershell
docker images ashish9693/derp-backend
```

### Pull an image from Docker Hub

```powershell
docker pull ashish9693/derp-backend:2.8
```

Pull the `latest` tag:

```powershell
docker pull ashish9693/derp-backend:latest
```

### Remove an image

```powershell
docker rmi ashish9693/derp-backend:2.8
```

The image cannot normally be removed while a container still references it. Stop and remove the relevant container first, if appropriate.

### Inspect image details

```powershell
docker image inspect ashish9693/derp-backend:2.8
```

### View image history

```powershell
docker history ashish9693/derp-backend:2.8
```

### Compare image sizes

```powershell
docker images ashish9693/derp-backend
```

---

## 4. Build the DERP Backend Image

First, open PowerShell in the backend project directory.

```powershell
cd "E:\Coding\Github\Derp_FullStack Project\DERP_Backend_Digital-Education-Records-Platform"
```

Use the actual directory name on your machine if it differs.

### Step 1: Check project files

```powershell
Get-ChildItem
```

Confirm that your project contains a `Dockerfile`, `pom.xml`, and `src` directory.

### Step 2: Build the Spring Boot JAR locally

If your project includes the Maven Wrapper:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Alternatively, if Maven is installed:

```powershell
mvn clean package -DskipTests
```

This creates the application JAR under `target`.

`-DskipTests` skips test execution. Run tests separately before releasing production builds.

### Step 3: Build the Docker image

```powershell
docker build -t ashish9693/derp-backend:2.9 .
```

Here:

- `docker build` builds an image.
- `-t` assigns a name and tag.
- `ashish9693/derp-backend` is the repository.
- `2.9` is the version.
- `.` uses the current directory as the build context.

Replace `2.9` with your next version when appropriate.

### Step 4: Confirm the image exists

```powershell
docker images ashish9693/derp-backend
```

### Build with a different tag

```powershell
docker build -t ashish9693/derp-backend:2.10 .
```

### Build without using the build cache

```powershell
docker build --no-cache -t ashish9693/derp-backend:2.10 .
```

This forces Docker to rerun build steps rather than reuse cached layers. It does not guarantee a newer version of every base image; pull updated base images separately when required.

### Build using a newer base image

```powershell
docker build --pull -t ashish9693/derp-backend:2.10 .
```

### Build and inspect build details

```powershell
docker build --progress=plain -t ashish9693/derp-backend:2.10 .
```

**Important:** Your Dockerfile runs Maven during its build. Environment variables loaded into your PowerShell session do not automatically become environment variables inside the Docker build stage. Pass runtime secrets when starting the container instead.

---

## 5. Run Containers

### Run a simple test container

```powershell
docker run hello-world
```

### Run a container in the foreground

```powershell
docker run --name derp-test ashish9693/derp-backend:2.8
```

The terminal displays its output. Stop it with `Ctrl+C` if it is still running.

### Run DERP in the background

```powershell
docker run -d --name derp-backend-local -p 8080:8080 ashish9693/derp-backend:2.8
```

Options:

- `-d`: run in detached mode.
- `--name`: assign a container name.
- `-p 8080:8080`: map host port 8080 to container port 8080.

The backend should then be reachable at:

```text
http://localhost:8080
```

If your application exposes Swagger at `/derp_docs`, try:

```text
http://localhost:8080/derp_docs
```

This assumes your application uses port 8080 and the route is accessible under your security configuration.

### Run with automatic restart

```powershell
docker run -d --restart unless-stopped --name derp-backend-local -p 8080:8080 ashish9693/derp-backend:2.8
```

Use this only if the container name is not already in use and the required database configuration has been supplied.

---

## 6. Container Management

### Show running containers

```powershell
docker ps
```

### Show all containers, including stopped ones

```powershell
docker ps -a
```

### Stop a container

```powershell
docker stop derp-backend-local
```

### Start a stopped container

```powershell
docker start derp-backend-local
```

### Restart a container

```powershell
docker restart derp-backend-local
```

### Remove a stopped container

```powershell
docker rm derp-backend-local
```

### Force-remove a container

```powershell
docker rm -f derp-backend-local
```

**Warning:** Force-removing a container stops and removes it. Save any required configuration and data first.

### Rename a container

```powershell
docker rename derp-backend-local derp-backend-old
```

### Pause and resume a container

```powershell
docker pause derp-backend-local
docker unpause derp-backend-local
```

### Display container status

```powershell
docker inspect -f '{{.State.Status}}' derp-backend-local
```

---

## 7. Logs and Debugging

### View all container logs

```powershell
docker logs derp-backend-local
```

### Show the latest 100 lines

```powershell
docker logs --tail 100 derp-backend-local
```

### Follow logs in real time

```powershell
docker logs -f derp-backend-local
```

Stop following logs with `Ctrl+C`; this does not normally stop the container.

### Follow only the last 100 lines

```powershell
docker logs --tail 100 -f derp-backend-local
```

### Display timestamps

```powershell
docker logs -t derp-backend-local
```

### Execute a shell inside a container

For a container with Bash:

```powershell
docker exec -it derp-backend-local bash
```

If Bash is unavailable, try:

```powershell
docker exec -it derp-backend-local sh
```

Exit the shell with:

```bash
exit
```

### Check the Java process

```powershell
docker top derp-backend-local
```

### Check published ports

```powershell
docker port derp-backend-local
```

### Common Spring Boot log problems

| Log message | What to investigate |
|---|---|
| Connection refused | Database host, port, network and service status |
| Unable to determine Dialect | Database connection or Hibernate configuration |
| Access denied | Database username and password |
| Address already in use | Port conflicts |
| Application failed to start | The earlier exception in the logs |
| 401 Unauthorized | Authentication credentials or token |
| 403 Forbidden | Authorization rules, security filters or CSRF configuration |

Read the complete exception around the first root-cause message before changing configuration.

---

## 8. Environment Variables and Secrets

**Never bake database passwords, SMTP passwords, OAuth secrets or JWT secrets into a Docker image.**

Keep secrets outside the image and provide them at runtime.

### Step 1: Prepare `.env2`

Example structure only—replace the placeholder values with your actual configuration:

```dotenv
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_DATABASE_HOST:5432/derp
SPRING_DATASOURCE_USERNAME=YOUR_DATABASE_USER
SPRING_DATASOURCE_PASSWORD=YOUR_DATABASE_PASSWORD
```

Add the other environment variables your application actually reads, including any required SMTP and JWT configuration.

Do not copy these example names blindly if your Spring Boot configuration uses different names.

### Step 2: Load environment variables into the container

```powershell
docker run -d `
  --name derp-backend-local `
  --env-file .env2 `
  -p 8080:8080 `
  ashish9693/derp-backend:2.8
```

The backtick at the end of each line is PowerShell's line-continuation character. Do not put spaces after it.

Docker's env-file should contain simple `KEY=value` entries. Avoid inline comments after values, and validate quoting and special characters. Do not commit `.env2` to Git.

### Step 3: Verify that the container is running

```powershell
docker ps
docker logs --tail 100 derp-backend-local
```

### Step 4: Check a non-secret variable

```powershell
docker exec derp-backend-local printenv SPRING_PROFILES_ACTIVE
```

Avoid displaying passwords or secrets in terminal output.

### Important database networking rule

Inside a container, `localhost` refers to that same container, not your Windows host or a separate database container.

- For a database hosted on Render, use its correct external connection details and SSL settings as required.
- For a database container, use the database container's network name on a shared Docker network.
- For a database installed on Windows, `host.docker.internal` may be appropriate, provided the database is listening and allows the connection.

---

## 9. Docker Hub

Docker Hub stores images so you can pull them on another machine or deploy them to a compatible hosting service.

### Step 1: Log in

```powershell
docker login
```

Complete authentication when prompted.

### Step 2: Tag your image

```powershell
docker tag ashish9693/derp-backend:2.8 ashish9693/derp-backend:2.9
```

This assigns the `2.9` tag to the same local image; it does not build new code.

### Step 3: Push a version

```powershell
docker push ashish9693/derp-backend:2.9
```

### Step 4: Pull the version on another machine

```powershell
docker pull ashish9693/derp-backend:2.9
```

### Step 5: Log out when appropriate

```powershell
docker logout
```

Check the repository and tags on Docker Hub:

https://hub.docker.com/r/ashish9693/derp-backend/tags

---

## 10. Versioning and Latest Tag

Use explicit version tags such as `2.8`, `2.9` and `2.10` so you can identify releases.

The `latest` tag is only a tag name. Docker does not automatically make it point to the newest version.

### Assign `latest` to version 2.9

```powershell
docker tag ashish9693/derp-backend:2.9 ashish9693/derp-backend:latest
```

### Push both tags

```powershell
docker push ashish9693/derp-backend:2.9
docker push ashish9693/derp-backend:latest
```

### Verify local tags

```powershell
docker images ashish9693/derp-backend
```

Both tags should reference the same image ID.

### Verify the remote tags

```powershell
docker manifest inspect ashish9693/derp-backend:2.9
docker manifest inspect ashish9693/derp-backend:latest
```

These commands inspect the remote manifests if the tags exist and are accessible.

### Recommended release pattern

For every code release:

1. Build a new version tag.
2. Test the image.
3. Push the version tag.
4. Update the `latest` tag to that same image.
5. Push `latest`.
6. Deploy the exact version tag where reproducibility matters.

Do not tag an old image as a new version unless it really represents the release you intend to publish.

---

## 11. Networking

Docker networks let containers communicate with each other.

### List networks

```powershell
docker network ls
```

### Create a network

```powershell
docker network create derp-network
```

### Connect an existing container to a network

```powershell
docker network connect derp-network derp-backend-local
```

### Inspect a network

```powershell
docker network inspect derp-network
```

### Disconnect a container

```powershell
docker network disconnect derp-network derp-backend-local
```

### Run a container on a specific network

```powershell
docker run -d `
  --name derp-backend-local `
  --network derp-network `
  --env-file .env2 `
  -p 8080:8080 `
  ashish9693/derp-backend:2.9
```

For a database container on the same network, configure the JDBC hostname to the database container name, not `localhost`.

Example:

```text
jdbc:mysql://derp-mysql:3306/derp
```

This example applies only if the database container is actually named `derp-mysql`, MySQL is configured to use port 3306, and both containers share the network.

### Remove an unused network

```powershell
docker network rm derp-network
```

Docker will not remove a network that still has connected containers.

---

## 12. Volumes and Data Persistence

Volumes store data independently of a container's lifecycle.

### List volumes

```powershell
docker volume ls
```

### Create a volume

```powershell
docker volume create derp-data
```

### Inspect a volume

```powershell
docker volume inspect derp-data
```

### Mount a volume into a container

```powershell
docker run -d `
  --name derp-backend-local `
  --mount source=derp-data,target=/app/data `
  ashish9693/derp-backend:2.9
```

Use a mount path that your application actually writes to. `/app/data` is only an example.

### Remove a volume

```powershell
docker volume rm derp-data
```

**Warning:** Removing a volume can permanently delete its stored data. Do not remove a database volume unless you have confirmed you no longer need the data and have appropriate backups.

---

## 13. Inspect and Monitor Containers

### Full container configuration

```powershell
docker inspect derp-backend-local
```

### Show environment-variable names and values

```powershell
docker inspect -f '{{range .Config.Env}}{{println .}}{{end}}' derp-backend-local
```

This may print secrets. Do not share the output publicly.

### Show the image used by a container

```powershell
docker inspect -f '{{.Config.Image}}' derp-backend-local
```

### Show the container's restart policy

```powershell
docker inspect -f '{{.HostConfig.RestartPolicy.Name}}' derp-backend-local
```

### Show resource usage

```powershell
docker stats
```

### Show resource usage for one container

```powershell
docker stats derp-backend-local
```

### Show disk usage

```powershell
docker system df
```

### Show events

```powershell
docker events
```

Stop the event stream with `Ctrl+C`.

---

## 14. Update an Existing Container

Building and pushing an image does not update an already-running container. A container must be recreated with the new image.

### Step 1: Inspect the existing container

```powershell
docker inspect derp-backend-local
```

Check its ports, environment settings, network attachments, mounts and restart policy. Securely save any required configuration before making changes.

### Step 2: Pull the new image

```powershell
docker pull ashish9693/derp-backend:2.9
```

### Step 3: Stop the old container

```powershell
docker stop derp-backend-local
```

### Step 4: Remove the old container

```powershell
docker rm derp-backend-local
```

This removes the container, not the image tag. Data stored only in the container's writable layer may be lost.

### Step 5: Recreate it

For a backend that uses `.env2` and listens on port 8080:

```powershell
docker run -d `
  --name derp-backend-local `
  --restart unless-stopped `
  --env-file .env2 `
  -p 8080:8080 `
  ashish9693/derp-backend:2.9
```

**Before running this example:** restore any additional settings from the old container, including network, volume mounts, port mappings and restart policy. If the backend requires a custom Docker network, add the appropriate `--network` option.

### Step 6: Verify the deployment

```powershell
docker ps
docker logs --tail 100 derp-backend-local
```

Test the application's health endpoint, if your backend exposes one.

Do not delete the old container until you have recorded its configuration and have a rollback plan.

---

## 15. Docker Compose

Docker Compose defines multiple services in a YAML file. It is useful when your application requires a backend, database and other supporting services.

### Example `compose.yaml`

This example defines only the DERP backend. It assumes the image is already published and `.env2` contains the application's runtime variables.

```yaml
services:
  derp-backend:
    image: ashish9693/derp-backend:2.9
    container_name: derp-backend-local
    restart: unless-stopped
    env_file:
      - .env2
    ports:
      - "8080:8080"
```

### Start the service

```powershell
docker compose up -d
```

### Build the image if a build configuration is present

```powershell
docker compose up -d --build
```

The example above specifies only an image, so Compose has no local build configuration for this service.

### List services

```powershell
docker compose ps
```

### View logs

```powershell
docker compose logs
docker compose logs -f derp-backend
```

### Restart the service

```powershell
docker compose restart derp-backend
```

### Pull the configured image

```powershell
docker compose pull
```

### Recreate the container after pulling a newer image

```powershell
docker compose up -d --force-recreate
```

### Stop services

```powershell
docker compose down
```

`docker compose down` removes the Compose containers and network. It does not normally remove named volumes unless requested.

**Security:** Keep `.env2` out of version control. The Compose file and environment configuration must match the variable names expected by your Spring Boot application.

---

## 16. Cleanup Commands

Use cleanup commands carefully, especially on machines with important images, containers and volumes.

### Remove a stopped container

```powershell
docker container prune
```

Docker asks for confirmation before removing stopped containers.

### Remove dangling images

```powershell
docker image prune
```

### Remove unused images

```powershell
docker image prune -a
```

This may remove images you want to keep locally if no container references them.

### Remove unused networks

```powershell
docker network prune
```

### Remove unused volumes

```powershell
docker volume prune
```

Review volume contents and backups before running this command.

### General system cleanup

```powershell
docker system prune
```

### More aggressive system cleanup

```powershell
docker system prune -a
```

This removes additional unused images. It can force you to download or rebuild images again.

Avoid `docker system prune -a --volumes` unless you fully understand the consequences and have confirmed that no required data will be removed.

---

## 17. Advanced Commands

### A. Check a container's exit code

```powershell
docker inspect -f '{{.State.ExitCode}}' derp-backend-local
```

A non-zero exit code commonly indicates an application or container failure. Check the logs to identify the cause.

### B. Check the container's health status

```powershell
docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}No healthcheck configured{{end}}' derp-backend-local
```

A health status is available only if the image or container has a health check configured.

### C. Copy a file from the container

```powershell
docker cp derp-backend-local:/app/example.log .\example.log
```

Replace the path with an actual file path inside the container.

### D. Copy a file into the container

```powershell
docker cp .\example.txt derp-backend-local:/tmp/example.txt
```

Files copied into a container's writable layer are not automatically preserved if the container is removed.

### E. Save an image to a TAR archive

```powershell
docker save -o derp-backend-2.9.tar ashish9693/derp-backend:2.9
```

### F. Load an image from a TAR archive

```powershell
docker load -i derp-backend-2.9.tar
```

This is useful for transferring images between machines without pushing them to a registry.

### G. Export a container filesystem

```powershell
docker export -o derp-container-filesystem.tar derp-backend-local
```

Exporting a container filesystem does not preserve its image history, normal image configuration or mounted volume contents. Use `docker save` when your goal is to transfer an image.

### H. Create a backup of an existing image

```powershell
docker image save -o derp-backend-backup.tar ashish9693/derp-backend:2.9
```

### I. Check which containers use an image

```powershell
docker ps -a --filter ancestor=ashish9693/derp-backend:2.9
```

### J. Build and tag a release

```powershell
docker build -t ashish9693/derp-backend:2.10 .
docker tag ashish9693/derp-backend:2.10 ashish9693/derp-backend:latest
```

Test the new image before publishing it.

### K. Push the release

```powershell
docker push ashish9693/derp-backend:2.10
docker push ashish9693/derp-backend:latest
```

### L. Check image digests

```powershell
docker image inspect -f '{{json .RepoDigests}}' ashish9693/derp-backend:2.10
```

A digest identifies an image manifest or manifest list. Use digests when you need a fixed image reference instead of a mutable tag.

### M. Run with a memory limit

```powershell
docker run -d `
  --name derp-backend-limited `
  --memory=1g `
  --cpus=1.5 `
  -p 8080:8080 `
  --env-file .env2 `
  ashish9693/derp-backend:2.9
```

These are example resource limits, not recommended production sizing. Set limits based on measured application and JVM requirements.

### N. Run a one-off command inside the image

```powershell
docker run --rm ashish9693/derp-backend:2.9 java -version
```

This runs the command and removes the temporary container after it exits. It assumes the image contains a Java runtime and uses a compatible entrypoint configuration.

---

## 18. Common Errors and Solutions

### Error 1: Docker daemon is not running

Check:

```powershell
docker info
```

**Fix:** Start Docker Desktop and wait for the engine to become available.

### Error 2: Container name is already in use

Example:

```text
The container name "/derp-backend-local" is already in use.
```

Check:

```powershell
docker ps -a
```

If it is an old container you no longer need, inspect it and then remove it:

```powershell
docker inspect derp-backend-local
docker stop derp-backend-local
docker rm derp-backend-local
```

Do not remove a container until you have confirmed its configuration and data are no longer required.

### Error 3: Image tag does not exist

Check:

```powershell
docker images ashish9693/derp-backend
```

Tag an existing image:

```powershell
docker tag ashish9693/derp-backend:2.8 ashish9693/derp-backend:latest
```

If `2.8` does not exist locally, build or pull the correct version first.

### Error 4: Docker Hub push is denied

Check:

```powershell
docker login
```

Verify that the repository name is correct and your account has permission to push to it.

Then:

```powershell
docker push ashish9693/derp-backend:2.9
```

### Error 5: The application cannot connect to the database

Check:

```powershell
docker logs --tail 200 derp-backend-local
```

Confirm the database URL, credentials, network, port, firewall rules, and SSL settings. Do not assume that `localhost` points to the database from inside the backend container.

### Error 6: Port 8080 is already allocated

Check:

```powershell
docker ps
```

If another service owns host port 8080, use a different host port:

```powershell
docker run -d --name derp-backend-local -p 8081:8080 --env-file .env2 ashish9693/derp-backend:2.9
```

The application is then available through host port 8081, provided the backend starts successfully.

### Error 7: New code is not appearing in the image

Build again:

```powershell
docker build --no-cache -t ashish9693/derp-backend:2.10 .
```

Verify that your changes are saved in the correct project directory and included in the Docker build context. Inspect the Dockerfile and `.dockerignore` if necessary.

### Error 8: MySQL reports that no root password was supplied

For a new MySQL container, configure the required password through the appropriate environment variable or a protected environment file before initializing it. Do not put passwords directly into commands that may be saved in shell history.

### Error 9: Docker image size is too large

Inspect the image:

```powershell
docker images ashish9693/derp-backend
docker history ashish9693/derp-backend:2.9
```

Use a multi-stage build, a suitable runtime base image, and a `.dockerignore` file. Test the final image before replacing a working release.

---

## 19. Complete DERP Deployment Workflow

This is the practical sequence for publishing a new backend release.

### Step 1: Open the backend folder

```powershell
cd "E:\Coding\Github\Derp_FullStack Project\DERP_Backend_Digital-Education-Records-Platform"
```

### Step 2: Build the JAR and run tests

```powershell
.\mvnw.cmd clean package
```

For a production release, prefer running tests rather than skipping them.

### Step 3: Build a new Docker image

```powershell
docker build -t ashish9693/derp-backend:2.10 .
```

### Step 4: Inspect the image

```powershell
docker images ashish9693/derp-backend
docker history ashish9693/derp-backend:2.10
```

### Step 5: Test the image locally

Use your actual `.env2` configuration and the required network settings:

```powershell
docker run -d `
  --name derp-backend-test `
  --env-file .env2 `
  -p 8081:8080 `
  ashish9693/derp-backend:2.10
```

Check its logs:

```powershell
docker logs --tail 100 derp-backend-test
```

Test the API and database connection. Remove the test container only after checking that no required data is stored in it:

```powershell
docker stop derp-backend-test
docker rm derp-backend-test
```

### Step 6: Publish the version

```powershell
docker login
docker push ashish9693/derp-backend:2.10
```

### Step 7: Update the `latest` tag

```powershell
docker tag ashish9693/derp-backend:2.10 ashish9693/derp-backend:latest
docker push ashish9693/derp-backend:latest
```

### Step 8: Verify the published tags

```powershell
docker manifest inspect ashish9693/derp-backend:2.10
docker manifest inspect ashish9693/derp-backend:latest
```

### Step 9: Deploy

If you deploy through Render, configure the correct Docker image, environment variables and database connection in the Render service settings. Trigger a deployment and check the deployment logs.

If you run the backend on your own Docker host, pull the version and recreate the container with its saved configuration.

**Note:** Publishing `latest` to Docker Hub does not itself update Render or an existing local container.

---

## 20. Docker Best Practices

1. **Use explicit version tags.** Prefer `2.10` for a controlled release and use `latest` as a convenience tag.
2. **Keep secrets outside the image.** Never commit `.env2` or bake it into a Docker layer.
3. **Use `.dockerignore`.** Exclude `.git`, `.env`, `.env2`, local build output when appropriate, and unrelated files from the build context.
4. **Use multi-stage builds.** Separate Maven compilation from the final runtime image.
5. **Test before pushing.** Confirm the application starts and can reach the intended database.
6. **Inspect before deleting.** Record container settings before recreating a container.
7. **Use persistent storage where required.** Database data should not depend on a container's writable layer.
8. **Use health checks.** Configure an appropriate health check when your application supports one.
9. **Monitor logs and resources.** Use `docker logs`, `docker stats`, and `docker inspect`.
10. **Keep rollback versions.** Do not overwrite or remove a known-good release until the new release is verified.
11. **Use secure authentication.** Use Docker Hub credentials or tokens securely; never put them in a public README.
12. **Use image digests for strict reproducibility.** Tags can be reassigned; digests identify a particular image manifest.

---

## Quick Command Cheat Sheet

| Task | Command |
|---|---|
| Check Docker | `docker --version` |
| List images | `docker images` |
| List running containers | `docker ps` |
| List all containers | `docker ps -a` |
| Build image | `docker build -t IMAGE:TAG .` |
| Pull image | `docker pull IMAGE:TAG` |
| Run container | `docker run -d --name NAME -p 8080:8080 IMAGE:TAG` |
| Stop container | `docker stop NAME` |
| Start container | `docker start NAME` |
| Restart container | `docker restart NAME` |
| Remove container | `docker rm NAME` |
| View logs | `docker logs NAME` |
| Follow logs | `docker logs -f NAME` |
| Inspect container | `docker inspect NAME` |
| Log in to Docker Hub | `docker login` |
| Push image | `docker push IMAGE:TAG` |
| Tag an image | `docker tag SOURCE TARGET` |
| Check disk usage | `docker system df` |
| View resource usage | `docker stats` |
| List networks | `docker network ls` |
| List volumes | `docker volume ls` |
| Start Compose | `docker compose up -d` |
| Stop Compose | `docker compose down` |

---

**Project image:** `ashish9693/derp-backend`

**Example release:** `ashish9693/derp-backend:2.10`

**Convenience tag:** `ashish9693/derp-backend:latest`

**Remember:** Build → Test → Tag → Push → Deploy → Verify.
