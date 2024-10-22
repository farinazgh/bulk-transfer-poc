#!/bin/bash

# Variables
FILE_PATH="/path/to/your/file.dat"
STORAGE_ACCOUNT="<your_storage_account>"
CONTAINER_NAME="<your_container_name>"
BLOB_NAME="<your_blob_name>"
SAS_TOKEN="?sv=...&ss=...&srt=...&sp=...&se=...&st=...&spr=...&sig=..."

# Compute checksum
CHECKSUM=$(sha256sum "$FILE_PATH" | awk '{ print $1 }')
echo "Computed checksum: $CHECKSUM"

# Construct destination URL
DESTINATION_URL="https://${STORAGE_ACCOUNT}.blob.core.windows.net/${CONTAINER_NAME}/${BLOB_NAME}"
DESTINATION_URL_WITH_SAS="${DESTINATION_URL}${SAS_TOKEN}"

# Upload file with checksum metadata
azcopy copy "$FILE_PATH" "$DESTINATION_URL_WITH_SAS" --metadata "checksum=$CHECKSUM" --overwrite=true

# Check azcopy exit status
if [ $? -eq 0 ]; then
    echo "File uploaded successfully with checksum metadata."
else
    echo "File upload failed."
    exit 1
fi
