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

    private int level;          // 현재 레벨
    private int exp;            // 현재 경험치
    private int currentPoints;  // 보유 포인트 (상점용)

    // 회원 정보 수정을 위한 비즈니스 메서드
    public void updateProfile(Double height, Double weight, String activity, String goal, Double targetWeight) {
        this.height = height;
        this.weight = weight;
        this.activity = activity;
        this.goal = goal;
        this.targetWeight = targetWeight;
    }

    // (참고) 레벨업 로직 등을 위해 Setter 대신 비즈니스 메서드 추가 추천
    public void gainExp(int amount) {
        this.exp += amount;
    }
}