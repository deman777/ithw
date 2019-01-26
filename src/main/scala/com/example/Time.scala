package com.example

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Time {
  def parse(string: String, format: String): LocalDateTime = {
    LocalDateTime.parse(string, DateTimeFormatter.ofPattern(format))
  }
}
