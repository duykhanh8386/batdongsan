package com.BTL.Springboot.dto.response.transaction;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDto {
        private String agentName;
        private double price;
        private String timeAgo;

        public TransactionDto(String agentName, double price, LocalDateTime createdAt) {
                this.agentName = agentName;
                this.price = price;
                this.timeAgo = calculateTimeAgo(createdAt);
        }

        private String calculateTimeAgo(LocalDateTime createdAt) {
                Duration duration = Duration.between(createdAt, LocalDateTime.now());

                if (duration.toMinutes() < 1) return "Vừa xong";
                if (duration.toMinutes() < 60) return duration.toMinutes() + " phút trước";
                if (duration.toHours() < 24) return duration.toHours() + " giờ trước";
                if (duration.toDays() < 7) return duration.toDays() + " ngày trước";
                if (duration.toDays() < 30) return (duration.toDays() / 7) + " tuần trước";
                if (duration.toDays() < 365) return (duration.toDays() / 30) + " tháng trước";
                return (duration.toDays() / 365) + " năm trước";
        }

}

