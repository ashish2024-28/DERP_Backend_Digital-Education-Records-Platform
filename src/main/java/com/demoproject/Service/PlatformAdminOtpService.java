package com.demoproject.Service;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.Entity.PlatformAdminDeleteChallenge;
import com.demoproject.Repository.PlatformAdminDeleteChallengeRepository;
import com.demoproject.Repository.UniversityRepo;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PlatformAdminOtpService {

    private static final ParameterizedTypeReference<ApiResponse<String>> SEND_RESPONSE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<Boolean>> VERIFY_RESPONSE =
            new ParameterizedTypeReference<>() {};

    private final RestTemplate restTemplate = new RestTemplate();
    private final PlatformAdminDeleteChallengeRepository challengeRepository;
    private final UniversityRepo universityRepository;

    @Value("${derp.otp-api-base-url:}")
    private String otpApiBaseUrl;

    @Transactional
    public void sendDeleteOtp(Long universityId, String domain, String email, String reason) {
        universityRepository.findByDomainAndId(domain, universityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "University not found for this domain and ID"));
        if (reason == null || reason.trim().length() < 10 || reason.trim().length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Deletion reason must be between 10 and 1000 characters");
        }
        challengeRepository.findTopByAdminEmailAndConsumedAtIsNullOrderByCreatedAtDesc(email)
                .filter(challenge -> challenge.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60)))
                .ifPresent(challenge -> {
                    throw new ResponseStatusException(
                            HttpStatus.TOO_MANY_REQUESTS, "Wait before requesting another deletion OTP");
                });
        ApiResponse<String> response = post("/otp/send", email, null, SEND_RESPONSE);
        if (response == null || !response.isSuccess()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OTP provider did not accept the request");
        }
        challengeRepository.deleteByAdminEmailAndConsumedAtIsNull(email);
        PlatformAdminDeleteChallenge challenge = new PlatformAdminDeleteChallenge();
        challenge.setAdminEmail(email);
        challenge.setUniversityId(universityId);
        challenge.setUniversityDomain(domain);
        challenge.setReason(reason.trim());
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        challengeRepository.save(challenge);
    }

    public void verifyDeleteOtp(String email, String otp) {
        ApiResponse<Boolean> response = post("/otp/verify", email, otp, VERIFY_RESPONSE);
        if (response == null || !response.isSuccess() || !Boolean.TRUE.equals(response.getData())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is invalid or expired");
        }
    }

    private <T> ApiResponse<T> post(
            String path,
            String email,
            String otp,
            ParameterizedTypeReference<ApiResponse<T>> responseType) {
        if (otpApiBaseUrl == null || otpApiBaseUrl.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The platform OTP provider is not configured on the backend");
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(otpApiBaseUrl.replaceAll("/+$", ""))
                .path(path)
                .queryParam("email", email);
        if (otp != null) {
            uriBuilder.queryParam("otp", otp);
        }
        URI uri = uriBuilder.build().encode().toUri();

        try {
            ResponseEntity<ApiResponse<T>> response = restTemplate.exchange(
                    uri, HttpMethod.POST, HttpEntity.EMPTY, responseType);
            return response.getBody();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Unable to verify with the platform OTP provider", exception);
        }
    }
}
