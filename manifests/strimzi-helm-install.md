kubectl create namespace data-dev
$ helm uninstall strimzi-kafka-operator -n data-dev
helm repo remove strimzi
helm repo add strimzi https://strimzi.io/charts/
helm repo update
helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator -n data-dev
