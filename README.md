---
title: PDF Service
keywords: 
last_updated: September 25, 2023
tags: []
summary: "Detailed description of PDF Service."
---

# Overview

Converts documents using JODConverter, which uses LibreOffice to convert documents. Please, find more info here:

https://jodconverter.github.io/jodconverter/latest/

## Configuration

No configuration available.

## Convert document

To convert a document, you should use this: 

```javascript
let res = svc.docsconv.convertDocument({
    input: fileId,
    outputName: 'output.pdf'
});
log('converted file id: '+res.fileId);
```

## About SLINGR

SLINGR is a low-code rapid application development platform that accelerates development, with robust architecture for integrations and executing custom workflows and automation.

[More info about SLINGR](https://slingr.io)

## License

This service is licensed under the Apache License 2.0. See the `LICENSE` file for more details.
