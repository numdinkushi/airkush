package com.kush.payload.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BulkResult<T> {

    private final List<T> created;
    private final List<BulkFailure> failed;
    private final int total;
    private final int createdCount;
    private final int failedCount;

    public static <T> BulkResult<T> of(List<T> created, List<BulkFailure> failed, int total) {
        return BulkResult.<T>builder()
                .created(created)
                .failed(failed)
                .total(total)
                .createdCount(created.size())
                .failedCount(failed.size())
                .build();
    }
}
