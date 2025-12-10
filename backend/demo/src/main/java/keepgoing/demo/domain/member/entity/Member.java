package keepgoing.demo.domain.member.entity;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Data
public class Member {
    private Long id;
    private String email;
    private String password;
    private String name;
    private int age;
    private String gender;

    // int -> double 로 변경
    private double height;
    private double weight;

    // AI 필수 필드 추가
    private String activity;
    private String goal;

    // 선택 사항
    private String role;
    private LocalDateTime createdAt;
    private String healthCondition; // 기저질환
    private String allergies;       // 알레르기
    private String dislikedFood;    // 기피음식
    private Double targetWeight;    // 목표체중
}