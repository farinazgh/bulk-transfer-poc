RESOURCE_GROUP="bulktransferneu"
NAMESPACE_NAME="bulktransfer-neu-sb-dev"
TOPIC_NAME="bulktransfer-blobevents-topic"

SUBSCRIPTIONS=("OmegaSubscription" "AlphaSubscription" "GammaSubscription")
PRODUCER_NAMES=("omega" "alpha" "gamma")


for i in "${!SUBSCRIPTIONS[@]}"; do
    SUBSCRIPTION_NAME="${SUBSCRIPTIONS[$i]}"
    PRODUCER_NAME="${PRODUCER_NAMES[$i]}"
    RULE_NAME="${PRODUCER_NAME^}Rule" # Capitalize the first letter for the rule name
    FILTER_EXPRESSION="producerName = '${PRODUCER_NAME}'"

    echo "Creating subscription: $SUBSCRIPTION_NAME"
    az servicebus topic subscription create \
        --resource-group "$RESOURCE_GROUP" \
        --namespace-name "$NAMESPACE_NAME" \
        --topic-name "$TOPIC_NAME" \
        --name "$SUBSCRIPTION_NAME"

    echo "Removing the default rule from $SUBSCRIPTION_NAME"
    az servicebus topic subscription rule delete \
        --resource-group "$RESOURCE_GROUP" \
        --namespace-name "$NAMESPACE_NAME" \
        --topic-name "$TOPIC_NAME" \
        --subscription-name "$SUBSCRIPTION_NAME" \
        --name '$Default'

    echo "Creating rule '$RULE_NAME' with filter '$FILTER_EXPRESSION' for $SUBSCRIPTION_NAME"
    az servicebus topic subscription rule create \
        --resource-group "$RESOURCE_GROUP" \
        --namespace-name "$NAMESPACE_NAME" \
        --topic-name "$TOPIC_NAME" \
        --subscription-name "$SUBSCRIPTION_NAME" \
        --name "$RULE_NAME" \
        --filter-sql-expression "$FILTER_EXPRESSION"
done
#az servicebus namespace show --resource-group bulktransferneu --name bulktransfer-neu-sb-dev
#az servicebus topic show --resource-group bulktransferneu --namespace-name bulktransfer-neu-sb-dev --name bulktransfer-neu-sb-dev
