package com.urbanmicrocad.common;

import com.urbanmicrocad.common.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successResponseUsesFrontendCompatibleCode() {
        ApiResponse<String> response = ApiResponse.ok("ok");

        assertThat(response.code()).isEqualTo(200);
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ok");
    }
}
