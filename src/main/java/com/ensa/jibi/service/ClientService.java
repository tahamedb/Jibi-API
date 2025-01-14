package com.ensa.jibi.service;

import com.ensa.jibi.cmi.CmiService;
import com.ensa.jibi.dto.ClientDTO;
import com.ensa.jibi.model.Client;
import com.ensa.jibi.repository.ClientRepository;
import com.ensa.jibi.sms.TwilioSmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Optional;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TwilioSmsSender twilioSmsSender;

    @Autowired
    private CmiService cmiService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Client registerClient(Client client) {
        if (!cmiService.isResponseFavorable(client)) {
            throw new IllegalArgumentException("CMI response is not favorable");
        }
        String tempPassword = generateTemporaryPassword();
        client.setPassword(passwordEncoder.encode(tempPassword));  // Hashing du mot de passe temporaire
        twilioSmsSender.sendSms(client.getPhone(), "Your temporary password is: " + tempPassword);
        return clientRepository.save(client);
    }

    public Optional<Client> verifyPassword(String phone, String password) {
        Optional<Client> clientOpt = clientRepository.findByPhone(phone);
        System.out.println(clientOpt.get());
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if (passwordEncoder.matches(password, client.getPassword())) {
                return Optional.of(client);
            }
        }
        return Optional.empty();
    }

    public boolean changePassword(String phone, String currentPassword, String newPassword) {
        Optional<Client> clientOpt = clientRepository.findByPhone(phone);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if (!passwordEncoder.matches(currentPassword, client.getPassword())) {
                return false; // Current password does not match
            }
            client.setPassword(passwordEncoder.encode(newPassword));  // Hashing du nouveau mot de passe
            client.setRequiresPasswordChange(false);
            clientRepository.save(client);
            return true; // Password changed successfully
        }
        return false; // Client not found
    }
    private String generateTemporaryPassword() {
        return RandomStringUtils.randomAlphanumeric(8);
    }
    public Double getPlafond(String accountType){
        switch(accountType){
            case "hsab1": return 200.0;
            case "hsab2": return 5000.0;
            case "hsab3": return 20000.0;
            default: throw new IllegalArgumentException("Invalid account type: " + accountType);
        }
    }

    public Optional<Long> getClientIdByPhoneNumber(String phoneNumber) {
        // Implement your logic to retrieve the client ID from the database based on the phone number
        // Assuming you have a Client entity with a phoneNumber field in your database
        Optional<Client> client = clientRepository.findByPhone(phoneNumber);
        return client.get().getId().describeConstable();
    }

    public ClientDTO getClientByPhoneNumber(String phoneNumber) {
        return new ClientDTO(clientRepository.findByPhone(phoneNumber).get());
    }

    public Float getbalancebyPhone(String phone){
        Long clientId = getClientIdByPhoneNumber(phone).get();
        System.out.println(clientId);
        return cmiService.getbalance(clientId);
    }
}
