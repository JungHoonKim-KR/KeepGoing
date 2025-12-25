from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import json
import os
import base64
from openai import OpenAI
from dotenv import load_dotenv
import io
from PIL import Image

# 1. 환경 변수 로드
load_dotenv()
API_KEY = os.getenv("OPENAI_API_KEY")

# SSAFY GMS 설정 (또는 일반 OpenAI API 사용 시 base_url 제거)
client = OpenAI(
    api_key=API_KEY,
    base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
)

app = FastAPI()

# 2. CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# --- DTO (Data Transfer Objects) ---

class UserProfile(BaseModel):
    goal: str


class DailyLog(BaseModel):
    date: str
    dietSummary: str


class DietRequest(BaseModel):
    userProfile: UserProfile
    dailyLog: DailyLog


class DietGenerationRequest(BaseModel):
    duration: int
    exercise: str
    sleep: str
    water: str
    meals: int
    favorite: str


class BodyScanRequest(BaseModel):
    height: float
    weight: float


# --- Helper Function ---
def encode_image(image_file):
    return base64.b64encode(image_file.read()).decode('utf-8')


# =========================================================
# 1. [Update] 분석 API (데일리 리포트)
# EndPoint: /api/diet/analyze
# 설명: 점수, 피드백뿐만 아니라 총 칼로리와 추천 운동 3가지를 랜덤으로 제안
# =========================================================
@app.post("/api/diet/analyze")
def analyze_diet(request: DietRequest):
    print(f"🧐 [식단 분석 요청] {request.dailyLog.date}")

    try:
        system_instruction = f"""
        너는 'AI 헬스 트레이너'야. 사용자의 오늘 식단을 평가해줘. JSON 포맷 준수.

        [필수 포함 항목]
        1. score (0~100 정수): 식단 점수
        2. rank (String): 등급 (S/A/B/C/F)
        3. dailyTitle (String): 한줄 요약 제목
        4. oneLineSummary (String): 구체적인 총평
        5. insights (Array): [{{ "type": "positive/negative", "iconType": "muscle/warning/balance/water", "title": "...", "description": "..." }}]
        6. miningKeywords (Array of Strings): 음식 키워드 5개 (예: ["닭가슴살", "사과"])

        [New! 칼로리 및 운동 처방]
        7. totalCalories (Integer): 오늘 식단의 총 추정 칼로리 (kcal)
        8. recommendedExercises (Array): 섭취한 칼로리와 영양소를 고려하여 추천하는 운동 3가지.
           - 단순히 걷기/뛰기만 하지 말고, '스쿼트', '버피테스트', '계단 오르기', '요가', '줄넘기', '플랭크' 등 다양한 운동 중 3개를 랜덤하게 선정.
           - 각 객체 형태: {{ "name": "운동명", "time": "시간(분, 정수)", "emoji": "이모지" }}

        [응답 예시]
        {{
          "score": 78, 
          "rank": "B", 
          "dailyTitle": "탄수화물이 조금 과했던 하루", 
          "oneLineSummary": "점심은 좋았지만 저녁에 면 요리를 드셨군요. 유산소 운동이 필요합니다.",
          "insights": [ {{ "type": "warning", "iconType": "balance", "title": "탄수화물 과다", "description": "저녁 식사의 비중을 조금 줄여보세요." }} ],
          "miningKeywords": ["라면", "김밥", "아이스크림", "단무지", "콜라"],
          "totalCalories": 2100,
          "recommendedExercises": [
              {{ "name": "버피테스트", "time": 20, "emoji": "🏋️" }},
              {{ "name": "계단 오르기", "time": 40, "emoji": "🪜" }},
              {{ "name": "실내 자전거", "time": 50, "emoji": "🚴" }}
          ]
        }}
        """

        user_message = f"오늘 식단: {request.dailyLog.dietSummary}\n목표: {request.userProfile.goal}"

        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": user_message}
            ],
            temperature=0.7,  # 창의적인 운동 추천을 위해 온도 약간 높임
            response_format={"type": "json_object"}
        )
        print("식단 분석 응답 결과 : ", json.loads(completion.choices[0].message.content))
        return json.loads(completion.choices[0].message.content)

    except Exception as e:
        print(f"❌ 분석 에러: {e}")
        raise HTTPException(status_code=500, detail="AI 분석 실패")


