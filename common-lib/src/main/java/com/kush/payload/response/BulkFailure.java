package com.kush.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BulkFailure {

    private final int index;
    private final String reference;
    private final String reason;
}
