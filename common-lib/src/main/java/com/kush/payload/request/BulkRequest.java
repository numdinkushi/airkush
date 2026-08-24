package com.kush.payload.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkRequest<T> {

    @NotEmpty(message = "At least one item is required")
    @Size(max = 500, message = "Cannot import more than 500 items at once")
    @Valid
    private List<T> items;
}
