package com.ssafy.cnnect.user.controller;

import com.ssafy.cnnect.result.ResultCode;
import com.ssafy.cnnect.result.ResultResponse;
import com.ssafy.cnnect.user.dto.JoinRequestDto;
import com.ssafy.cnnect.user.dto.LoginRequestDto;
import com.ssafy.cnnect.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/user")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @Operation(summary = "유저 회원가입")
    @PostMapping(value = "/join", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultResponse> registUser(@RequestBody JoinRequestDto joinRequestDto) {
        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS, userService.registUser(joinRequestDto)));
    }
    @Operation(summary = "유저 로그인")
    @PostMapping(value = "/login")
    public ResponseEntity<ResultResponse> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS, userService.loginUser(loginRequestDto)));
    }

    // 추후 token 기반으로 변경
    @Operation(summary = "유저 정보")
    @GetMapping(value = "/info/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultResponse> getUserInfo(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS, userService.getUser(userId)));
    }

    @Operation(summary = "refreshToken으로 accessToken 재발급")
    @GetMapping("/refreshtoken")
    public ResponseEntity<ResultResponse> reissueToken(@RequestParam String refreshToken){
        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS, userService.reissueToken(refreshToken)));
    }


}
