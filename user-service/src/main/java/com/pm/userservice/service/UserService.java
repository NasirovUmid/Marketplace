package com.pm.userservice.service;

import com.pm.commonevents.UserEvent;
import com.pm.commonevents.UserNotificationEvent;
import com.pm.commonevents.enums.UserEventType;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.InternalProblemException;
import com.pm.commonevents.exception.NotFoundException;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.dto.UserUpdateRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KafkaNotificationEventProducer kafkaNotificationEventProducer;
    private final FileService fileService;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);


    public Page<User> userList(int page, String sort, int size, String filter) {

        Specification<User> spec = ((root, query, criteriaBuilder) -> {

            if (filter == null || filter.isEmpty()) {

                return criteriaBuilder.conjunction();
            }

            if (filter.contains(">")) {
                String[] parts = filter.split(">");
                return criteriaBuilder.greaterThan(root.get(parts[0]), LocalDate.parse(parts[1]));
            }

            if (filter.contains("<")) {
                String[] parts = filter.split("<");
                return criteriaBuilder.lessThan(root.get(parts[0]), LocalDate.parse(parts[1]));
            }

            if (filter.contains(":")) {
                String[] parts = filter.split(":");
                return criteriaBuilder.equal(root.get(parts[0]), LocalDate.parse(parts[1]));
            }

            return null;

        });


        String sorting = sort.substring(",".length()).equals("asc") ? "asc" : "desc";
        String sortingField = sorting.substring(0, ",".length());

        Sort sort1 = sorting.equals("asc") ? Sort.by(sortingField).ascending() : Sort.by(sortingField).descending();
        return userRepository.findAll(spec, PageRequest.of(page,size,sort1));

    }

    public void userCreating(UserEvent userEvent) {

        if (userRepository.findUserByEmail(userEvent.email()))
            throw new AlreadyExistsException("USER CREATE: user with Email = [ " + userEvent.email() + " ] ");

        userRepository.save(new User(userEvent.id(), null,
                userEvent.email(), userEvent.phoneNumber(), null, "C:\\Java\\27\\monke.jpg", userEvent.birthDate(), userEvent.timeOfCreation()));

        kafkaNotificationEventProducer.sendingNotificationEvent(
                new UserNotificationEvent(userEvent.id(), userEvent.email(), userEvent.phoneNumber(), UserEventType.USER_CREATED.name(), Instant.now()));

    }

    public UserResponseDTO userUpdating(UserUpdateRequestDTO updateRequestDTO) {

        User user = getUser(updateRequestDTO.getEmail());

        if (user == null)
            throw new NotFoundException("USER UPDATE: User with email = [ " + updateRequestDTO.getEmail() + " ] ");

        if (updateRequestDTO.getFullName().isPresent()) {
            user.setFullName(updateRequestDTO.getFullName().toString());
        }

        // IF ITS NULL MAYBE USER WANT TO REMOVE BIO?
        user.setBio(updateRequestDTO.getBio().toString());


        // I WILL CHANGE USEREVENTS AND WILL MAKE OBLIGATORY IN REGISTRATION WRITE BITRHDATE SO HERE SHOULD NOT BE CHANCE TO CHANGE IT
        //    user.setBirthDate(LocalDate.parse(updateRequestDTO.getBirthDate().toString())); I DID AT THE MOMENT

        // I WANNA MAKE PHONE NUMBER OBLIGATORY SO IN FUTURE I CAN USE IN TWILIO
        if (updateRequestDTO.getPhoneNumber().isPresent()) {
            user.setPhoneNumber(updateRequestDTO.getPhoneNumber().get());
        }


        if (updateRequestDTO.getImage() != null) { // does user want to change photo ?

            if (user.getImageUrl().startsWith(user.getId().toString())) {

                fileService.deleteImageFromBucket(user.getImageUrl());

            }
            String objectKey = fileService.uploadFileToMinio(user.getId(), updateRequestDTO.getImage());

            if (objectKey == null) { // if uploading was successful

                logger.error("PROBLEM WITH SAVING FILE = {}", updateRequestDTO.getImage().getOriginalFilename());
                throw new InternalProblemException(
                        "The Image file could not saved = [ " + updateRequestDTO.getImage().getOriginalFilename().
                                substring(updateRequestDTO.getImage().getOriginalFilename().lastIndexOf('_') + 1));
            }
            user.setImageUrl(objectKey);

        }

        User editedUser = userRepository.save(user);

        kafkaNotificationEventProducer.sendingNotificationEvent(
                new UserNotificationEvent(editedUser.getId(), editedUser.getEmail(), editedUser.getPhoneNumber().toString(),
                        UserEventType.USER_UPDATED.name(), editedUser.getRegisteredDate()));

        return new UserResponseDTO(editedUser.getEmail(), true,
                editedUser.getImageUrl().substring(editedUser.getImageUrl().lastIndexOf('_') + 1));

    }

    public User getUser(String email) {

        Optional<User> validateUser = userRepository.findByEmail(email);

        return validateUser.orElse(null);

    }


}
