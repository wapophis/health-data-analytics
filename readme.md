# Health Data Analytics Demonstrator

## Capabilities
- Value set generator that works with SNOMED subsumption with inclusion, exclusion and attribute constraints.
- Data analysis tool which finds cohorts of patients with given conditions, procedures and/or prescriptions.
- Statistical testing to measure how a procedure or drug affects the chances of a subsequent disorder.
- Demo data generator which creates a large population of patients including disease associations and conditions.


## Background and strategic fit
SNOMED CT’s distinguishing feature is its logical framework.  Unlike ICD terms which are not based on Description Logic, SNOMED features the ability to do logical subsumption searches to find cohorts of patients, medications, or conditions.

Every kind of quality measure, outcomes measurement or retrospective research involving cohorts of patients depends on defining lists of clinical terms sometimes called “value sets”.

When these types of data analysis are done with ICD or other non-SNOMED terminologies, the creation of value sets requires human experts and is prone to errors. The humans creating the value sets must remember all the possible ways to refer to a condition, and ICD does not allow more than one parent, so Viral Pneumonia much be either a respiratory disease or an infectious disease but can’t be both.

In SNOMED because of its logical structure, you can search hierarchies and do logical role based searches. This allows you to create value sets without arbitrary knowledge of which synonyms are used to describe a term.

These data analysis tasks require three steps that we can define as a High Value Implementation of SNOMED.

First an organization must be able to create value sets by using SNOMED subsumption searches.  This task is independent of any patient data.  If the patient data is linked to SNOMED directly, then the cohort of patients can be found without any further processing.  But if the EMR uses a non-SNOMED interface terminology, then there must be an intermediate step mapping the SNOMED value set to the interface terms.  For the purposes of our demo tool, we will use SNOMED terms directly linked to patient data.

These principles are difficult to explain to clinicians, in order to demonstrate these advantages to them,  it is necessary to have a tool that is populated with SNOMED coded patient data.  With such a tool we can highlight the advantages of SNOMED compared to ICD (or any other terminology).

Real patient data (a large population linked to SNOMED) is practically impossible to get for a variety of reasons.  Real data would contain expected associations between diseases, events, medications and conditions. For example, real data would show that fractured bones were more common after a motor vehicle accident.  Real data would show that infections were more common after an immunosuppressive medication were given.  Real data would also show these and other known links between conditions. Other obvious examples are patients with COPD would have pneumonia and bronchitis more often than patients in general.  Diabetics would have more foot ulcers and more peripheral neuropathy.


## Summary
This demo tool is a real tool. If we were to get some real patient data and stream it into the tool then we could explore for real associations.


## Technical Information

### Web User Interface
A frontend web application for this API is available [here](https://github.com/IHTSDO/health-data-analytics-frontend).

### Data Model
The data model is very simple:
- Patient
  - roleId
  - dob
  - dobYear (for optimisation)
  - gender (MALE / FEMALE)
  - encounters (many ClinicalEncounters)
- ClinicalEncounter
  - date
  - conceptId (SNOMED CT Concept Identifier)

A clinical encounter could represent an observation, finding, drug prescription or procedure depending on the SNOMED CT concept used.

### Project Setup
#### Prerequisites
- Java 8 or later
- 4G of memory
- SNOMED CT release RF2 archive
- Patient data (can be generated)

Build the project using maven.
```bash
mvn clean install
```

Extract the SNOMED CT archive to a directory named `release` in the root of the project. Only the Snapshot files will be used so any others can be removed if needed.

### Synthetic Patient Generation
If you would like to use synthetic data a patient population can be generated using the `generator` module.  
From the root of the project the following command will generate 1,000,000 patients, which takes ~1 minute.
```bash
java -Xms3g -jar generator/target/generator*.jar
``` 
Command options:
- `--population-size` Optional. Defaults to 1,000,000.

The generated population will be written in NDJSON format to a directory named `patient-data-for-import`.

### Server
The server module provides the Data Analytics API. A Java application using Spring Boot with Swagger API documentation.

#### Patient Data Import
Patient data, in NDJSON format using the model above, can be imported into the server with this command: 
```bash
java -Xms3g -jar server/target/server*.jar --import-population='patient-data-for-import'
```
The program will exit when all patient data has been consumed.

#### Run the Server
Once the patient data has been loaded run the server without the import argument:
```bash
java -Xms3g -jar server/target/server*.jar
```
Once the server is started the API and documentation will be available here: http://localhost:8080/health-analytics-api/

#### Data Stores
The following data stores will be created when the server starts.
- `snomed-index` directory contains a Lucene index of the SNOMED CT release to provide semantic information for the server.
- `data` directory contains an Elasticsearch index of the imported patient data.

The server should be stopped before removing either of these data stores.

#### Realtime Patient Data
Patient data can be loaded in realtime by implementing HealthDataIngestionSource interface. For example a class could be added which receives 
patient record updates over JMS or polls a directory.
