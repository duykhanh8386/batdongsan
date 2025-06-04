package com.BTL.Springboot.config;

import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import com.BTL.Springboot.dto.request.auth.IntrospectRequest;
import com.BTL.Springboot.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import com.nimbusds.jose.JOSEException;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    /**
     * Phương thức giải mã token JWT.
     * - Kiểm tra tính hợp lệ của token bằng cách gọi phương thức introspect của AuthenticationService.
     * - Nếu token không hợp lệ, ném ra JwtException.
     * - Khởi tạo NimbusJwtDecoder với khóa bí mật và thuật toán HS512 nếu chưa được khởi tạo.
     * - Giải mã token và trả về đối tượng Jwt.
     *
     * @param token Token JWT cần giải mã
     * @return Đối tượng Jwt chứa thông tin đã giải mã
     * @throws JwtException Nếu token không hợp lệ hoặc có lỗi trong quá trình giải mã
     */
    @Override
    public Jwt decode(String token) throws JwtException {

        try {
            var response = authenticationService.introspect(
                    IntrospectRequest.builder().token(token).build());

            if (!response.isValid()) throw new JwtException("Token invalid");
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}