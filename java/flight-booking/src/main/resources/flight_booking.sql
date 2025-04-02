CREATE TABLE `flight_booking` (
      `booking_number` varchar(255) DEFAULT NULL,
      `date` date DEFAULT NULL,
      `booking_to` date DEFAULT NULL,
      `name` varchar(255) DEFAULT NULL,
      `from` varchar(255) DEFAULT NULL,
      `to` varchar(255) DEFAULT NULL,
      `booking_status` varchar(255) DEFAULT NULL,
      `booking_class` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;