# =========================================================
# 2. [Update] AI 맞춤형 식단 생성 API
# EndPoint: /api/diet/generate
# 설명: 3~14일치 식단표 생성 (자바 List 호환성 수정 완료)
# =========================================================
@app.post("/api/diet/generate")
def generate_diet_plan(request: DietGenerationRequest):
    print(f"📅 [식단 생성 요청] 기간: {request.duration}일, 목표: {request.favorite}")

    try:
    # [들여쓰기 중요] try 내부이므로 4칸 들여쓰기 시작
        system_instruction = f"""
        너는 전문 영양사야. 사용자의 생활 습관 설문을 바탕으로 {request.duration}일치 식단표를 짜줘.

        [사용자 정보]
        - 운동: {request.exercise} (운동량에 따라 칼로리를 조절할 것)
        - 수면: {request.sleep}
        - 선호 음식: {request.favorite} (건강하게 변형해서 포함할 것)

        [중요 지침]
        1. "cal" (칼로리) 필드는 절대 1500으로 고정하지 말고, 메뉴 구성에 따라 현실적으로 계산된 정수값(예: 1450, 1620)을 넣어.
        2. 메뉴의 키 값은 반드시 영어 소문자("breakfast", "lunch", "dinner")를 사용해.
        3. [중요] 사용자가 입력한 '선호 음식(favorite_food)'은 7일 중 "단 한 끼"에만 포함시켜야 해. (희소성 유지)
        4. '선호 음식'이 포함된 날짜만 "isIncludeFavorite": true로 설정하고, 나머지 날짜는 무조건 false로 설정해.

        5. [핵심 로직] "isIncludeFavorite" 판별 기준:d
        - 단순히 재료 이름이 같다고 true로 설정하지 마.
        - 예: 선호 음식이 "치킨(Fried Chicken)"일 때, "닭가슴살 샐러드(Chicken Breast Salad)"는 다이어트 식단이므로 false여야 함.
        - 선호 음식은 '보상(Cheat Meal)' 개념의 메뉴로 구성하고, 그 메뉴가 들어간 날만 true로 마킹해.

        6. 선호 음식이 포함된 날의 식단 예시:
        - 입력값이 "치킨"이라면, 해당 날짜의 저녁 메뉴를 "양념 치킨" 혹은 "후라이드 치킨" 등으로 명확히 '특식'처럼 구성해.
        7. 특식이 너무 초반에 몰리지 않도록 기간 내에 고르게 분포시켜줘. 예를 들어 7일이면 적어도 5일 이후에 배치되게 해줘

        8. JSON 출력 형식을 엄격히 지켜.

        [응답 포맷 (JSON Only)]
        반드시 최상위 키 "plans" 안에 배열을 담아서 반환해.
        {{
            "plans": [
                {{ 
                    "day": 1,
                    "menu": {{
                        "breakfast": "오트밀과 블루베리 (약 350kcal)",
                        "lunch": "닭가슴살 샐러드와 고구마 (약 500kcal)",
                        "dinner": "연어 스테이크와 아스파라거스 (약 600kcal)"
                    }},
                    "cal": 1450,
                    "difficulty": "EASY",
                    "quest": "식전 물 한 컵 마시기"
                    "isIncludeFavorite": true
                }},
                {{
                    "day": 2,
                    "menu": {{
                        "breakfast": "그릭 요거트",
                        "lunch": "현미밥과 불고기",
                        "dinner": "두부 쉐이크"
                    }},
                    "cal": 1620,
                    "difficulty": "NORMAL",
                    "quest": "스쿼트 20회 하기"
                    "isIncludeFavorite": false
                }}
            ]
        }}
        """
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": "식단표를 만들어주세요."}
            ],
            temperature=0.7,
            response_format={"type": "json_object"}
        )

        result = json.loads(completion.choices[0].message.content)

        # 자바 호환성을 위해 리스트만 추출하여 반환
        if "plans" in result:
            return result["plans"]

        # plans 키가 없을 경우 값 중 리스트 탐색
        for value in result.values():
            if isinstance(value, list):
                return value

        # 리스트가 없으면 빈 리스트 반환 (서버 에러 방지)
        return []

    except Exception as e:
        print(f"❌ 식단 생성 에러: {e}")
        # 에러 시 더미 데이터 반환
        return [
            {"day": i + 1, "menu": "생성 실패(재시도 필요)", "cal": 0, "difficulty": "EASY", "quest": "잠시 후 다시 시도해주세요"}
            for i in range(request.duration)
        ]


def resize_image(image_bytes, max_size=768):
    image = Image.open(io.BytesIO(image_bytes))

    if image.mode in ("RGBA", "P"):
        image = image.convert("RGB")

    image.thumbnail((max_size, max_size))

    buffer = io.BytesIO()
    image.save(buffer, format="JPEG", quality=70)  # 🔥 핵심
    return buffer.getvalue()



