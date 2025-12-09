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
    public static MealTime fromMealType(String mealType) {
        if (mealType == null) {
            throw new IllegalArgumentException("MealType must not be null");
        }

        String cleanMealType = mealType.trim();

        for (MealTime time : MealTime.values()) {
            if (time.mealType.equals(cleanMealType)) {
                return time;
            }
        }
        throw new IllegalArgumentException("No MealTime constant with mealType: " + mealType);
    }
}