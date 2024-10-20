az servicebus topic create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --name bulktransfer-blobevents-topic
az servicebus topic subscription create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --topic-name bulktransfer-blobevents-topic --name consumerOmegaSubscription
az servicebus topic subscription rule create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --topic-name bulktransfer-blobevents-topic --subscription-name consumerOmegaSubscription --name Rule1 --filter "producer.containerName = 'omega-container'"
az servicebus topic subscription rule create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --topic-name bulktransfer-blobevents-topic --subscription-name consumerOmegaSubscription --name Rule1 --filter "producer.containerName = 'omega-container'"
az servicebus topic update --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --name bulktransfer-blobevents-topic --enable-duplicate-detection true --duplicate-detection-history-time-window PT10M
az servicebus topic create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --name FileProcessingTopic
az servicebus topic subscription create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --topic-name FileProcessingTopic --name ConsumerOmegaSubscription
az servicebus topic subscription rule create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --topic-name FileProcessingTopic --subscription-name ConsumerOmegaSubscription --name Rule1 --filter "producer.containerName = 'omega-container'"
#// Azure CLI Command (for reference)
#// az servicebus topic create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --name bulktransfer-blobevents-topic
#// Example: Creating a Subscription with a SQL Filter using Azure Management Libraries (not shown here)
#// Alternatively, use Azure Portal or Azure CLI to set up filters
#producer.containerName = 'omega-container'
az servicebus topic update --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --name FileProcessingTopic --enable-duplicate-detection true --duplicate-detection-history-time-window PT10M
az servicebus topic subscription rule create --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --topic-name FileProcessingTopic --subscription-name ConsumerOmegaSubscription --name Rule1 --filter "producer.containerName = 'omega-container'"
producer.containerName = 'omega-container'
az servicebus topic create \
  --resource-group bulktransferneu \
  --namespace-name bulktransfer-neu-sb-dev \
  --name FileProcessingTopic \
  --enable-duplicate-detection true \
  --duplicate-detection-history-time-window PT10M
az servicebus topic subscription create \
  --resource-group bulktransferneu \
  --namespace-name bulktransfer-neu-sb-dev \
  --topic-name FileProcessingTopic \
  --name ConsumerOmegaSubscription
az servicebus topic subscription rule create \
  --resource-group bulktransferneu \
  --namespace-name bulktransfer-neu-sb-dev \
  --topic-name FileProcessingTopic \
  --subscription-name ConsumerOmegaSubscription \
  --name OmegaFilter \
  --filter "producer.containerName = 'omega-container'"
az servicebus topic subscription rule create \
  --resource-group bulktransferneu \
  --namespace-name bulktransfer-neu-sb-dev \
  --topic-name FileProcessingTopic \
  --subscription-name ConsumerOmegaSubscription \
  --name OmegaFilter \
  --filter "producer.containerName = 'omega-container'"
