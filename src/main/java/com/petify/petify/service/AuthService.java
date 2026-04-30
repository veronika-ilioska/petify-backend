package com.petify.petify.service;

import com.petify.petify.domain.Client;
import com.petify.petify.domain.Owner;
import com.petify.petify.domain.User;
import com.petify.petify.domain.UserType;
import com.petify.petify.dto.AuthResponse;
import com.petify.petify.dto.LoginRequest;
import com.petify.petify.dto.SignUpRequest;
import com.petify.petify.dto.UserDTO;
import com.petify.petify.dto.UserActivityRankingProjection;
import com.petify.petify.repo.AdminRepository;
import com.petify.petify.repo.AnalyticsRepository;
import com.petify.petify.repo.ClientRepository;
import com.petify.petify.repo.OwnerRepository;
import com.petify.petify.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final OwnerRepository ownerRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnalyticsRepository analyticsRepository;

    public AuthService(UserRepository userRepository, ClientRepository clientRepository,
                      OwnerRepository ownerRepository, AdminRepository adminRepository,
                      PasswordEncoder passwordEncoder, AnalyticsRepository analyticsRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.ownerRepository = ownerRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.analyticsRepository = analyticsRepository;
    }

    /**
     * Register a new user as CLIENT
     * All new users are registered as CLIENT by default
     * Creates both a User and a Client row in the database
     */
    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        // Check if username or email already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        logger.error(">>> SIGNUP METHOD HIT <<<");

        // Create new user as CLIENT
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getFirstName(),
            request.getLastName()
        );

        User savedUser = userRepository.save(user);
        logger.info("User saved successfully - ID: {}, Username: {}", savedUser.getUserId(), savedUser.getUsername());

        try {
            // Create a corresponding Client row
            Client client = new Client(savedUser);
            Client savedClient = clientRepository.save(client);
            logger.info("Client saved successfully - Client linked to User ID: {}", savedClient.getUser().getUserId());
        } catch (Exception e) {
            logger.error("Failed to create client for user ID: {}", savedUser.getUserId(), e);
            throw new RuntimeException("Failed to create client profile: " + e.getMessage(), e);
        }

        return new AuthResponse(
            savedUser.getUserId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            UserType.CLIENT,
            false  // New users are not verified by default
        );
    }

    /**
     * Login user with username and password
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername());

        if (user.isEmpty()) {
            logger.warn("Login failed: no user found for identifier: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }

        User foundUser = user.get();

        // Debug logging to see foundUser details
        logger.info("User found - ID: {}, Username: {}, Email: {}, FirstName: {}, LastName: {}",
            foundUser.getUserId(),
            foundUser.getUsername(),
            foundUser.getEmail(),
            foundUser.getFirstName(),
            foundUser.getLastName());

        // Verify password (supports legacy plain-text passwords and upgrades them)
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), foundUser.getPassword());
        if (!passwordMatches) {
            if (request.getPassword() != null && request.getPassword().equals(foundUser.getPassword())) {
                foundUser.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(foundUser);
                logger.info("Upgraded legacy password hash for user: {}", foundUser.getUsername());
            } else {
                logger.warn("Login failed: password mismatch for user: {}", foundUser.getUsername());
                throw new RuntimeException("Invalid username or password");
            }
        }

        logger.info("Password verified successfully for user: {}", foundUser.getUsername());

        // Check if user is blocked (both clients and owners can be blocked)
        if (clientRepository.existsById(foundUser.getUserId())) {
            Client client = clientRepository.findById(foundUser.getUserId()).orElse(null);
            if (client != null && client.isBlocked()) {
                logger.warn("❌ Login attempt by blocked user: {}", foundUser.getUsername());
                throw new RuntimeException("Your account has been blocked. Reason: " + (client.getBlockedReason() != null ? client.getBlockedReason() : "No reason provided"));
            }
        }

        // Determine user type by checking tables in order: ADMIN -> OWNER -> CLIENT
        UserType userType = UserType.CLIENT;

        if (adminRepository.existsById(foundUser.getUserId())) {
            userType = UserType.ADMIN;
            logger.info(" User is ADMIN");
        } else if (ownerRepository.existsById(foundUser.getUserId())) {
            userType = UserType.OWNER;
            logger.info(" User is OWNER");
        } else {
            logger.info(" User is CLIENT");
        }

        // Check if user is verified (in top 10 most active users)
        boolean isVerified = isUserInTopActive(foundUser.getUserId());
        logger.info("User {} verification status: {}", foundUser.getUsername(), isVerified);

        return new AuthResponse(
            foundUser.getUserId(),
            foundUser.getUsername(),
            foundUser.getEmail(),
            foundUser.getFirstName(),
            foundUser.getLastName(),
            userType,
            isVerified
        );
    }


    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.info("===== GET ALL USERS SERVICE =====");
        List<UserDTO> users = userRepository.findAll()
            .stream()
            .map(user -> {
                UserDTO dto = this.mapToDTO(user);
                logger.debug("✓ Mapped user {} with type: {}", user.getUserId(), dto.getUserType());
                return dto;
            })
            .collect(Collectors.toList());
        logger.info("✓ Successfully mapped {} users", users.size());
        return users;
    }




    /**
     * Map User entity to UserDTO
     */
    private UserDTO mapToDTO(User user) {
        // Determine user type by checking tables in order: ADMIN -> OWNER -> CLIENT
        String userType = "CLIENT";
        boolean isBlocked = false;
        String blockedReason = "";

        if (adminRepository.existsById(user.getUserId())) {
            userType = "ADMIN";
            logger.debug("✓ User {} is ADMIN", user.getUserId());
        } else if (ownerRepository.existsById(user.getUserId())) {
            userType = "OWNER";
            logger.debug("✓ User {} is OWNER", user.getUserId());

            // Check if owner is blocked (via their client record)
            if (clientRepository.existsById(user.getUserId())) {
                Client client = clientRepository.findById(user.getUserId()).orElse(null);
                if (client != null) {
                    isBlocked = client.isBlocked();
                    blockedReason = client.getBlockedReason() != null ? client.getBlockedReason() : "";
                    if (isBlocked) {
                        logger.debug("⚠ Owner {} is BLOCKED. Reason: {}", user.getUserId(), blockedReason);
                    }
                }
            }
        } else {
            logger.debug("✓ User {} is CLIENT", user.getUserId());

            // Check if client is blocked
            if (clientRepository.existsById(user.getUserId())) {
                Client client = clientRepository.findById(user.getUserId()).orElse(null);
                if (client != null) {
                    isBlocked = client.isBlocked();
                    blockedReason = client.getBlockedReason() != null ? client.getBlockedReason() : "";
                    if (isBlocked) {
                        logger.debug("⚠ User {} is BLOCKED. Reason: {}", user.getUserId(), blockedReason);
                    }
                }
            }
        }

        // Check if user is verified (in top 10 most active users)
        boolean isVerified = isUserInTopActive(user.getUserId());

        UserDTO dto = new UserDTO(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCreatedAt(),
            userType,
            isBlocked,
            blockedReason,
            isVerified
        );

        logger.debug("✓ Created UserDTO for {} with type: {}, blocked: {}, verified: {}", user.getUsername(), userType, isBlocked, isVerified);
        return dto;
    }

    /**
     * Check if a user is in the top active users
     */
    private boolean isUserInTopActive(Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyDaysAgo = now.minusDays(30);

            List<UserActivityRankingProjection> topUsers = analyticsRepository.getTopActiveUsers(thirtyDaysAgo, now);
            return topUsers.stream().anyMatch(u -> u.getUserId().equals(userId));
        } catch (Exception e) {
            logger.error("Error checking if user is in top active: {}", e.getMessage());
            return false;
        }
    }
}
