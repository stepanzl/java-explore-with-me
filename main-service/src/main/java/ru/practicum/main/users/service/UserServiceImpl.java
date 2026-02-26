package ru.practicum.main.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.users.dto.NewUserRequest;
import ru.practicum.main.users.dto.UserDto;
import ru.practicum.main.users.mapper.UserMapper;
import ru.practicum.main.users.model.User;
import ru.practicum.main.users.repository.UserRepository;
import ru.practicum.main.util.PageRequestUtil;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        log.info("Create user: email={}", request.getEmail());
        User saved = userRepository.save(userMapper.toEntity(request));
        log.info("User created: id={}", saved.getId());
        return userMapper.toDto(saved);
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, int from, int size) {
        log.info("Get users: idsProvided={}, from={}, size={}", ids != null, from, size);

        Pageable pageable = PageRequestUtil.from(from, size);

        if (ids != null && ids.isEmpty()) {
            return List.of();
        }

        Page<User> page = (ids == null)
                ? userRepository.findAll(pageable)
                : userRepository.findAllByIdIn(ids, pageable);

        return page.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(long userId) {
        log.info("Delete user: id={}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }
}