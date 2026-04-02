package com.example.cinetrackerbackend.movie.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResponse<T>{

  private int pages;
  private int totalPages;
  private List<T> results;

}
