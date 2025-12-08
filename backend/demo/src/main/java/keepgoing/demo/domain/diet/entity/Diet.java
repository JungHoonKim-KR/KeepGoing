package keepgoing.demo.domain.diet.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Diet {
    private int id;
    private LocalDate date;
    private List<Food> foods;
    private double carbohydrate;
    private double protein;
    private double fat;
    private double sugars;
    private String mealTime;
//    private int member_id; // 임시
    // private Member member;
}
