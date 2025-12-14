package com.pm.authservice.payload;

import lombok.Data;

@Data
public class ApiResponse {

      private String message;

      private boolean isSuccess;

      private Object data;
}
