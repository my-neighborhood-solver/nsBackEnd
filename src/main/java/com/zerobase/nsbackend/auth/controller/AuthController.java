package com.zerobase.nsbackend.auth.controller;

import com.zerobase.nsbackend.auth.dto.Auth.SignIn;
import com.zerobase.nsbackend.auth.dto.Auth.SignInResponse;
import com.zerobase.nsbackend.auth.dto.Auth.SignUp;
import com.zerobase.nsbackend.auth.dto.Auth.SignUpResponse;
import com.zerobase.nsbackend.auth.dto.KakaoTokenResponse;
import com.zerobase.nsbackend.auth.dto.KakaoUserInfoResponse;
import com.zerobase.nsbackend.auth.external.KakaoTokenFeign;
import com.zerobase.nsbackend.auth.external.KakaoUserFeign;
import com.zerobase.nsbackend.auth.security.TokenProvider;
import com.zerobase.nsbackend.auth.service.AuthService;
import com.zerobase.nsbackend.member.domain.Member;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenProvider tokenProvider;
    @Autowired
    private KakaoTokenFeign kakaoTokenFeign;
    @Autowired
    private KakaoUserFeign kakaoUserFeign;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String REDIRECT_URI;
    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String GRANT_TYPE;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String CLIENT_ID;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> createMember(@RequestBody @Valid SignUp request){
        Member member = this.authService.register(request);
        SignUpResponse response = SignUpResponse.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> logInMember(@RequestBody @Valid SignIn request){
        Member member = this.authService.authenticate(request);
        String token = this.tokenProvider.generateToken(member.getEmail(), member.getAuthority());
        SignInResponse response = SignInResponse.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .token(token)
            .build();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/kakao/callback")
    @ResponseBody
    public ResponseEntity<SignInResponse> kakaoOauth(@RequestParam("code") String code) {
        KakaoTokenResponse kakaoTokenResponse = this.kakaoTokenFeign.call(GRANT_TYPE, CLIENT_ID,
            REDIRECT_URI, code);
        KakaoUserInfoResponse userInfo = this.kakaoUserFeign.call(
            "Bearer " + kakaoTokenResponse.getAccess_token());
        String name = userInfo.getKakao_account().getProfile().getNickname();
        Member member = this.authService.kakaoRegister(userInfo.getKakao_account().getEmail(),
            name);
        String token = this.tokenProvider.generateToken(member.getEmail(),member.getAuthority());
        SignInResponse response = SignInResponse.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .token(token)
            .build();
        return ResponseEntity.ok(response);
    }

}
