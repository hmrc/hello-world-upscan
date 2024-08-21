
# hello-world-upscan

This is a demo of upscan, and how to integrate with it.

    sm2 --start UPSCAN_STUB
    sbt run

And then navigate to one of the following:

 - http://localhost:9001/hello-world-upscan/hello-world
 - http://localhost:9001/hello-world-upscan/v2/hello-world

This hello-world project is configured to allow uploading of the following filetypes: ".pdf,.doc,.docx,.xlsx,.xls,.png,.jpeg,.jpg,.txt", and specifies a maximum filesize of 512 bytes.

## Configuration

### To increase the maximum file size limit

In order to increase the maximum file size limit edit the `UpscanInitiateRequestV1` or `UpscanInitiateRequestV2` class
depending on which routes you are planning to hit, and modify the `maximumFileSize` field.
The file size is specified in **Bytes** as an Integer number.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
