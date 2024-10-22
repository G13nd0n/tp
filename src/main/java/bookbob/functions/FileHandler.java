package bookbob.functions;

import bookbob.entity.Patient;
import bookbob.entity.Records;
import bookbob.entity.AppointmentRecord;
import bookbob.entity.Appointment;
import bookbob.entity.Visit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHandler {

    private static final Logger logger = Logger.getLogger(FileHandler.class.getName());
    private static String filePath = "data" + File.separator + "bookbob_data.txt";
    private static String appointmentFilePath = "data" + File.separator + "bookbob_appointment.txt";

    public static void initFile(Records records){
        try {

            String directoryName = "data";
            String currentDirectory = System.getProperty("user.dir");
            String directory = currentDirectory + File.separator + directoryName;
            File directoryFile = new File(directory);

            if(directoryFile.mkdirs()) {           //directory was not created
                File file = new File(filePath);
                file.createNewFile();              //create new data file
            } else {                               //directory already created
                logger.log(Level.INFO, "Directory exists");
                File file = new File(filePath);
                if(file.createNewFile()) {         //file was not created
                    logger.log(Level.INFO, "Directory exists, creating new file");
                } else {
                    retrieveData(records);
                }
            }
        } catch(Exception e){
            logger.log(Level.WARNING, "Error initialising file", e);
            e.printStackTrace();
        }
    }

    public static void initFile(AppointmentRecord appointmentRecord){
        try {
            String directoryName = "data";
            String currentDirectory = System.getProperty("user.dir");
            String directory = currentDirectory + File.separator + directoryName;
            File directoryFile = new File(directory);

            if(directoryFile.mkdirs()) {           //directory was not created
                File file = new File(filePath);
                file.createNewFile();              //create new data file
            } else {                               //directory already created
                logger.log(Level.INFO, "Directory existed, creating new file");
                File file = new File(appointmentFilePath);
                if(file.createNewFile()) {         //file was not created
                    logger.log(Level.INFO, "Directory existed, creating new file");
                } else {
                    retrieveData(appointmentRecord);
                }
            }
        } catch(Exception e){
            logger.log(Level.WARNING, "Error initializing file", e);
            e.printStackTrace();
        }
    }

    public static String convertPatientToOutputText(Patient patient) {
        String output = "";
        output += "Name: " + patient.getName() + " | " + "NRIC: " + patient.getNric() + " | "
                + "Phone Number: " + patient.getPhoneNumber() + " | " + "Date_Of_Birth: " + patient.getDateOfBirth()
                + " | " + "Home Address: " + patient.getHomeAddress() + " | " + "Allergy: " + patient.getAllergy()
                + " | " + "Sex: " + patient.getSex() + " | " + "Medical History: " + patient.getMedicalHistory()
                + "Visit: " + patient.getVisit() + ";";

        return output;
    }

    public static String convertPatientToOutputText(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String patientName = appointment.getPatientName();
        String patientNric = appointment.getPatientNric();
        String date = appointment.getDate().format(formatter);
        String time = appointment.getTime().toString();
        String output = "";
        output += "Name: " + patientName + "|" + "NRIC: " + patientNric + "|"
                + "Date: " + date  + "|" + "Time: " + time;
        return output;
    }

    public static void autosave(Records records) throws IOException {
        List<Patient> patients = records.getPatients();
        FileWriter fw = new FileWriter(filePath);
        for (Patient currPatient : patients) {
            String toWrite = convertPatientToOutputText(currPatient);
            fw.write(toWrite + "\n");
        }
        fw.close();
        logger.log(Level.INFO, "Autosaved successfully");
    }

    public static void autosave(AppointmentRecord appointmentRecord) throws IOException {
        List<Appointment> appointments = appointmentRecord.getAppointments();
        FileWriter fw = new FileWriter(appointmentFilePath);
        for (Appointment appointment : appointments) {
            String toWrite = convertPatientToOutputText(appointment);
            fw.write(toWrite + "\n");
        }
        fw.close();
        logger.log(Level.INFO, "Autosaved appointments successfully");
    }

    public static void retrieveData(Records records){
        try {
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] data = line.split("\\|");
                String name = data[0].substring(6).trim();
                String nric = data[1].substring(6).trim();
                String phoneNumber = data[2].substring(15).trim();
                String dateOfBirth = data[3].substring(16).trim();
                String homeAddress = data[4].substring(15).trim();
                String allergy = data[5].substring(9).trim();
                String sex = data[6].substring(5).trim();
                String medicalHistory = data[7].substring(17).trim();
                String visitDetails = data[8];
                // Parse the visit information
                List<Visit> visits = new ArrayList<>();
                Visit visit = parseVisitInputString(visitDetails);
                visits.add(visit);
                Patient patient = new Patient(name, nric, phoneNumber, dateOfBirth, homeAddress, allergy,
                        sex, medicalHistory, visits);
                records.addPatient(patient);
            }
            logger.log(Level.INFO, "Data retrieved successfully");
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "File not found", e);
            throw new RuntimeException(e);
        }
    }

    //@@author coraleaf0602
    // Parses string with visit details and creates visit object
    public static Visit parseVisitInputString(String visitString) {
        int visitStartIndex = visitString.indexOf("[") + 1;
        int visitEndIndex = visitString.lastIndexOf("]");
        String visitDetails = visitString.substring(visitStartIndex, visitEndIndex);

        // Split visit details into individual components
        String[] components = visitDetails.split(", Diagnosis: \\[|\\], Medications: \\[|\\]");

        // Parse date and time
        String dateTimeString = components[0].trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime visitDateTime = LocalDateTime.parse(dateTimeString, formatter);

        // Parse diagnosis
        String diagnosisString = components[1].trim();
        List<String> diagnosisList = new ArrayList<>();
        for (String diagnosis : diagnosisString.split(",\\s*")) {
            diagnosisList.add(diagnosis);
        }

        // Parse medications
        String medicationsString = components[2].trim();
        List<String> medicationsList = new ArrayList<>();
        for (String medication : medicationsString.split(",\\s*")) {
            medicationsList.add(medication);
        }
        return new Visit(visitDateTime, diagnosisList, medicationsList);
    }

    public static void retrieveData(AppointmentRecord appointmentRecord){
        try {
            File file = new File(appointmentFilePath);
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] data = line.split("\\|");
                String name = data[0].substring(6).trim();
                String nric = data[1].substring(6).trim();
                String date = data[2].substring(6).trim();
                String time = data[3].substring(6).trim();
                Appointment appointment = new Appointment(name, nric, date, time);
                appointmentRecord.addAppointment(appointment);
            }
            logger.log(Level.INFO, "Retrieved successfully");
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "File not found", e);
            throw new RuntimeException(e);
        }
    }
}
