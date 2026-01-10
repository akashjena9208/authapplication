//package com.akash.auth.controller;
//
//import com.akash.auth.dto.UserDto;
//import com.akash.auth.service.UserService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/v1/users")
//@AllArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    //create user api
//    @PostMapping
//    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
//    }
//
//    // get all user api
//    @GetMapping
//    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
//        return ResponseEntity.ok(userService.getAllUsers());
//    }
//
//    // get user by email
//    @GetMapping("/email/{email}")
//    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
//        return ResponseEntity.ok(userService.getUserByEmail(email));
//    }
//
//    //delete user
//    //api/v1/users/{userId}
//    @DeleteMapping("/{userId}")
//    public void deleteUser(@PathVariable("userId") String userId) {
//        userService.deleteUser(UUID.fromString(userId));
//    }
//
//    //update user
//    //api/v1/users/{userId}
//    @PutMapping("/{userId}")
//    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable("userId") String userId) {
//        return ResponseEntity.ok(userService.updateUser(userDto, userId));
//    }
//
//    //get user by id
//    //api/v1/users/{userId}
//    @GetMapping("/{userId}")
//    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") String userId) {
//        return ResponseEntity.ok(userService.getUserById(userId));
//    }
//
//}
package com.akash.auth.controller;

import com.akash.auth.dto.UserDto;
import com.akash.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//http://localhost:8082/api/v1/users
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // =========================
    // CREATE USER
    // =========================
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // =========================
    // GET USER BY ID
    // =========================
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // =========================
    // GET USER BY EMAIL
    // =========================
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // =========================
    // GET ALL USERS
    // =========================
    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // =========================
    // UPDATE USER
    // =========================
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String userId,
            @RequestBody UserDto userDto
    ) {
        return ResponseEntity.ok(userService.updateUser(userDto, userId));
    }

    // =========================
    // DELETE USER
    // =========================
//    @DeleteMapping("/{userId}")
//    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
//        userService.deleteUser(UUID.fromString(userId));
//          return ResponseEntity.noContent().build();
//    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(
                Map.of("message", "User deleted successfully")
        );
    }

}