# =========================================================
# 3. 음식 사진 스캔 (칼로리 분석) API
# EndPoint: /api/diet/scan
# =========================================================
@app.post("/api/diet/scan")
async def scan_food_image(file: UploadFile = File(...)):
    print(f"📸 [음식 스캔 요청] 파일명: {file.filename}")

    try:
        # 1. 파일 읽기
        contents = await file.read()

        # 2. [핵심] 리사이징 수행! (용량 줄이기)
        resized_contents = resize_image(contents)

        # 3. Base64 인코딩
        base64_image = base64.b64encode(resized_contents).decode('utf-8')

        system_instruction = """
        너는 'AI 칼로리 측정기'이자 '식단 코치'야.
        사진 속 음식을 분석해서 칼로리뿐만 아니라
        다이어트 관점에서의 특징을 한 줄로 설명해줘.

        [응답 포맷 (JSON Only)]
        {
            "name": "음식 이름 (예: 라면)",
            "emoji": "🍜",
            "calories": 0,
            "dietTip": "이 음식의 식단 관련 특징 한 줄 (다이어트/건강 관점)",
            "exercise": {
                "running": 0,
                "walking": 0,
                "swimming": 0
            }
        }

        [주의]
        - dietTip은 1문장
        - 겁주지 말고 코치처럼 부드럽게
        - '피하세요' 보다는 '줄이는 것이 좋아요' 같은 톤
        - 너무 길지 않고 중요한 내용만 쏙 들어간 내용
        """

        # 4. OpenAI API 호출
        completion = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": system_instruction},
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": "이 음식의 칼로리를 분석해줘."},
                        {
                            "type": "image_url",
                            "image_url": {"url": f"data:image/jpeg;base64,{base64_image}"}
                        }
                    ]
                }
            ],
            max_tokens=500,
            response_format={"type": "json_object"}
        )

        # 5. 결과 반환
        return json.loads(completion.choices[0].message.content)

    except Exception as e:
        print(f"❌ 이미지 분석 에러: {e}")
        return {
            "name": "분석 실패",
            "emoji": "❌",
            "calories": 0,
            "exercise": {"running": 0, "walking": 0, "swimming": 0}
        }


# =========================================================
# 4. RPG 바디 스캔
# EndPoint: /api/body/scan
# 설명: 키/몸무게 기반 RPG 캐릭터 생성
# =========================================================
# 1. 모델은 그대로 (Optional 허용)
class BodyScanRequest(BaseModel):
    height: float
    weight: float
    age: int
    gender: str
    activity: str
    goal: str | None = None
    sleep: str | None = None
    water: str | None = None
    meals: int | None = None
    favorite: str | None = None


# 2. 바디 스캔 로직 (설문 없을 때 대응 버전)
@app.post("/api/body/scan")
def body_scan(request: BodyScanRequest):
    print(f"🧬 [간편 바디 스캔] {request.gender}, {request.age}세, 활동량:{request.activity}")

    # --- 프롬프트: 데이터가 부족하면 '추론'하라고 지시 ---
    system_prompt = """
    당신은 통찰력 있는 'AI 신체 분석가'입니다. 
    제공된 신체 정보(키, 몸무게, 나이, 활동량)를 바탕으로 사용자의 상태를 분석하여 JSON으로 반환하세요.

    [중요 지침]
    **수면, 물, 식습관 정보가 'None'이나 'Unknown'으로 들어올 수 있습니다.**
    이 경우, 입력된 BMI(체질량지수)와 활동량(Activity), 나이를 기반으로 **가장 개연성 있는 습관을 추론**하여 분석을 채우세요.
    (예: 활동량이 적고 BMI가 높음 -> '운동 부족' 및 '관절' 위험 추론)

    [분석 항목]
    1. healthTier: 건강 등급 (입문자/아마추어/프로/월드클래스 중 택1)
    2. vulnerableParts: 취약 부위 1~2곳 (영어 대문자: HEAD, HEART, STOMACH, LIVER, KNEE, MUSCLE, SKIN)
       - 과체중 -> KNEE
       - 고령 -> JOINT
       - 저체중/근육부족 -> MUSCLE
    3. prediction: 30일 후 예상 변화 (위트 있게)
    4. healthScore: 5가지 지표 (0~100점). 정보가 없으면 신체 스펙으로 추정.

    [JSON 포맷]
    {
        "bmi": float,
        "title": "한 줄 별명 (예: 잠재력만 높은 휴먼)",
        "healthTier": "문자열",
        "vulnerableParts": ["KNEE"],
        "prediction": "예측 문구",
        "healthScore": {
            "muscle": 0~100,
            "endurance": 0~100,
            "recovery": 0~100,
            "nutrition": 0~100,
            "metabolism": 0~100
        },
        "tags": ["#추정태그1", "#추정태그2"],
        "actionTip": "조언"
    }
    """

    # 정보가 없을 때를 대비한 문자열 처리
    user_sleep = request.sleep if request.sleep else "정보 없음(추론 필요)"
    user_water = request.water if request.water else "정보 없음(추론 필요)"
    user_meals = f"{request.meals}끼" if request.meals else "정보 없음"

    user_prompt = f"""
    [사용자 데이터]
    - 신체: {request.height}cm / {request.weight}kg ({request.age}세, {request.gender})
    - 활동량: {request.activity}
    - 목표: {request.goal}
    - (참고) 수면: {user_sleep}
    - (참고) 물섭취: {user_water}
    - (참고) 식사: {user_meals}
    """

    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            response_format={"type": "json_object"},
            temperature=0.7
        )

        result = json.loads(response.choices[0].message.content)

        # BMI 재계산 (정확도 보장)
        h_m = request.height / 100
        result["bmi"] = round(request.weight / (h_m * h_m), 1)

        return result

    except Exception as e:
        print(f"❌ 분석 실패: {e}")
        # 기본값 반환
        return {
            "bmi": 0.0,
            "title": "데이터 부족한 유령 회원",
            "healthTier": "입문자",
            "vulnerableParts": [],
            "prediction": "데이터가 부족하여 미래를 볼 수 없습니다.",
            "healthScore": {"muscle": 40, "endurance": 40, "recovery": 40, "nutrition": 40, "metabolism": 40},
            "tags": ["#데이터필요"],
            "actionTip": "AI 서버 상태를 확인해주세요."
        }

