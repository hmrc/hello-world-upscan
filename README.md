
# `hello-world-upscan` 

## Context

This is an example frontend service created to demonstrate the use of Upscan to allow users to upload files to MDTP safely. The service now includes integration with Object Store, which enables teams to persist files on the platform beyond Upscan's revised storage expiration of six hours.

## Running the service locally

    sm2 --start UPSCAN_STUB OBJECT_STORE_STUB INTERNAL_AUTH
    sbt run

And then navigate to one of the following:

 - http://localhost:9001/hello-world-upscan/hello-world


This hello-world project is configured to allow uploading of the following filetypes: ".pdf,.doc,.docx,.xlsx,.xls,.png,.jpeg,.jpg,.txt", and specifies a maximum filesize of 512 bytes.

### Local `object-store` persistence

Files uploaded an persisted to `object-store` are available under `/tmp/object-store/hello-world-upscan/` the format for file paths under this directory (for this example service) is `/<file-reference>/<file-name>`.

## Configuration

### To increase the maximum file size limit

In order to increase the maximum file size limit edit the `UpscanInitiateRequest` class and modify the `maximumFileSize` field. The file size is specified in **Bytes** as an Integer number.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
