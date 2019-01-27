package com.example

import java.time.LocalDateTime

class Reminders {
}

case class Remind(duration: java.time.Duration, message: Any)
case class Reminder(timestamp: LocalDateTime, message: Any)
case object ClearReminders