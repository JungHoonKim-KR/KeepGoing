from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import json
import os
import base64
from openai import OpenAI
from dotenv import load_dotenv
from typing import List, Optional, Any

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
        1. "cal" (칼로리) 필드는 절대 1500으로 고정하지 마.
        2. 그 날짜의 아침, 점심, 저녁 메뉴 구성을 보고 실제 대략적인 총 칼로리를 계산해서 정수값으로 넣어. (예: 1450, 1620, 1800 등 다양하게)
        3. 메뉴의 키 값은 반드시 영어 소문자("breakfast", "lunch", "dinner")를 사용해. (한글 "아침" 금지)

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



# =========================================================
# 3. 음식 사진 스캔 (칼로리 분석) API
# EndPoint: /api/diet/scan
# 설명: 사진을 분석하여 음식명, 칼로리, 소모 운동량(고정형) 반환
# =========================================================
@app.post("/api/diet/scan")
def scan_food_image(file: UploadFile = File(...)):
    print(f"📸 [음식 스캔 요청] 파일명: {file.filename}")

    try:
        contents = file.file.read()
        base64_image = base64.b64encode(contents).decode('utf-8')

        system_instruction = """
        너는 'AI 칼로리 측정기'야. 사진 속 음식을 분석해줘.

        [응답 포맷 (JSON Only)]
        {{
            "name": "음식 이름 (예: 페퍼로니 피자)",
            "emoji": "🍕",
            "calories": 0 (총 칼로리 정수),
            "exercise": {{
                "running": 0 (분),
                "walking": 0 (분),
                "swimming": 0 (분)
            }}
        }}
        """

        completion = client.chat.completions.create(
            model="gpt-4o",  # Vision 기능은 gpt-4o 필수
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

        return json.loads(completion.choices[0].message.content)

    except Exception as e:
        print(f"❌ 이미지 분석 에러: {e}")
        return {
            "name": "분석 실패", "emoji": "❌", "calories": 0,
            "exercise": {"running": 0, "walking": 0, "swimming": 0}
        }


# =========================================================
# 4. RPG 바디 스캔
# EndPoint: /api/body/scan
# 설명: 키/몸무게 기반 RPG 캐릭터 생성
# =========================================================
@app.post("/api/body/scan")
def body_scan(request: BodyScanRequest):
    print(f"🧬 [바디 스캔] 키:{request.height}, 몸무게:{request.weight}")

    h_m = request.height / 100
    bmi = request.weight / (h_m * h_m)

    char_class = "UNKNOWN"
    desc = ""
    stats = {"hp": 50, "str": 50, "agi": 50, "def": 50}

    if bmi < 18.5:
        char_class = "SKELETON THIEF"
        desc = "가볍고 빠르지만 내구력이 약합니다. 잘 먹는 것이 곧 훈련입니다."
        stats = {"hp": 30, "str": 20, "agi": 95, "def": 10}
    elif 18.5 <= bmi < 23:
        char_class = "BALANCED KNIGHT"
        desc = "가장 이상적인 밸런스입니다. 꾸준한 훈련으로 전설이 되세요."
        stats = {"hp": 75, "str": 60, "agi": 60, "def": 60}
    elif 23 <= bmi < 25:
        char_class = "ORC WARRIOR"
        desc = "넘치는 힘! 지방 갑옷을 근육으로 바꾸면 최강이 됩니다."
        stats = {"hp": 90, "str": 85, "agi": 40, "def": 70}
    else:
        char_class = "IRON GOLEM"
        desc = "압도적인 탱킹 능력! 하지만 움직임이 둔합니다. 유산소가 시급합니다."
        stats = {"hp": 100, "str": 95, "agi": 10, "def": 90}

    return {
        "bmi": round(bmi, 1),
        "class": char_class,
        "desc": desc,
        "stats": stats
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)