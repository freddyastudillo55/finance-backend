package finance.backend.bvnos.controller;

import finance.backend.bvnos.model.*;
import finance.backend.bvnos.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public UserResponseDTO createUser(@RequestBody CreateUserRequestDTO requestDTO) {
        return userService.createUser(requestDTO);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO requestDTO) {
        return userService.login(requestDTO);
    }

    @PutMapping("/update/{id}")
    public UserResponseDTO updateUser(@PathVariable String id, @RequestBody UpdateUserRequestDTO requestDTO) {
        return userService.updateUser(id, requestDTO);
    }

}
