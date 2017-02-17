package org.snomed.heathanalytics.ingestion.exampledata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.heathanalytics.domain.Patient;
import org.snomed.heathanalytics.domain.Sex;
import org.snomed.heathanalytics.ingestion.HealthDataIngestionSource;
import org.snomed.heathanalytics.ingestion.HealthDataOutputStream;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.snomed.heathanalytics.ingestion.exampledata.ExampleDataGenerator.DateUtil.dateOfBirthFromAge;
import static org.snomed.heathanalytics.ingestion.exampledata.ExampleDataGenerator.DateUtil.getAge;

public class ExampleDataGenerator implements HealthDataIngestionSource {

	private final ExampleConceptService concepts;
	private int numberOfPatients;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ExampleDataGenerator(ExampleConceptService exampleConceptService, int numberOfPatients) {
		this.concepts = exampleConceptService;
		this.numberOfPatients = numberOfPatients;
	}

	@Override
	public void stream(HealthDataOutputStream healthDataOutputStream) {
		long start = new Date().getTime();
		IntStream.range(0, numberOfPatients).parallel().forEach(i -> {
			if (i % 1000 == 0) {
				System.out.print(".");
			}
			generateExamplePatientAndActs(i + "", healthDataOutputStream);
		});
		System.out.println();
		logger.info("Generating patient data took {} seconds.", (new Date().getTime() - start) / 1000);
	}

	private void generateExamplePatientAndActs(String roleId, HealthDataOutputStream healthDataOutputStream) {
		Patient patient = new Patient();

		//  All patients are over the age of 30 and under the age of 85.
		patient.setDob(dateOfBirthFromAge(ThreadLocalRandom.current().nextInt(30, 85)));

		//  50% of patients are Male.
		if (chance(0.5f)) {
			patient.setSex(Sex.MALE);
		} else {
			patient.setSex(Sex.FEMALE);
		}

		healthDataOutputStream.createPatient(roleId, patient.getName(), patient.getDob(), patient.getSex());
		GregorianCalendar encounterDate = new GregorianCalendar();
		// 10% of patients have diabetes.
		if (chance(0.1f)) {
			healthDataOutputStream.addClinicalEncounter(roleId, encounterDate.getTime(), concepts.selectRandomChildOf("420868002"));// Disorder due to type 1 diabetes mellitus

			// 7% of the diabetic patients also have Peripheral Neuropathy.
			if (chance(0.07f)) {
				healthDataOutputStream.addClinicalEncounter(roleId, encounterDate.getTime(), concepts.selectRandomChildOf("302226006"));// Peripheral Neuropathy
			}

			// 10% of the diabetic patients have a Myocardial Infarction.
			if (chance(0.1f)) {
				healthDataOutputStream.addClinicalEncounter(roleId, encounterDate.getTime(), concepts.selectRandomChildOf("22298006"));// Myocardial Infarction
			}
		} else {
			// 1% of the non-diabetic patients have Peripheral Neuropathy.
			if (chance(0.01f)) {
				healthDataOutputStream.addClinicalEncounter(roleId, encounterDate.getTime(), concepts.selectRandomChildOf("302226006"));// Peripheral Neuropathy
			}
		}

		// 30 % of patients over 40 years old have hypertension.
		if (getAge(patient.getDob()) > 40 && chance(0.3f)) {
			healthDataOutputStream.addClinicalEncounter(roleId, encounterDate.getTime(), concepts.selectRandomChildOf("38341003"));// Hypertension

			// 8% of patients with hypertension have a Myocardial Infarction.
			if (chance(0.08f)) {
				healthDataOutputStream.addClinicalEncounter(roleId, encounterDate.getTime(), concepts.selectRandomChildOf("22298006"));// Myocardial Infarction
			}
		}

		// 5% of all patients over 55 years old have Myocardial Infarction.
		if (getAge(patient.getDob()) > 55 && chance(0.05f)) {
			healthDataOutputStream.addClinicalEncounter(roleId, encounterDate.getTime(), concepts.selectRandomChildOf("22298006"));// Myocardial Infarction
		}
	}

	private boolean chance(float probability) {
		return probability >= Math.random();
	}

	static final class DateUtil {

		static long millisecondsInAYear;
		static {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(new Date(0));
			calendar.add(Calendar.YEAR, 1);
			millisecondsInAYear = calendar.getTime().getTime();
		}

		static int getAge(Date patientDob) {
			// Rough calculation for example data
			return Math.round((new Date().getTime() - patientDob.getTime()) / millisecondsInAYear);
		}

		static Date dateOfBirthFromAge(int ageInYears) {
			GregorianCalendar date = new GregorianCalendar();
			date.add(Calendar.YEAR, -ageInYears);
			return date.getTime();
		}
	}
}
