package com.pm.authservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponse {

      private String message;

      private boolean isSuccess;

      private Object data;
}
