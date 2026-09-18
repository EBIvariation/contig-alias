# contig-alias #
Reference sequences are files that are used as a reference to describe variants that are present in analyzed sequences and play a central role in defining a baseline of knowledge against which our understanding of biological systems, phenotypes and variation are based upon. Reference sequence files often use different naming schemes to refer to the same sequence and thus there is a strong need to be able to cross reference chromosomes/contigs using different nomenclatures. Thus there is a need for a centralized database with a alias resolution service that can cross reference accessions easily and reliably. Also a web service is required that allows users to access these services from any client and has a mechanism for manually or periodically ingesting new aliases from a remote datasource.


## Build

Build the jar:
```
mvn clean package -DskipTests
```

Build the Docker image:
```
docker build -t contig-alias:local .
```

## Deployment

Deployment to Kubernetes is handled by GitLab CI (`.gitlab-ci.yml`)

## Local testing with Docker Compose

`docker-compose.yaml` runs the app together with a local PostgreSQL instance to test the image itself.

1. Build and start everything:
   ```
   docker compose up --build
   ```
2. Check the health endpoint:
   ```
   curl -i http://localhost:8080/eva/webservices/contig-alias/health
   ```
3. Tear down when done:
   ```
   docker compose down -v
   ```

## Local testing with Kubernetes

The `local` overlay in the [eva-k8s](https://github.com/EBIvariation/eva-k8s) repository deploys this service plus an in-cluster PostgreSQL to any local Kubernetes cluster.

Prerequisites: a local cluster with `kubectl` pointed at it, and a local checkout of the `eva-k8s` repository.

1. Build the image the manifests expect (same as under [Build](#build)):
   ```
   docker build -t contig-alias:local .
   ```
2. Apply the local overlay, pointing `EVA_K8S_DIR` at your `eva-k8s` checkout:
   ```
   EVA_K8S_DIR=/path/to/eva-k8s
   kubectl apply -k "$EVA_K8S_DIR/k8s-manifests/contig-alias/overlays/local"
   ```
3. Wait for both deployments to roll out:
   ```
   kubectl rollout status deployment/postgres -n contig-alias-local --timeout=90s
   kubectl rollout status deployment/contig-alias -n contig-alias-local --timeout=120s
   ```
4. Reach the app.
   ```
   curl -i http://localhost:8080/eva/webservices/contig-alias/health
   ```
    If your local cluster doesn't auto-forward `LoadBalancer` ports (e.g. plain kind/minikube), or you want a different local port without editing the manifest, use `kubectl port-forward` instead:
   ```
   kubectl port-forward -n contig-alias-local svc/contig-alias 18080:8080
   curl -i http://localhost:18080/eva/webservices/contig-alias/health
   ```
5. Tear down when done:
   ```
   kubectl delete namespace contig-alias-local
   ```