# =========================================================
# 5. 음식 영양성분 분석/생성 API
# EndPoint: /api/food/create
# 설명: 음식 이름을 입력받아 영양성분 JSON 데이터 반환
# =========================================================

class FoodAnalyzeRequest(BaseModel):
    foodName: str
@app.post("/api/food/create")
def analyze_food_info(request: FoodAnalyzeRequest):
    print(f"🍎 [음식 분석 요청] 입력값: {request.foodName}")

    try:
        system_instruction = """
        역할: 당신은 전문 영양사입니다.
        
        지시사항:
        1. 입력값이 사람이 먹는 '음식', '음료', '영양제'인지 판단하세요.
        2. 음식이 아니라면 "isFood": 0 를 반환하세요.
        3. 음식이라면 "isFood": 1 와 함께, 해당 음식 1인분 기준의 영양성분을 추정하여 DB 테이블 컬럼명에 맞게 반환하세요.
        4. 값은 숫자(Int/Double)여야 하며, 단위는 생략합니다.
        
        [데이터베이스 스키마 매핑 규칙]
        - name: 음식 이름 (String)
        - dataTypeName: "가공식품" 또는 "농축산물" 또는 "음식" 중 적절한 것 선택 (String)
        - middleCategoryName: 음식의 대분류 (예: 면류, 밥류, 빵류, 육류 등) (String)
        - foodWeight: 1인분 총 중량 (g 단위 숫자)
        - servingSize: 1인분 기준 표기, 100으로 고정
        - energy: 칼로리 (kcal 단위 숫자)
        - water: 수분 (g 단위 숫자, 추정치)
        - protein: 단백질 (g 단위 숫자)
        - fat: 지방 (g 단위 숫자)
        - carbohydrate: 탄수화물 (g 단위 숫자)
        - sugars: 당류 (g 단위 숫자)
        - sodium: 나트륨 (mg 단위 숫자)

        [응답 형식 (JSON Only)]
        {
          "isFood": 1,
          "name": "짜파구리",
          "dataTypeName": "음식",
          "middleCategoryName": "면류",
          "foodWeight": 400.0,
          "servingSize": "100",
          "energy": 500.0,
          "water": 200.0,
          "protein": 12.0,
          "fat": 15.0,
          "carbohydrate": 80.0,
          "sugars": 5.0,
          "sodium": 1200.0
        }
        """

        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": request.foodName}
            ],
            temperature=0.3,
            response_format={"type": "json_object"}
        )

        result = json.loads(completion.choices[0].message.content)
        print("\n🔎 [AI 응답 전체 데이터 확인]", flush=True)
        print(json.dumps(result, indent=4, ensure_ascii=False), flush=True)
        print("--------------------------------------------------\n", flush=True)
        #

        return result

    except Exception as e:
        print(f"❌ 음식 분석 에러: {e}")
        # 에러 발생 시 isFood: false로 안전하게 반환하거나 500 에러 발생
        return {"isFood": False, "error": str(e)}
if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)


