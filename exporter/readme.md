# Data Migration Validator
This document describes the Data Migration Validator, a tool that validates Elasticsearch documents. This validation is the first step in the process of migrating data from Elasticsearch 5.6.x to Postgresql 12. The Validator can be used for validating documents produced by RINA versions 5.6.x. The Validator is a Spring Boot application, and requires Java 1.8.

## Deploy
The maven project is configured to be deployed as a jar.

To build the project and create the jar, run the following command from the root folder:

```mvn clean install```
    
Before running the Validator, one needs to configure the [application.properties](../application/src/main/resources/application.properties). As per common practices when deploying spring boot applications, this file must be copied where the jar is deployed, in a subfolder named `config`. The following properties must be configured in the `application.properties`:
* `elasticsearch.host`: the host of the Elasticsearch node (it does not matter if Elasticsearch is clustered, a connection to any active node will suffice)
* `elasticsearch.port`: the port of the Elasticsearch node
* `reporting.folder`: the output folder where the validation reports will be saved; this will be automatically created if it does not exist
* `logging.config`: the path to the `log4j2.xml` configuration file; it is recommended that pre-configured [log4j2.xml](../application/src/main/resources/log4j2.xml) is copied in the same folder as `application.properties`

With this recommendations, the folder structure would look like this:
```
.
└── deploy_folder                           # the folder where the jar is deployed
    ├── EESSI-RINA-DATA-MIGRATION-x.y.z.jar # the Validator jar
    └── config                              # config folder
        ├── application.properties          # Validator configurations 
        └── log4j2.xml                      # logging configurations
```

The Validator can be ran with the following command:
```
java -jar EESSI-RINA-DATA-MIGRATION-x.y.z.jar
``` 

While running, the Validator will write to the console various information such as the index that is being processed, number of documents, and other stats. The validation process should end with a line like this: `Total processing time: [...]`.

The output of the Validator is a set of reports containing validation results on different levels. These reports aggregate information on what types of documents were processed, how many, and a summary of the validation issues found (this last part may be missing if no errors are found). The report files are formatted as jsons.

Currently, there are two types of reports:
 * an aggregated overview report at the index level
 * a more detailed report at the case level
 
 For each index that is being processed, the Validator creates an aggregated overview report which is saved as `<index_name>.json` in the reporting folder. If validation errors are found when processing case resources, another set of reports is created in a subfolder of the reporting folder named `cases`. These are more detailed reports and are aggregated at the case level, meaning that all the validation errors found in any of the resources belonging to a case will be saved in the same file, which is named `case_<caseId>.json`. The subfolder `cases` may be completely missing if no validation errors are found in any of the cases.
 
 As an example, structure of the output folder may look like this:
 ```
.
└── reports                     # the reporting folder
    ├── cases.json              # report for index cases
    ├── entities.json           # report for index entities
    ├── identity_v1.json        # report for index identity_v1
    ├── [...]                   # other reports
    └── cases                   # folder containing detailed case reports
        ├── case_1111.json      # aggregated report for case 1111 
        └── case_2222.json      # aggregated report for case 2222
 ```

The logging used within the Validator is log4j2. The pre-configured file can be found here [log4j2.xml](../application/src/main/resources/log4j2.xml). By default the logger uses a `FileAppender` with log rotation, and the log level is set to `INFO`. Note that changing this to `DEBUG` may create a large quantity of logs (the number of files or their size may need to be increased to store all log information).

## Components
The Validator consists of two main components: the [Field Mappings](#field-mappings) and the [Validation Engine](#validation-engine). The Field Mappings contain the definitions of the fields and various types of validation to be performed on the fields. The Validation Engine goes through all the documents in Elasticsearch and validates them according to the rules defined in the field mappings. 

### Field Mappings
Field mappings are json files that describe how to validate data from Elasticsearch. The json files are named by convention as index_type.json and contain the validation details for Elasticsearch documents of that particular index and type. They are stored in the folder [resources/field_mappings](src/main/resources/field_mappings).

The list of fields for each type of Elasticsearch document is extracted from the Elasticsearch mappings. RINA does not provide a concrete schema in form of predefined static mappings for the indices. Instead, Elasticsearch constructs the mappings dynamically, based on the contents of the indexed documents. These mappings may therefore be different on different machines depending on how RINA was used and what type of data was stored. For this reason, we tried to aggregate mappings extracted from different RINA installations into meta-mappings. The creation of the meta-mappings and the extraction of the fields and their data types are external to the Validator and will not be described here.

The json files are structured as lists of field mapping objects, which have the following keys:
* `esPath`: the path of the field inside the document
* `esType`: the type of the field, as extracted from the Elasticsearch mapping
* `ignorePath`: boolean flag that indicates whether this field and all its sub-fields must be ignored from any validation. By default this is set to false.
* `required`: boolean flag for validating whether the value exists and is not null 
* `sqlPath`: an indication of how to map the field to the corresponding sql field path in the current sql schema
* `sqlType`: the type of the corresponding sql field
* `value`: information describing the semantic validation of the field value. The possible options are:
    * `reference`: the value is a valid reference; this is defined as `reference(<index>,<type>[,<format>])`; for example, this is used for validating a reference to a document id: `reference(cases,document,caseId_id)`
    * `duplicate`: the value is a duplicate of another field value; this is defined as `duplicate(<field_path>)`; for example, this is used for validating that the value is the same as the caseId: `duplicate(caseId)`
    * `enum`: the value must be taken from an enum; this is not yet implemented
    * `regex`: the value must match a given regex; this is defined as `regex(<regex>)`; for example, this is used for validating the process definition name: `regex((PO|CP)_[A-Z]{1,3}_BUC_[0-9]{1,2}\w?_v4\.[012])`

As an example, a field mapping object may look like this:
```
{
    "esPath": "creator.id",
    "esType": "string",
    "required": true,
    "sqlPath": "iamUser.id",
    "sqlType": "VARCHAR(255)",
    "value": "reference(identity_v1,user)"
}
```

### Validation Engine
The Validation Engine is the component that validates all the documents in Elasticsearch. It loads the validation definitions from the field mappings, then applies the validation rules to every document in Elasticsearch. The output of this validation is a set of reports defined at specific levels (e.g. at document level, case level, context level).

In short, this is what happens when the Validator tool is ran:
* for each known combination of index and type, the associated field mapping json file is loaded (if exists)
* the contents of the field mappings are processed and a final schema is created; this schema essentially maps document paths to lists of pre-configured validator instances; the schema is private, and cannot be changed; the validators states are not shared and don't need to be synchronized
* a schema provider is created for dispatching validation requests to the proper schema
* a document is fetched from Elasticsearch for validation
* the document is recursively parsed; there is specific logic for handling objects, arrays and leaf fields
* an aggregator instance is injected in the parser; the aggregator performs basically two functions: executes certain actions on the field (in this case the actual validation), and provides means of aggregating results
* some validators (e.g. [ReferenceValidator](src/main/java/eu/ec/dgempl/eessi/rina/tool/migration/exporter/validator/ReferenceValidator.java)) need to fetch additional data from Elasticsearch; a cache is provided for speeding things up by minimizing the number of requests to Elasticsearch
* the reporting system propagates the validation results upwards, from the fields to the document, case, and context
