#!/bin/bash

# Path to the downloaded file
DOWNLOADED_FILE_PATH="/path/to/downloaded/file.dat"

# The checksum received from the message (e.g., extracted from the Service Bus message)
RECEIVED_CHECKSUM="the_checksum_from_message"

# Compute the checksum of the downloaded file
COMPUTED_CHECKSUM=$(sha256sum "$DOWNLOADED_FILE_PATH" | awk '{ print $1 }')

echo "Computed checksum: $COMPUTED_CHECKSUM"
echo "Received checksum: $RECEIVED_CHECKSUM"

# Compare checksums
if [ "$COMPUTED_CHECKSUM" == "$RECEIVED_CHECKSUM" ]; then
    echo "Checksum validation succeeded. File is intact."
else
    echo "Checksum validation failed. File may be corrupted."
    # Handle the error as needed
fi
