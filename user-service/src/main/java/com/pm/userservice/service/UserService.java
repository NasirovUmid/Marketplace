package com.pm.userservice.service;

import com.pm.commonevents.UserEvent;
import com.pm.commonevents.UserNotificationEvent;
import com.pm.commonevents.enums.UserEventType;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.InternalProblemException;
import com.pm.userservice.dto.AdminFilterDto;
import com.pm.userservice.dto.UserProfileDto;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.dto.UserUpdateRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.filter.SpecFilter;
import com.pm.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KafkaNotificationEventProducer kafkaNotificationEventProducer;
    private final FileService fileService;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final String imageUrl = "http://localhost:9000/";


    @Transactional(readOnly = true)
    public Page<User> userList(int page, String sort, int size, String email, Instant birthDateFrom, Instant birthDateTo, Instant registerDateFrom, Instant registerDateTo) {

        Specification<User> spec = SpecFilter.byFilter(new AdminFilterDto(email, birthDateFrom, birthDateTo, registerDateFrom, registerDateTo));

        String[] sortingArr = sort.split(",");

        Sort sort1 = sortingArr[1].equals("asc") ? Sort.by(sortingArr[0]).ascending() : Sort.by(sortingArr[0]).descending();
        return userRepository.findAll(spec, PageRequest.of(page, size, sort1));
    }

    @Transactional
    public void userCreating(UserEvent userEvent) {

        if (userRepository.existsByEmail(userEvent.email()))
            throw new AlreadyExistsException("USER CREATE: user with Email = [ " + userEvent.email() + " ] ");

        userRepository.save(new User(userEvent.id(), null,
                userEvent.email(), userEvent.phoneNumber(), null, "C:\\Java\\27\\monke.jpg", userEvent.birthDate(), userEvent.timeOfCreation()));

        kafkaNotificationEventProducer.sendingNotificationEvent(
                new UserNotificationEvent(userEvent.id(), userEvent.email(), userEvent.phoneNumber(), UserEventType.USER_CREATED.name(), Instant.now()));

    }

    @Transactional
    public UserResponseDTO userUpdating(UserUpdateRequestDTO updateRequestDTO, MultipartFile multipartFile) {

        User user = userRepository.findByEmail(updateRequestDTO.email());

        if (updateRequestDTO.fullName() != null) {
            user.setFullName(updateRequestDTO.fullName());
        }

        if (updateRequestDTO.bio() != null) {
            user.setBio(updateRequestDTO.bio());
        }

        if (updateRequestDTO.phoneNumber() != null) {
            user.setPhoneNumber(updateRequestDTO.phoneNumber());
        }

        if (multipartFile != null && !multipartFile.isEmpty()) {

            String extension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());

            String newFileUrl = UUID.randomUUID() + (extension != null ? "." + extension : "");

            fileService.deleteImageFromBucket(user.getImageUrl());

            String objectKey = fileService.uploadFileToMinio(user.getId(), multipartFile, newFileUrl);

            if (objectKey == null) { // if uploading was successful

                logger.error("PROBLEM WITH SAVING FILE = {}", multipartFile.getOriginalFilename());
                throw new InternalProblemException(
                        "The Image file could not saved = [ " + multipartFile.getOriginalFilename().
                                substring(multipartFile.getOriginalFilename().lastIndexOf('_') + 1));
            }
            user.setImageUrl(objectKey);
        }

        User editedUser = userRepository.save(user);

        kafkaNotificationEventProducer.sendingNotificationEvent(
                new UserNotificationEvent(editedUser.getId(), editedUser.getEmail(), editedUser.getPhoneNumber(),
                        UserEventType.USER_UPDATED.name(), editedUser.getRegisteredDate()));

        editedUser.setImageUrl(imageUrl + editedUser.getImageUrl());

        return UserResponseDTO.from(editedUser);
    }

    public UserProfileDto getUserProfile(UUID userId) {

        User user = userRepository.findUserById(userId);

        user.setImageUrl(imageUrl + user.getImageUrl());

        return UserProfileDto.from(user);

    }

}
