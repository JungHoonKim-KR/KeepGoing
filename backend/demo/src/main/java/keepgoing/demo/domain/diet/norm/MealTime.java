package keepgoing.demo.domain.diet.norm;

import lombok.Getter;

@Getter
public enum MealTime {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식");
    private final String mealType;
    MealTime(String mealType) {
        this.mealType = mealType;
    }
}