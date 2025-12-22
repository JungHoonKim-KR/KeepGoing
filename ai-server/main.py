from fastapi import FastAPI
from pydantic import BaseModel
import json
import os
from openai import OpenAI
from dotenv import load_dotenv 

# 1. 환경 변수 로드
load_dotenv()
API_KEY = os.getenv("OPENAI_API_KEY")

# SSAFY GMS 설정
client = OpenAI(
    api_key=API_KEY,
    base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
)


app = FastAPI()

# --- DTO (Data Transfer Objects) ---

class UserProfile(BaseModel):
    goal: str  # 예: "다이어트", "근비대"
    # necessary info...

class DailyLog(BaseModel):
    date: str
    dietSummary: str # 오늘 먹은 음식 리스트 문자열

class DietRequest(BaseModel):
    userProfile: UserProfile
    dailyLog: DailyLog

# ---------------------------------------------------------
# 1. 식단 분석 API (평가 + 아이템 채굴)
# ---------------------------------------------------------
@app.post("/analyze")
def analyze_diet(request: DietRequest):
    print(f"🧐 [분석 요청] {request.dailyLog.date}")

    try:
        system_instruction = f"""
        너는 'AI 영양사'야. 사용자의 오늘 식단을 영양학적으로 평가해줘.
        
        [필수 수행 과제]
        1. 영양 균형 점수(0~100)와 등급(S/A/B/C/F)을 매길 것.
        2. 식단에서 '게임 아이템'으로 만들만한 **대표 음식 키워드 5개**를 명사로 추출할 것.
        3. 잘한 점과 아쉬운 점을 분석할 것.

        [응답 포맷 (JSON Only)]
        {{
          "score": 85,
          "rank": "B",
          "dailyTitle": "단백질은 충분하지만 지방이 과해요",
          "oneLineSummary": "치킨을 조금만 줄였다면 완벽했을 하루입니다.",
          "insights": [
             {{ "type": "positive", "iconType": "muscle", "title": "단백질 섭취", "description": "근성장에 충분한 양입니다." }},
             {{ "type": "negative", "iconType": "warning", "title": "나트륨 주의", "description": "국물 섭취가 많았습니다." }}
          ],
          "miningKeywords": [
             "닭가슴살", "현미밥", "아메리카노", "사과", "계란"
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
            temperature=0.3,
            response_format={"type": "json_object"} # JSON 모드 강제 (안정성 UP)
        )

        response_content = completion.choices[0].message.content
        return json.loads(response_content)

    except Exception as e:
        print(f"❌ 분석 에러: {e}")
        return {
            "score": 0, "rank": "F", "dailyTitle": "분석 실패", 
            "oneLineSummary": "AI 서버 연결 상태를 확인해주세요.",
            "insights": [], "miningKeywords": []
        }

# ---------------------------------------------------------
# 2. 식단 추천 API (내일 메뉴 제안)
# ---------------------------------------------------------
@app.post("/recommend")
def recommend_diet(request: DietRequest):
    print(f"🍽️ [추천 요청] {request.userProfile.goal}")

    try:
        system_instruction = f"""
        너는 'AI 퍼스널 트레이너'야. 
        사용자의 목표와 오늘 먹은 음식을 바탕으로, **내일 먹으면 좋을 식단 3가지**를 추천해줘.
        
        [고려 사항]
        - 오늘 부족했던 영양소를 보충하거나, 과했던 영양소를 조절할 수 있는 메뉴여야 함.
        - 사용자의 목표({request.userProfile.goal})에 부합해야 함.

        [응답 포맷 (JSON Only)]
        {{
          "recommendations": [
             {{ "menu": "아침 추천: 그릭요거트와 블루베리", "reason": "어제 저녁이 무거웠으니 가벼운 유산균 섭취가 필요해요." }},
             {{ "menu": "점심 추천: 현미밥과 제육볶음(지방 제거)", "reason": "활동량이 많은 시간대에 탄수화물을 보충하세요." }},
             {{ "menu": "저녁 추천: 연어 샐러드", "reason": "오메가3 지방산으로 염증을 줄이고 포만감을 줍니다." }}
          ]
        }}
        """

        user_message = f"사용자 목표: {request.userProfile.goal}\n참고(오늘 먹은 것): {request.dailyLog.dietSummary}"

        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": user_message}
            ],
            temperature=0.7, # 추천은 조금 더 창의적이게
            response_format={"type": "json_object"}
        )

        response_content = completion.choices[0].message.content
        return json.loads(response_content)

    except Exception as e:
        print(f"❌ 추천 에러: {e}")
        return { "recommendations": [] }