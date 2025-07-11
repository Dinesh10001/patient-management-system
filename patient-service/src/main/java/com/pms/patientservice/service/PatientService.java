package com.pms.patientservice.service;

import com.pms.patientservice.dto.PatientRequestDTO;
import com.pms.patientservice.dto.PatientResponseDTO;
import com.pms.patientservice.exception.EmailAlreadyExistsException;
import com.pms.patientservice.exception.PatientNotFoundException;
import com.pms.patientservice.grpc.BillingServiceGrpcClient;
import com.pms.patientservice.kafka.KafkaProducer;
import com.pms.patientservice.mapper.PatientMapper;
import com.pms.patientservice.model.Patient;
import com.pms.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer){
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();

        // .map(patient -> PatientMapper.toDTO(patient)).toList(); is replaced by short and method
        //below code is replaced by inline response to avoid unnecessary variables
        //  List<PatientResponseDTO> patientResponseDTOs = patients.stream()
        //        .map(PatientMapper::toDTO).toList();
        //
        //  return patientResponseDTOs;
        return patients.stream()
                .map(PatientMapper::toDTO).toList();
     }

     public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){

        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A patient with this email already exists " + patientRequestDTO.getEmail());
        }

        Patient newPatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO));


        //gRPC request
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());

        //kafka message
        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);
     }

     public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
            Patient patient = patientRepository.findById(id).orElseThrow(()-> new PatientNotFoundException("Patient not found with the id: "+ id));

         patient.setName(patientRequestDTO.getName());
         patient.setEmail(patientRequestDTO.getEmail());
         patient.setAddress(patientRequestDTO.getAddress());
         patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

         Patient updatedPatient = patientRepository.save(patient);

         return PatientMapper.toDTO((updatedPatient));

     }

     public void deletePatient(UUID id){
        patientRepository.deleteById(id);
     }
}

