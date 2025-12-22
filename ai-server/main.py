from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import json
import os
import base64
from openai import OpenAI
from dotenv import load_dotenv
from typing import List, Optional

# 1. 환경 변수 로드
load_dotenv()
API_KEY = os.getenv("OPENAI_API_KEY")

# SSAFY GMS 설정
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
# 1. [기존] 식단 분석 API
# EndPoint: /api/diet/analyze
# =========================================================
@app.post("/api/diet/analyze")
def analyze_diet(request: DietRequest):
    print(f"🧐 [식단 분석 요청] {request.dailyLog.date}")

    try:
        system_instruction = f"""
        너는 'AI 영양사'야. 사용자의 오늘 식단을 평가해줘. JSON 포맷 준수.

        [필수 포함]
        1. score (0~100), rank (S/A/B/C/F)
        2. dailyTitle (한줄 요약 제목)
        3. oneLineSummary (총평)
        4. insights (배열: type, iconType, title, description)
           - iconType: muscle, warning, balance, water 중 택1
        5. miningKeywords (배열: 음식명 문자열 5개)

        [응답 예시]
        {{
          "score": 85, "rank": "B", "dailyTitle": "제목", "oneLineSummary": "총평",
          "insights": [ {{ "type": "positive", "iconType": "muscle", "title": "제목", "description": "내용" }} ],
          "miningKeywords": ["닭가슴살", "사과", "아메리카노", "현미밥", "계란"]
        }}
        """

        user_message = f"오늘 식단: {request.dailyLog.dietSummary}\n목표: {request.userProfile.goal}"

        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": user_message}
            ],
            temperature=0.3,
            response_format={"type": "json_object"}
        )
        # 여기는 객체 {} 형태가 맞음 (자바 DTO와 매핑됨)
        return json.loads(completion.choices[0].message.content)

    except Exception as e:
        print(f"❌ 분석 에러: {e}")
        raise HTTPException(status_code=500, detail="AI 분석 실패")


# =========================================================
# 2. [New] AI 맞춤형 식단 생성 API (여기가 문제였음)
# EndPoint: /api/diet/generate
# =========================================================
@app.post("/api/diet/generate")
def generate_diet_plan(request: DietGenerationRequest):
    print(f"📅 [식단 생성 요청] 기간: {request.duration}일, 목표: {request.favorite}")

    try:
        # 중요: response_format={"type": "json_object"}는 최상위가 {}여야 합니다.
        # 따라서 AI에게 "plans"라는 키 안에 배열을 담으라고 지시해야 합니다.
        system_instruction = f"""
        너는 전문 영양사야. 사용자의 생활 습관 설문을 바탕으로 {request.duration}일치 식단표를 짜줘.

        [사용자 정보]
        - 운동: {request.exercise}
        - 수면: {request.sleep}
        - 선호 음식: {request.favorite} (건강하게 포함)

        [응답 포맷 (JSON Only)]
        반드시 "plans" 라는 키 안에 배열을 담아서 줘.
        {{
            "plans": [
                {{ 
                  "day": 1, 
                  "menu": "현미밥과 닭가슴살 샐러드", 
                  "cal": 500, 
                  "difficulty": "EASY",
                  "quest": "물 2L 마시기"
                }},
                ...
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

        # [핵심 수정] 자바는 List<?>를 원하므로, 딕셔너리를 벗기고 내용물 리스트만 리턴
        if "plans" in result:
            return result["plans"]

        # 혹시 키 이름이 다를 경우를 대비해 값 중 리스트인 것을 찾음
        for value in result.values():
            if isinstance(value, list):
                return value

        # 리스트가 없으면 빈 배열이라도 줘야 자바가 안 죽음
        print("⚠️ AI 응답에 배열이 없습니다. 빈 배열 반환.")
        return []

    except Exception as e:
        print(f"❌ 식단 생성 에러: {e}")
        # 에러 발생 시에도 빈 리스트나 더미 리스트 반환해야 자바 에러 방지
        return [
            {"day": i + 1, "menu": "일시적 오류", "cal": 0, "difficulty": "EASY", "quest": "잠시 후 다시 시도"}
            for i in range(request.duration)
        ]


# =========================================================
# 3. [New] 음식 사진 스캔 (칼로리 분석) API
# EndPoint: /api/diet/scan
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
            "name": "음식 이름",
            "emoji": "🍕",
            "calories": 0 (정수),
            "exercise": {{
                "running": 0,
                "walking": 0,
                "swimming": 0
            }}
        }}
        """

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
            max_tokens=300,
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
# 4. [New] RPG 바디 스캔
# EndPoint: /api/body/scan
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
        desc = "가볍고 빠르지만 스치면 부러집니다. 단백질 섭취가 시급합니다."
        stats = {"hp": 30, "str": 20, "agi": 95, "def": 10}
    elif 18.5 <= bmi < 23:
        char_class = "BALANCED KNIGHT"
        desc = "가장 이상적인 밸런스입니다. 근육량만 늘리면 완벽합니다."
        stats = {"hp": 75, "str": 60, "agi": 60, "def": 60}
    elif 23 <= bmi < 25:
        char_class = "ORC WARRIOR"
        desc = "힘이 넘치지만 지방 갑옷이 조금 두껍습니다. 커팅이 필요합니다."
        stats = {"hp": 90, "str": 85, "agi": 40, "def": 70}
    else:
        char_class = "HEAVY GOLEM"
        desc = "압도적인 탱킹 능력! 하지만 계단을 오를 때 HP가 급격히 소모됩니다."
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