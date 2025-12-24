---
title: Docs Conversion
keywords: 
last_updated: December 23, 2025
tags: []
summary: "Documents conversion service"
---

# Overview

Converts documents using JODConverter, which uses LibreOffice to convert documents. Please, find more info here:

https://jodconverter.github.io/jodconverter/latest/

## Configuration

No configuration available.

## Convert document

To convert a document, you should use this: 

```js
let res = svc.docsconv.convertDocument({
    inputFileId: fileId,
    inputMimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    outputMimeType: 'application/pdf'
});
if (res.status == 'ok') {
    log('converted file id: '+res.file.fileId);    
} else {
    log(res.error);
}
```

Here are the supported mime types:

- Text documents
  - `application/vnd.openxmlformats-officedocument.wordprocessingml.document` (.docx)
  - `application/msword` (.doc)
  - `application/vnd.oasis.opendocument.text` (.odt)
  - `application/rtf` (.rtf)
  - `text/plain` (.txt)
  - `application/pdf` (.pdf, only output)
  - `text/html` (.html)
- Spreadsheets
  - `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` (.xlsx)
  - `application/vnd.ms-excel` (.xls)
  - `application/vnd.oasis.opendocument.spreadsheet` (.ods)
  - `text/csv` (.csv)
  - `text/tab-separated-values` (.tsv)
  - `text/html` (.html, only output)
  - `application/pdf` (.pdf, only output)
- Presentations
  - `application/vnd.openxmlformats-officedocument.presentationml.presentation` (.pptx)
  - `application/vnd.ms-powerpoint` (.ppt)
  - `application/vnd.oasis.opendocument.presentation` (.odp)
  - `application/pdf` (.pdf, only output)
  - `text/html` (.html, only output)
- Drawings
  - `application/vnd.oasis.opendocument.graphics` (.odg)
  - `application/vnd.visio` (.vsd)
  - `image/svg+xml` (.svg)
  - `application/pdf` (.pdf, only output)
  - `image/png` (.png, only output)

You can convert between formats in each category. More options might be possible, you should check the documentation of JODConverter for more information.

## About SLINGR

SLINGR is a low-code rapid application development platform that accelerates development, with robust architecture for integrations and executing custom workflows and automation.

[More info about SLINGR](https://slingr.io)

## License

This service is licensed under the Apache License 2.0. See the `LICENSE` file for more details.
