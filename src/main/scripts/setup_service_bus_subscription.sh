#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Optional: Log in to Azure if not already logged in
echo "Logging into Azure..."
az login

# Set the active subscription
SUBSCRIPTION_ID="<your_subscription_id>"
echo "Setting the active subscription to $SUBSCRIPTION_ID"
az account set --subscription "$SUBSCRIPTION_ID"

# Variables - replace these with your actual values
RESOURCE_GROUP="<your_resource_group_name>"
NAMESPACE_NAME="<your_namespace_name>"
TOPIC_NAME="<your_topic_name>"
SUBSCRIPTION_NAME="<your_subscription_name>"
RULE_NAME="OmegaRule"
FILTER_EXPRESSION="ProcessName = 'Omega'"

echo "Resource Group: $RESOURCE_GROUP"
echo "Namespace: $NAMESPACE_NAME"
echo "Topic: $TOPIC_NAME"
echo "Subscription: $SUBSCRIPTION_NAME"

# Remove the default rule
echo "Removing the default rule..."
az servicebus topic subscription rule delete \
    --resource-group "$RESOURCE_GROUP" \
    --namespace-name "$NAMESPACE_NAME" \
    --topic-name "$TOPIC_NAME" \
    --subscription-name "$SUBSCRIPTION_NAME" \
    --name '$Default'

# Create the new rule with SQL filter
echo "Creating new rule '$RULE_NAME' with filter '$FILTER_EXPRESSION'..."
az servicebus topic subscription rule create \
    --resource-group "$RESOURCE_GROUP" \
    --namespace-name "$NAMESPACE_NAME" \
    --topic-name "$TOPIC_NAME" \
    --subscription-name "$SUBSCRIPTION_NAME" \
    --name "$RULE_NAME" \
    --filter-sql-expression "$FILTER_EXPRESSION"

echo "Subscription rule '$RULE_NAME' created successfully."

# List the rules to verify
echo "Listing rules for the subscription..."
az servicebus topic subscription rule list \
    --resource-group "$RESOURCE_GROUP" \
    --namespace-name "$NAMESPACE_NAME" \
    --topic-name "$TOPIC_NAME" \
    --subscription-name "$SUBSCRIPTION_NAME"

echo "Script execution completed successfully."
