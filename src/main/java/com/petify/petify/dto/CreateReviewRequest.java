package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateReviewRequest {
    private Integer rating;
    private String comment;

    public CreateReviewRequest() {
    }

    public CreateReviewRequest(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

}
